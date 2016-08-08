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

import de.speexx.csv.table.util.Conditions;
import java.util.Objects;
import static de.speexx.csv.table.util.UuidSupport.shortUuid;

/**
 * Creates new {@link Table} instances. Use {@link #of()} to create a new
 * builder instance.
 */
public abstract class TableBuilder {

    /**
     * Creates a new instance.
     * @return never {@code null}
     */
    public static TableBuilder of() {

        return new TableBuilder() {
            private String name;
            private RowReader reader;

            @Override
            public TableBuilder addName(final String name) {
                this.name = name;
                return this;
            }
            @Override
            public TableBuilder addRowReader(final RowReader reader) {
                this.reader = reader;
                return this;
            }
            @Override
            public Table build() {
                this.name = Conditions.requireNonNullElse(this.name, createTempTableName());
                this.reader = Objects.requireNonNull(this.reader, "reader is null");
                if (this.name.length() == 0) {
                    throw new IllegalStateException("table name is zero");
                }
                
                final DbTable table = new DbTable(this.name);
                table.init(this.reader);
                return table;
            }
            
            final String createTempTableName() {
                return "a" + shortUuid();
            }
        };
    }
    
    /**
     * Sets the name of the table to build.
     * <p>The name is not required. The {@linkplain #build() build method}
     * creates an own name if required.</p>
     * @param name the name of the table
     * @return a reference to this object
     */
    public abstract TableBuilder addName(final String name);
    /**
     * Sets the reader to get the table content from (column, rows).
     * @param reader the reader
     * @return a reference to this object
     */
    public abstract TableBuilder addRowReader(final RowReader reader);
    
    /**
     * Builds a new table from the given data.
     * @return a new table. Never {@code null}
     * @throws TableException if and only if it is not possible to create a new table
     */
    public abstract Table build();
}
