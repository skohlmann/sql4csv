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
import de.speexx.csv.table.TransformationException;
import de.speexx.csv.table.transformer.TypeTransformer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import static de.speexx.csv.table.util.IteratorSupport.asStream;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import static de.speexx.csv.table.EntryDescriptor.Type.STRING;
import static de.speexx.csv.table.EntryDescriptor.Type.DATE;
import static de.speexx.csv.table.EntryDescriptor.Type.DATETIME;
import static de.speexx.csv.table.EntryDescriptor.Type.TIME;
import static de.speexx.csv.table.EntryDescriptor.Type.DECIMAL;
import static de.speexx.csv.table.EntryDescriptor.Type.INTEGER;

public final class SimpleRowDataMetric implements RowDataMetric {

    private static final List<TypeTransformer> TRANSFORMERS = new ArrayList<>(Arrays.asList(
            TypeTransformer.of(STRING, DATETIME),
            TypeTransformer.of(STRING, DATE),
            TypeTransformer.of(STRING, INTEGER),
            TypeTransformer.of(STRING, DECIMAL),
            TypeTransformer.of(STRING, TIME)
        )
    );

    private final TypeMetric metric = new TypeMetric();

    public Optional<EntryDescriptor.Type> getMostSignificantTypeForName(final String name) {
        if (Objects.isNull(name)) {
            return Optional.empty();
        }
        final Map<Class<?>, AtomicInteger> frequencyMap = this.metric.getFrequencyMapForName(name);

        if (Objects.isNull(frequencyMap)) {
            return Optional.empty();
        }
        
        if (frequencyMap.containsKey(Long.class)) {
            return Optional.of(INTEGER);
        }
        if (frequencyMap.containsKey(Time.class)) {
            return Optional.of(TIME);
        }
        if (frequencyMap.containsKey(Double.class)) {
            return Optional.of(DECIMAL);
        }
        if (frequencyMap.containsKey(Date.class)) {
            return Optional.of(DATE);
        }
        if (frequencyMap.containsKey(Timestamp.class)) {
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
            
            assert Objects.nonNull(TRANSFORMERS);
            for (final TypeTransformer transformer : TRANSFORMERS) {
                try {
                    final Optional<Object> target = transformer.transform(e.getValue());
                    if (target.isPresent()) {
                        final Class<?> targetType = target.get().getClass();
                        final EntryDescriptor descriptor = e.getDescriptor();
                        if (Objects.nonNull(descriptor)) {
                            assert Objects.nonNull(this.metric);
                            this.metric.incrementTypeForName(descriptor.getName(), targetType);
                            break;
                        }
                    }
                } catch (final TransformationException ex) {
                    // can be ignored because the behavior is to measure frequency
                    // not to really transform.
                }
            }
        });
    }
}
