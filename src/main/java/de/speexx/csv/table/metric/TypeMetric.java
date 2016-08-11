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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import de.speexx.csv.table.EntryDescriptor.Type;
import java.util.Collections;

final class TypeMetric {

    private final Map<String, Map<Type, AtomicInteger>> nameTypeFrequence = new HashMap<>();

    public void incrementTypeForName(final String name, final Type type) {
        Objects.requireNonNull(name, "name is null");
        Objects.requireNonNull(type, "type class is null");

        Map<Type, AtomicInteger> typeCountMap = this.nameTypeFrequence.get(name);
        if (typeCountMap == null) {
            typeCountMap = new HashMap<>();
            this.nameTypeFrequence.put(name, typeCountMap);
        }
        AtomicInteger typeCount = typeCountMap.get(type);
        if (typeCount == null) {
            typeCount = new AtomicInteger(0);
            typeCountMap.put(type, typeCount);
        }
        typeCount.incrementAndGet();
    }

    final Map<Type, AtomicInteger> getFrequencyMapForName(final String name) {
        if (name == null) {
            return null;
        }
        return this.nameTypeFrequence.get(name);
    }
    
    Map<String, Map<Type, AtomicInteger>> getNameTypeFrequence() {
        return Collections.unmodifiableMap(this.nameTypeFrequence);
    }
}
