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

import java.util.Iterator;
import java.util.List;

/**
 * Implementation of {@code RowReader} instances are the main construction
 * part for new tables with the {@link TableBuilder}.
 * <p>The {@linkplain #getEntryDescriptors() list of entry descriptors} describes
 * the type of the column of a table.
 * @see Table
 * @see Table#executeSql(java.lang.String) 
 */
public interface RowReader extends Iterable<Row>, AutoCloseable {
    
    /**
     * The description of the column type.
     * <p><strong>Note:</strong> The {@linkplain EntryDescriptor#getType()} type
     * may change at runtime at every time.</p>
     * @return a descriptor list. Never {@code null}
     */
    List<EntryDescriptor> getEntryDescriptors();

    /** Iterator over the rows of a reader.
     * @return never {@code null}.
     */
    @Override
    Iterator<Row> iterator();
}
