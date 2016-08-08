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

import de.speexx.csv.table.EntryDescriptor;
import static de.speexx.csv.table.EntryDescriptor.Type.STRING;
import static de.speexx.csv.table.EntryDescriptor.Type.DATE;
import static de.speexx.csv.table.EntryDescriptor.Type.DATETIME;
import static de.speexx.csv.table.EntryDescriptor.Type.TIME;
import static de.speexx.csv.table.EntryDescriptor.Type.DECIMAL;
import static de.speexx.csv.table.EntryDescriptor.Type.INTEGER;
import de.speexx.csv.table.TransformationException;
import java.util.Objects;
import java.util.Optional;

/**
 * Transforms a given source value a type <tt>S</tt> into a value of type
 * <tt>T</tt>.
 * @param <S> source value and type.
 * @param <T> target value and type.
 */
public interface TypeTransformer<S, T> {
    
    /**
     * Transforms a given value of type <tt>S</tt> into a value of type <tt>T</tt>.
     * @param s the value to transform.
     * @return the transformed value of {@code T}
     * @throws TransformationException if it is unable to transform the input
     *                                 value to the output value.
     */
    Optional<T> transform(S s) throws TransformationException;
    
    /**
     * Factory method to get a type transformer for a decicated type transformation.
     * @param <S> the source type
     * @param <T> the target type
     * @param fromType the source type description
     * @param targetType the target type description
     * @return a transformater to transform a <em>fromType</em> to a <em>targetType</em>
     * @throws UnsupportedTransformationException if the transformation is not supported
     * @throws NullPointerException if and only if <em>fromType</em> or <em>targetType</em> are {@code null}
     */
    public static <S, T> TypeTransformer of(final EntryDescriptor.Type fromType, EntryDescriptor.Type targetType) {
        Objects.requireNonNull(fromType, "fromType is null");
        Objects.requireNonNull(targetType, "targetType is null");
        
        if (fromType == targetType) {
            return new DoNothingTransformer();
        }
        
        switch (fromType) {
            case STRING: {
                switch (targetType) {
                    case STRING: return TransformerHolder.DO_NOTHING;     
                    case DATE: return TransformerHolder.STRING_TO_DATE;
                    case DATETIME: return TransformerHolder.STRING_TO_DATETIME;
                    case TIME: return TransformerHolder.STRING_TO_TIME;
                    case DECIMAL: return TransformerHolder.STRING_TO_DOUBLE;
                    case INTEGER: return TransformerHolder.STRING_TO_LONG;                   
                }
            } case DECIMAL: {
                switch (targetType) {
                    case STRING: return TransformerHolder.DECIMAL_TO_STRING;
                }
            } case INTEGER: {
                switch (targetType) {
                    case STRING: return TransformerHolder.INTEGER_TO_STRING;
                }
            } case DATE: {
                switch (targetType) {
                    case STRING: return TransformerHolder.DATE_TO_STRING;
                }
            } case DATETIME: {
                switch (targetType) {
                    case STRING: return TransformerHolder.TIMESTAMP_TO_STRING;
                }
            } case TIME: {
                switch (targetType) {
                    case STRING: return TransformerHolder.TIME_TO_STRING;
                }
            }
        }
        throw new UnsupportedTransformationException("We are sorry, the transformation from type '" + fromType + "' to type '" + targetType + "' not supported yet.");
    }
}
