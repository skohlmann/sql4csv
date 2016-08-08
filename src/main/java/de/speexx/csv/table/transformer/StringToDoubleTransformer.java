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
import java.util.Objects;
import java.util.Optional;

class StringToDoubleTransformer implements TypeTransformer<String, Double> {

    @Override
    public Optional<Double> transform(final String t) {
        if (Objects.isNull(t)) {
            return Optional.empty();
        }
        try {
            if (t.indexOf('.') == 2 && t.charAt(0) == '0') {
                throw new TransformationException("Leading zero for double not allowed: " + t);
            }
            return Optional.of(Double.parseDouble(t));
        } catch(final NumberFormatException e) {
            throw new TransformationException(e);
        }
    }
}
