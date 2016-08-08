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
package de.speexx.csv.table.transformer;

/**
 * Thrown if the transformation is not supported.
 */
public class UnsupportedTransformationException extends RuntimeException {

    /**
     * Creates a new instance of <code>UnsupportedTransformationException</code>
     * without detail message.
     */
    public UnsupportedTransformationException() {
        super();
    }

    /**
     * Constructs an instance of <code>UnsupportedTransformationException</code>
     * with the specified detail message.
     *
     * @param msg the detail message.
     */
    public UnsupportedTransformationException(final String msg) {
        super(msg);
    }
}
