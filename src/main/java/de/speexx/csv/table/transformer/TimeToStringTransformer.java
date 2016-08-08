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
import java.sql.Time;
import java.time.LocalTime;
import java.util.Optional;

class TimeToStringTransformer implements TypeTransformer<Object, String> {

    private static final LocalTimeToStringTransformer LOCAL_TIME_TRANSFORMER = new LocalTimeToStringTransformer();

    @Override
    public Optional<String> transform(final Object time) throws TransformationException {
        if (time == null) {
            return Optional.empty();
        }
        if (time instanceof LocalTime) {
            return LOCAL_TIME_TRANSFORMER.transform((LocalTime) time);
        } else if (time instanceof Time) {
            return LOCAL_TIME_TRANSFORMER.transform(((Time) time).toLocalTime());
        }
        throw new UnsupportedTransformationException("Unsupported time type: " + time.getClass());
    }
}
