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

import java.util.List;

/**
 * A Table represents a set of columns with rows. The columns are types by the
 * a {@link EntryDescriptor.Type}. The type is provides by the 
 * {@link #getEntryDescriptors() EntryDescriptor} list.
 * <p>A table supports the ececution of simple SQL select statements within the
 * {@link #executeSql(java.lang.String)} method. The result of such a query
 * is a {@link RowReader} which itself is the input to
 * {@linkplain TableBuilder#of() create additional tables}.
 * @see TableBuilder
 */
public interface Table {

    /** The name of the table.
     * @return the table name. */
    String getName();
    
    /** Descriptor of the table columns.
     * <p>The implementation may change the type of the columns during runtime.</p>
     * @return table column descriptor
     * @see #changeColumnType(java.lang.String, de.speexx.csv.table.EntryDescriptor.Type) 
     */
    List<? extends EntryDescriptor> getEntryDescriptors();

    /**
     * Executes an SQL select statement. The name of the from clause must be the
     * same as the {@linkplain #getName() table name}.
     * @param sql the select statement to execute
     * @return the result of the SQL select statement. Never {@code null}.
     * @throws TableException if the execution of the SQL command is not possible
     * @throws NullPointerException if the query is {@code null}
     */
    RowReader executeSql(final String sql);

    /**
     * Change the type of the columns from the current type to the given <em>newTypes</em>.
     * <p>If the given column for the given <em>columnName</em> does not exists,
     * the implementation returns silently. If the current type of the given
     * column is the same as of the given <em>newType</em> the implementation
     * returns silently.</p>
     * <p>Implementation detail: currently only the transformation from 
     * {@link EntryDescriptor.Type#STRING} to the other {@linkplain EntryDescriptor.Type types}
     * is possible.</p>
     * @param descriptors list of entry descriptors for the target type of the given column
     * @throws NullPointerException if a parameter is {@code null}
     * @throws TableException if the transformation of the type is not possible
     */
    void changeColumnTypes(final EntryDescriptor... descriptors);
}
