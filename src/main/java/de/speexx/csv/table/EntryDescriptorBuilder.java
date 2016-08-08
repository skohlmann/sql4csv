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

import java.util.Objects;

final class EntryDescriptorBuilder {
    
    private String name;
    private EntryDescriptor.Type type;
    
    public EntryDescriptorBuilder addName(final String name) {
        this.name = name;
        return this;
    }
    public EntryDescriptorBuilder addType(final EntryDescriptor.Type type) {
        this.type = type;
        return this;
    }
    
    public EntryDescriptor build() {
        final String ln = name;
        if (Objects.isNull(ln)) {
            throw new TableException("no entry name");
        }
        if (ln.length() == 0) {
            throw new TableException("entry name of size zero");
        }
        final EntryDescriptor.Type lt = this.type != null ? this.type : EntryDescriptor.Type.STRING;
        
        return new SimpleEntryDescriptor(ln, lt);
    }
    
    static class SimpleEntryDescriptor implements EntryDescriptor {
        private final String name;
        private final EntryDescriptor.Type type;
        
        SimpleEntryDescriptor(final String name, final EntryDescriptor.Type type) {
            assert name != null;
            assert type != null;
            this.name = name;
            this.type = type;
        }

        @Override
        public Type getType() {
            return this.type;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public String toString() {
            return "EntryDescriptor{" + "name=" + name + ", type=" + type + '}';
        }
    }
}
