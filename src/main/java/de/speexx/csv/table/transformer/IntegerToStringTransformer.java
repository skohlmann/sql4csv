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

import de.speexx.csv.table.TransformationException;
import java.math.BigInteger;
import java.util.Optional;


class IntegerToStringTransformer implements TypeTransformer<Number, String> {

    @Override
    public Optional<String> transform(final Number integer) throws TransformationException {
        if (integer == null) {
            return Optional.empty();
        }
        if (integer instanceof BigInteger) {
            return Optional.of(((BigInteger) integer).toString());
        }
        if (integer instanceof Integer || integer instanceof Long || integer instanceof Short || integer instanceof Byte) {
            return Optional.of(BigInteger.valueOf(integer.longValue()).toString());
        }
        throw new UnsupportedTransformationException("Unsupported integer type: " + integer.getClass());
    }
}
