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

/**
 * The entry of a table.
 * @param <T> the concrete Java type of the entry described by the
 * {@linkplain #getDescriptor() entry descriptor}.
 */
public interface Entry<T> {

    /**
     * The descriptor of the entry.
     * @return never {@code null}
     */
    EntryDescriptor getDescriptor();
    
    /**
     * @return the value of the entry. Can be {@code null}
     */
    T getValue();
}
