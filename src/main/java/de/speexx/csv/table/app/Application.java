/* CSV query table to work with CSV files and SQL like statements.
 *
 * Copyright (C) 2016  Sascha Kohlmann
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.speexx.csv.table.app;

import com.beust.jcommander.JCommander;
import de.speexx.csv.table.CsvReader;
import de.speexx.csv.table.Entry;
import de.speexx.csv.table.EntryDescriptor;
import de.speexx.csv.table.Row;
import de.speexx.csv.table.RowReader;
import de.speexx.csv.table.Table;
import de.speexx.csv.table.TableBuilder;
import de.speexx.csv.table.TableException;
import de.speexx.csv.table.app.sql.FromInfo;
import de.speexx.csv.table.app.sql.SelectData;
import de.speexx.csv.table.app.sql.SelectQueryData;
import de.speexx.csv.table.metric.SimpleRowDataMetric;
import de.speexx.csv.table.metric.TypeIndentifyRowReaderDelegate;
import de.speexx.csv.table.transformer.TypeTransformer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Application {

    private static final Logger LOG = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

    public static void main(final String... args) {
        try {
            new Application().run(args);
        } catch (final Throwable t) {
            LOG.error("Unexpected end of application: {}", t.getMessage());
            final StackTraceElement[] st = t.getStackTrace();
            LOG.error("Location - class: {} - method: {} - line: {}", st[0].getClassName(), st[0].getMethodName(), st[0].getLineNumber());
            System.exit(1);
        }
        System.exit(0);
    }
    
    void run(final String... args) throws Exception {
        final Configuration conf = new Configuration();
        final JCommander jc = new JCommander(conf);
        jc.parse(args);
        
        if (conf.isHelp()) {
            jc.usage();
            return;
        }

        final Optional<List<Table>> tables = loadTable(conf);
        final Optional<RowReader> rows = executeQuery(conf, tables.orElseThrow(() -> new TableException("No table available")));
        if (rows.isPresent()) {
            exportResult(conf, rows.get());
        }
    }
    
    Optional<List<Table>> loadTable(final Configuration conf) throws Exception {
        if (conf.isVerbose()) {LOG.info("Load table");}
        final long loadStart = System.currentTimeMillis();

        final SelectData selectData = conf.getQueryData();
        final SelectQueryData queryData = selectData.getQueryData();

        final List<Table> tables = new ArrayList<>();
        for (final FromInfo fromInfo : queryData.getFromInfo()) {

            final RowReader reader = createSourceReader(fromInfo);
            if (conf.isWithoutTypeDetections()) {
                final Table table = loadTableFromSource(fromInfo, reader);
                doVerboseLog(conf, "Load table tock {}ms", System.currentTimeMillis() - loadStart);
                tables.add(table);
            } else {
                final SimpleRowDataMetric metric = new SimpleRowDataMetric();
                final TypeIndentifyRowReaderDelegate delegationReader = new TypeIndentifyRowReaderDelegate(reader, metric);

                final Table table = loadTableFromSource(fromInfo, delegationReader);
                doVerboseLog(conf, "Load table tock {}ms", System.currentTimeMillis() - loadStart);
                adjustTableColumns(conf, table, metric);
                tables.add(table);
            }
        }
        
        return Optional.of(tables);
    }

    RowReader createSourceReader(final FromInfo fromInfo) throws IOException {
        final String source = fromInfo.getOriginalFrom();
        return new CsvReader(source);
    }

    Table loadTableFromSource(final FromInfo fromInfo, final RowReader delegationReader) {
        final String adjusted = fromInfo.getAdjustedFrom();
        final TableBuilder tableBuilder = TableBuilder.of();
        return tableBuilder.addName(adjusted).addRowReader(delegationReader).build();
    }

    void adjustTableColumns(final Configuration conf, final Table table, final SimpleRowDataMetric metric) {
        assert Objects.nonNull(table) : "Table is null";
        assert Objects.nonNull(metric) : "Metric is null";
        
        if (conf.isVerbose()) {LOG.info("Adjust column types");}
        final long adjustStart = System.currentTimeMillis();
        table.getEntryDescriptors().stream().map((desc) -> desc.getName()).forEach((columnName) -> {
            final Optional<EntryDescriptor.Type> mostSignificantType = metric.getMostSignificantTypeForName(columnName);
            if (mostSignificantType.isPresent()) {
                final EntryDescriptor.Type type = mostSignificantType.get();
                if (type != EntryDescriptor.Type.STRING) { // From CSV there are only strings, so ignore that
                    final long changeStart = System.currentTimeMillis();
                    doVerboseLog(conf, "    Change column  '{}' to type {}", columnName, type);
                    table.changeColumnType(columnName, type);
                    doVerboseLog(conf, "    Changed column '{}' to type {} in {}ms", columnName, type, System.currentTimeMillis() - changeStart);
                }
            }
        });
        doVerboseLog(conf, "Adjust tables tock {}ms", System.currentTimeMillis() - adjustStart);
    }

    Optional<RowReader> executeQuery(final Configuration conf, final List<Table> tables) throws Exception {
        assert Objects.nonNull(conf) : "Configuration is null";
        assert Objects.nonNull(tables) : "No tables. Is null";

        // At this time only one table is supported. Might be improved later.
        if (!tables.isEmpty()) {
            final SelectQueryData queryData = conf.getQueryData().getQueryData();
            final String select = queryData.getAdjustedQuery().getQuery();
            final Table table = tables.get(0);
            final RowReader result = table.executeSql(select);
            return Optional.of(result);
        }
        return Optional.empty();
    }
    
    void exportResult(final Configuration conf, final RowReader rows) throws Exception {
        assert Objects.nonNull(rows) : "Rows are null";
        assert Objects.nonNull(conf) : "configuration is null";
        
        final CSVPrinter printer = createCsvPrinter(rows, conf);
        
        for (final Row row : rows) {
            final List<String> recordEntries = new ArrayList<>();
            for (final Entry entry : row) {
                final EntryDescriptor.Type type = entry.getDescriptor().getType();
                final TypeTransformer transformer = TypeTransformer.of(type, EntryDescriptor.Type.STRING);
                final Optional<String> opt = transformer.transform(entry.getValue());
                recordEntries.add(opt.orElseGet(() -> ""));
            }
            printer.printRecord(recordEntries);
        }
    }

    CSVPrinter createCsvPrinter(final RowReader rows, final Configuration conf) throws IOException {
        final List<EntryDescriptor> descriptors = rows.getEntryDescriptors();
        if (conf.isWithoutHeader()) {
            return CSVFormat.RFC4180.print(System.out);
        }
        final List<String> headers = descriptors.stream().map(desc -> desc.getName()).collect(Collectors.toList());
        return CSVFormat.RFC4180.withHeader(headers.toArray(new String[headers.size()])).print(System.out);
    }
    
    void doVerboseLog(final Configuration conf, final String message, final Object... values) {
        assert Objects.nonNull(conf);
        if (conf.isVerbose()) {
            LOG.info(message, values);
        }
    }
}
