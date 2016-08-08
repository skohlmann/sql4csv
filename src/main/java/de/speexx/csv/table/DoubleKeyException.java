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
 * Throwing the exception indicates that the key is a east two times
 * available to describe the columns of a table (e.g. CSV header).
 */
public class DoubleKeyException extends RuntimeException {

    private final String name;
    /**
     * The doublicated key.
     * @return the doublicated id name if given. Otherwise <tt>null</tt>.
     */
    public final String getIdName() {
        return this.name;
    }

    /**
     * Constructs an instance of <code>TableException</code> with the specified
     * detail message.
     *
     * @param msg the detail message.
     * @param idName the doublicated id name.
     */
    public DoubleKeyException(final String idName, final String msg) {
        super(msg);
        this.name = idName;
    }
}
