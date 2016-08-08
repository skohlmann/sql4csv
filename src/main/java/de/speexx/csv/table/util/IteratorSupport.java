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
package de.speexx.csv.table.util;

import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import static de.speexx.csv.table.util.Conditions.requireNonNullElse;
import java.util.Collections;

public final class IteratorSupport {
    
    private IteratorSupport() {
        throw new AssertionError("Forbidden to create instance from " + IteratorSupport.class);
    }

    public static <T> Stream<T> asStream(final Iterator<T> sourceIterator) {
        return asStream(sourceIterator, false);
    }

    /**
     * Transforms a given {@code Iterable} into a sequential {@code Stream}.
     * If the {@code Iterable} is {@code null} the return value is an empty
     * {@code Stream}.
     * @param <T> the type of the stream
     * @param sourceIterable an iterable
     * @return a stream
     */
    public static <T> Stream<T> asStream(final Iterable<T> sourceIterable) {
        return asStream(sourceIterable, false);
    }

    /**
     * Transforms a given {@code Iterable} into a {@code Stream}.
     * If the {@code Iterable} is {@code null} the return value is an empty
     * {@code Stream}.
     * <p>This method is a {@code NullPointerException} safe shortcut for</p>
     * <pre>StreamSupport.stream(sourceIterable.spliterator(), parallel)</pre>
     * @param <T> the type of the stream
     * @param sourceIterable an iterable
     * @param parallel {@code true} for a parallel stream, otherwise {@code false} for a sequential stream.
     * @return a stream
     */
    public static <T> Stream<T> asStream(final Iterable<T> sourceIterable, final boolean parallel) {
        final Iterable iterable = requireNonNullElse(sourceIterable, Collections.emptySet());
        return StreamSupport.stream(iterable.spliterator(), parallel);
    }

    public static <T> Stream<T> asStream(final Iterator<T> sourceIterator, final boolean parallel) {
        final Iterator itr = requireNonNullElse(sourceIterator, Collections.emptyIterator());
        final Iterable<T> iterable = () -> itr;
        return StreamSupport.stream(iterable.spliterator(), parallel);
    }
}
