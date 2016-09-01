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
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

class TimestampToStringTransformer implements TypeTransformer<Object, String> {

    private static final LocalDateTimeToStringTransformer LOCAL_DATETIME_TRANSFORMER = new LocalDateTimeToStringTransformer();
    
    @Override
    public Optional<String> transform(final Object timestamp) throws TransformationException {
        if (timestamp == null) {
            return Optional.empty();
        }
        if (timestamp instanceof LocalDateTime) {
            return LOCAL_DATETIME_TRANSFORMER.transform((LocalDateTime) timestamp);
        } else if (timestamp instanceof Timestamp) {
            return LOCAL_DATETIME_TRANSFORMER.transform(((Timestamp) timestamp).toLocalDateTime());
        }
        throw new UnsupportedTransformationException("Unsupported timestamp type: " + timestamp.getClass());
    }
}
