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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final class EntryDescriptorSupport {

    public static List<TypeChangeableEntryDescriptor> cloneEntryDescriptorList(final List<EntryDescriptor> list) {
        final List<TypeChangeableEntryDescriptor> newList = new ArrayList<>(list.size());
        list.forEach(descriptor -> newList.add(new TypeChangeableEntryDescriptor(descriptor)));
        return newList;
    }

    public static final class TypeChangeableEntryDescriptor implements EntryDescriptor {
        
        private Type type;
        private final String name;
        
        public TypeChangeableEntryDescriptor(final EntryDescriptor desc) {
            this.type = Objects.requireNonNull(desc.getType(), "descriptor contains no type");
            this.name = Objects.requireNonNull(desc.getName(), "descriptor contains no name");
        }

        public void setType(final Type type) {
            this.type = Objects.requireNonNull(type);
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
            return "EntryDescriptor{" + "type=" + type + ", name=" + name + '}';
        }
    }
}
