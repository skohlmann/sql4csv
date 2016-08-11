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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import static org.apache.commons.csv.CSVFormat.RFC4180;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads CSV files. Currently CSV files with format RFC 4180 are supported only.
 * The first line of the CSV file is interpreted as the header.
 */
public final class CsvReader implements RowReader {

    private static final Logger LOG = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

    static final String TYPE_MAP_POSTFIX = ".map";
    
    private CSVParser parser;
    private Map<String, Integer> headerMap;
    private Iterator<CSVRecord> itr;
    private List<EntryDescriptor> descriptors;
    private final Reader reader;
    private boolean ownReader;

    /** Creates a new {@code CsvReader} for the given <em>reader</em>.
     * @param reader the reader to read the CSV content from
     * @throws NullPointerException if the given <em>reader</em> is {@code null}
     * @throws IOException if is not possible to init the reader. 
     */
    public CsvReader(final Reader reader) throws IOException {
        Objects.requireNonNull(reader, "reader is null");
        this.reader = reader;
        this.ownReader = false;
        init(reader);
    }
    
    /** Creates a new {@code CsvReader} for the given <em>path</em> in the
     * file system.
     * @param path the path to a CSV file in the file system to read the CSV content from
     * @throws NullPointerException if the given <em>reader</em> is {@code null}
     * @throws IOException if is not possible to init the reader. 
     */
    public CsvReader(final String path) throws IOException, FileNotFoundException {
        this(new FileReader(path));
        this.ownReader = true;
    }

    void init(final Reader reader) throws IOException {
        synchronized(this) {
            Objects.requireNonNull(reader, "reader is null");
            this.parser = RFC4180.withFirstRecordAsHeader().parse(reader);
            this.headerMap = this.parser.getHeaderMap();
            this.itr = this.parser.iterator();

            final int headerMapSize = this.headerMap.size();
            this.descriptors = new ArrayList<>(headerMapSize);
            for (int i = 0; i < headerMapSize; i++) {
                for (final Map.Entry<String, Integer> e : this.headerMap.entrySet()) {
                    if (e.getValue() == i) {
                        final EntryDescriptorBuilder edb = new EntryDescriptorBuilder();
                        edb.addName(e.getKey());
                        this.descriptors.add(edb.build());
                    }
                }
            }
        }
    }

    @Override
    public Iterator<Row> iterator() {
        return new Iterator<Row>() {
            @Override
            public boolean hasNext() {
                return CsvReader.this.itr.hasNext();
            }

            @Override
            public Row next() {
                final CSVRecord record = CsvReader.this.itr.next();
                final List<Entry> entries = new ArrayList<>();
                
                CsvReader.this.descriptors.stream().forEach((desc) -> {
                    final String value = record.get(desc.getName());
                    final SimpleEntry<String> entry = new SimpleEntry<>(value, desc);
                    entries.add(entry);
                });
                
                return new SimpleRow(entries);
            }
        };
    }

    @Override
    public void close() throws Exception {
        if (this.ownReader) {
            this.reader.close();
        }
        this.parser.close();
    }

    @Override
    public List<EntryDescriptor> getEntryDescriptors() {
        return Collections.unmodifiableList(this.descriptors);
    }
    
    static class SimpleEntry<T> implements Entry<T> {
        private final EntryDescriptor descriptor;
        private final T value;

        public SimpleEntry(final T value, final EntryDescriptor descriptor) {
            assert value != null;
            assert descriptor != null;
            this.value = value;
            this.descriptor = descriptor;
        }
        
        @Override
        public EntryDescriptor getDescriptor() {
            return this.descriptor;
        }

        @Override
        public T getValue() {
            return this.value;
        }
    }
    
    static class SimpleRow implements Row {
        
        private final List<Entry> entries;
        
        public SimpleRow(final List<Entry> entries) {
            assert entries != null;
            this.entries = entries;
        }

        @Override
        public int size() {
            assert this.entries != null;
            return this.entries.size();
        }

        @Override
        public Iterator<Entry> iterator() {
            assert this.entries != null;
            return this.entries.iterator();
        }
    }
}
