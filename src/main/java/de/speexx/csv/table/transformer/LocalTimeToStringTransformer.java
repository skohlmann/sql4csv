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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

class LocalTimeToStringTransformer implements TypeTransformer<LocalTime, String> {

    
    @Override
    public Optional<String> transform(final LocalTime time) throws TransformationException {
        if (time == null) {
            return Optional.empty();
        }
        return Optional.of(DateTimeFormatter.ISO_TIME.format(time));
    }
}
