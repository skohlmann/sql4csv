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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import static de.speexx.csv.table.EntryDescriptor.Type.STRING;
import static de.speexx.csv.table.EntryDescriptor.Type.DATE;
import static de.speexx.csv.table.EntryDescriptor.Type.DATETIME;
import static de.speexx.csv.table.EntryDescriptor.Type.TIME;
import static de.speexx.csv.table.EntryDescriptor.Type.DECIMAL;
import static de.speexx.csv.table.EntryDescriptor.Type.INTEGER;
import de.speexx.csv.table.EntryDescriptor.Type;
import static de.speexx.csv.table.util.IteratorSupport.asStream;

public final class SimpleRowDataMetric implements RowDataMetric {

    private static final List<Type> CHECKERS = new ArrayList<>(Arrays.asList(
            DATETIME, DATE, INTEGER, DECIMAL, TIME, STRING
        )
    );

    private final TypeMetric metric = new TypeMetric();

    public Optional<EntryDescriptor.Type> getMostSignificantTypeForName(final String name) {
        if (Objects.isNull(name)) {
            return Optional.empty();
        }
        final Map<Type, AtomicInteger> frequencyMap = this.metric.getFrequencyMapForName(name);

        if (Objects.isNull(frequencyMap)) {
            return Optional.empty();
        }
        
        if (frequencyMap.containsKey(DECIMAL) 
                && !(frequencyMap.containsKey(STRING) || frequencyMap.containsKey(TIME) || frequencyMap.containsKey(DATE) || frequencyMap.containsKey(DATETIME))) {
            return Optional.of(DECIMAL);
        }
        if (frequencyMap.containsKey(INTEGER) 
                && !(frequencyMap.containsKey(STRING) || frequencyMap.containsKey(TIME) || frequencyMap.containsKey(DATE) || frequencyMap.containsKey(DATETIME))) {
            return Optional.of(INTEGER);
        }
        if (frequencyMap.containsKey(TIME) 
                && !(frequencyMap.containsKey(STRING) || frequencyMap.containsKey(INTEGER) || frequencyMap.containsKey(DECIMAL) || frequencyMap.containsKey(DATE) || frequencyMap.containsKey(DATETIME))) {
            return Optional.of(TIME);
        }
        if (frequencyMap.containsKey(DATE) 
                && !(frequencyMap.containsKey(STRING) || frequencyMap.containsKey(INTEGER) || frequencyMap.containsKey(DECIMAL) || frequencyMap.containsKey(TIME) || frequencyMap.containsKey(DATETIME))) {
            return Optional.of(DATE);
        }
        if (frequencyMap.containsKey(DATETIME) 
                && !(frequencyMap.containsKey(STRING) || frequencyMap.containsKey(INTEGER) || frequencyMap.containsKey(DECIMAL) || frequencyMap.containsKey(DATE) || frequencyMap.containsKey(TIME))) {
            return Optional.of(DATETIME);
        }

        return Optional.of(STRING);
    }

    @Override
    public void collectRowData(final Row row) {
        if (Objects.isNull(row)) {
            return;
        }
        
        asStream(row, row.size() >= 20 /* check this */ ).filter(Objects::nonNull)
                    .forEach(e -> {
            
            assert Objects.nonNull(CHECKERS);
            for (final Type checker : CHECKERS) {
                if (checker.isTypeMatch((String) e.getValue())) {
                    final EntryDescriptor descriptor = e.getDescriptor();
                    if (Objects.nonNull(descriptor)) {
                        assert Objects.nonNull(this.metric);
                        this.metric.incrementTypeForName(descriptor.getName(), checker);
                        break;
                    }
                }
            }
        });
    }
    
    final TypeMetric getTypeMetric() {
        return this.metric;
    }
}
