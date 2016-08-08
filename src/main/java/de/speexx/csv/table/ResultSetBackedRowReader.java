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
package de.speexx.csv.table;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import static de.speexx.csv.table.EntryDescriptor.Type.DATE;
import static de.speexx.csv.table.EntryDescriptor.Type.DATETIME;
import static de.speexx.csv.table.EntryDescriptor.Type.DECIMAL;
import static de.speexx.csv.table.EntryDescriptor.Type.INTEGER;
import static de.speexx.csv.table.EntryDescriptor.Type.STRING;
import static de.speexx.csv.table.EntryDescriptor.Type.TIME;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ResultSetBackedRowReader implements RowReader {

    private static final Logger LOG = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

    private final List<EntryDescriptor> descriptors;
    private final DbTable.OriginalReplacementMap replacementMap;
    private final String rowNumberColumnName;
    private final List<Row> rows = new ArrayList<>();
    int rowNumberColumn = -1;

    public ResultSetBackedRowReader(final ResultSet result,
            final String rowNumberColumnName,
            final DbTable.OriginalReplacementMap replacementMap) throws SQLException {
        this.rowNumberColumnName = Objects.requireNonNull(rowNumberColumnName, "rowNumberColumnName is null");
        this.replacementMap = Objects.requireNonNull(replacementMap, "replacementMap is null");
        Objects.requireNonNull(result, "resultset is null");
        this.descriptors = createEntryListDescriptorsList(result);
        fillRows(result);
    }

    @Override
    public List<EntryDescriptor> getEntryDescriptors() {
        return this.descriptors;
    }

    @Override
    public Iterator<Row> iterator() {
        return this.rows.iterator();
    }

    @Override
    public void close() throws Exception {
    }

    final void fillRows(final ResultSet rs) throws SQLException {
        assert rs != null;
        final int descriptorSize = this.descriptors.size();
        final int maxEntriesPerDbRow = this.rowNumberColumn == -1 ? descriptorSize : descriptorSize + 1;

        while (rs.next()) {
            final List<Entry> entries = new ArrayList<>(descriptorSize);
            for (int columnIdx = 1, entryIdx = 0; columnIdx <= maxEntriesPerDbRow; columnIdx++, entryIdx++) {
                if (columnIdx != this.rowNumberColumn) {
                    final EntryDescriptor desc = this.descriptors.get(entryIdx);
                    switch (desc.getType()) {
                        case DATE: {
                            final LocalDate date = rs.getDate(columnIdx).toLocalDate();
                            entries.add(toEntry(desc, date));
                            break;
                        }
                        case DATETIME: {
                            final LocalDateTime datetime = rs.getTimestamp(columnIdx).toLocalDateTime();
                            entries.add(toEntry(desc, datetime));
                            break;
                        }
                        case TIME: {
                            final LocalTime datetime = rs.getTime(columnIdx).toLocalTime();
                            entries.add(toEntry(desc, datetime));
                            break;
                        }
                        case DECIMAL: {
                            final double d = rs.getDouble(columnIdx);
                            entries.add(toEntry(desc, d));
                            break;
                        }
                        case INTEGER: {
                            final long l = rs.getLong(columnIdx);
                            entries.add(toEntry(desc, l));
                            break;
                        }
                        case STRING: {
                            final String s = rs.getString(columnIdx);
                            entries.add(toEntry(desc, s));
                            break;
                        }
                        default:
                            throw new TableException("unsupported type: " + desc.getType());
                    }
                } else {
                    entryIdx--;
                }
            }
            final Row row = new Row() {
                @Override
                public int size() {
                    return entries.size();
                }

                @Override
                public Iterator<Entry> iterator() {
                    return entries.listIterator();
                }

                public String toString() {
                    return "Row{" + entries + "}";
                }
            };
            this.rows.add(row);
        }
    }

    final Entry toEntry(final EntryDescriptor desc, final Object o) {
        return new Entry() {
            @Override
            public EntryDescriptor getDescriptor() {
                return desc;
            }

            @Override
            public Object getValue() {
                return o;
            }

            @Override
            public String toString() {
                return "Entry{" + getDescriptor() + "; Value: " + getValue() + "}";
            }
        };
    }

    final List<EntryDescriptor> createEntryListDescriptorsList(final ResultSet result) throws SQLException {
        assert result != null;

        final ResultSetMetaData rsmd = result.getMetaData();
        final int numberOfColumns = rsmd.getColumnCount();
        final List<EntryDescriptor> descs = new ArrayList<>(numberOfColumns);
        for (int i = 1; i <= numberOfColumns; i++) {

            final String columnName = rsmd.getColumnName(i);
            if (!this.rowNumberColumnName.equals(columnName.toLowerCase(Locale.ENGLISH))) {
                final EntryDescriptorBuilder builder = new EntryDescriptorBuilder();
                final Optional<String> originalName = this.replacementMap.originalForReplacement(columnName);
                builder.addName(originalName.orElse(columnName));
                final int sqlType = rsmd.getColumnType(i);
                final EntryDescriptor.Type type =
                        EntryDescriptor.Type.getTypeForSqlType(sqlType).orElseThrow(() -> new TableException("No Type for SQL type " + sqlType));
                builder.addType(type);
                descs.add(builder.build());
            } else {
                this.rowNumberColumn = i;
            }
        }
        return Collections.unmodifiableList(descs);
    }

    @Override
    public String toString() {
        return "ResultSetBackedRowReader{" + "descriptors=" + descriptors + ", entries=" + rows + '}';
    }
}
