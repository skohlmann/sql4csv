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
package de.speexx.csv.table.metric;

import de.speexx.csv.table.EntryDescriptor;
import de.speexx.csv.table.Row;
import de.speexx.csv.table.RowReader;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public final class TypeIndentifyRowReaderDelegate implements RowReader {
    
    private final RowDataMetric rowDataCollector;
    private final RowReader delegate;
    
    public TypeIndentifyRowReaderDelegate(final RowReader reader,
                                          final RowDataMetric collector) {
        this.delegate = Objects.requireNonNull(reader, "row reader is null");
        this.rowDataCollector = Objects.requireNonNull(collector, "collector is null");
    }


    @Override
    public List<EntryDescriptor> getEntryDescriptors() {
        assert this.delegate != null;
        return this.delegate.getEntryDescriptors();
    }

    @Override
    public Iterator<Row> iterator() {
        // yet not stable for multithreading
        assert this.delegate != null;
        return new IteratorDelegate(this.delegate.iterator());
    }

    @Override
    public void close() throws Exception {
        assert this.delegate != null;
        this.delegate.close();
    }
    
    final class IteratorDelegate implements Iterator<Row> {
        
        private final Iterator<Row> itr;
        
        public IteratorDelegate(final Iterator<Row> rowItr) {
            this.itr = Objects.requireNonNull(rowItr, "row iterator is null");
        }

        @Override
        public boolean hasNext() {
            assert Objects.nonNull(this.itr);
            try {
                return this.itr.hasNext();
            } catch (final Exception e) {
                return false;
            }
        }

        @Override
        public Row next() {
            assert Objects.nonNull(this.itr);
            final Row row = this.itr.next();
            assert Objects.nonNull(TypeIndentifyRowReaderDelegate.this.rowDataCollector);
            TypeIndentifyRowReaderDelegate.this.rowDataCollector.collectRowData(row);
            return row;
        }
    }
}
