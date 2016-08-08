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

import java.util.Objects;
import java.util.function.Supplier;

public final class Conditions {
    
    private Conditions() {
        throw new AssertionError("Forbidden to create instance from " + Conditions.class);
    }
    
    /**
     * Returns the first argument if it is non-<tt>null</tt>,
     * otherwise the non-<tt>null</tt> second argument.
     *
     * @param instance an object to test
     * @param defaultInstance a non-<tt>null</tt> object to return if the first argument
     *                   is <tt>null</tt>
     * @param <T> the type of the reference
     * @return the first argument if it is non-<tt>null</tt> and
     *        otherwise the second argument if it is non-<tt>null</tt>
     * @throws NullPointerException if both <tt>instance</tt> is null and
     *        <tt>defaultInstance</tt> is <tt>null</tt>
     */
    public static <T> T requireNonNullElse(final T instance, final T defaultInstance) {
        return Objects.nonNull(instance) ? instance : Objects.requireNonNull(defaultInstance, "defaultInstance is null");
    }
    
    /**
     * Returns the first argument if it is non-<tt>null</tt>,
     * otherwise the non-<tt>null</tt> second argument.
     *
     * @param <X> Type of the exception to be thrown
     * @param instance an object to test
     * @param exceptionSupplier The supplier which will be thrown if <em>instance</em> is {@code null}
     * @param <T> the type of the reference
     * @throws X if and only if <em>instance</em> is {@code null}
     * @return the first argument if it is non-<tt>null</tt> otherwise exception is thrown
     */
    public static <T, X extends Throwable> T requireNonNullOrThrow(final T instance, Supplier<? extends X> exceptionSupplier) throws X {
        if (instance == null) {
            throw exceptionSupplier.get();
        }
        return instance;
    }
}
