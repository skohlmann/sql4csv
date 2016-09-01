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
package de.speexx.csv.table.db.derby;

import java.sql.Date;
import java.time.temporal.IsoFields;

/**
 * Support class for additional functions in Derby DB.
 */
public class DateSupport {

    /**
     * Returns the numerical value of the day of the week. For Monday the value
     * is 1. For Sunday the value is 7.
     * <p>In case of a given {@code null} as parameter the return value is {@code null}.</p>
     * @param date the date to get the day of week for
     * @return the numerical value of the day of week staring with 1 for Monday.
     */
    public static Integer dayOfWeek(final Date date) {
        if (date == null) {
            return null;
        }
        return date.toLocalDate().getDayOfWeek().getValue();
    }

    /**
     * Returns the numerical value of the week of a year for the given date.
     * The week calculation based on the ISO 8601 rules.
     * <p>In case of a given {@code null} as parameter the return value is {@code null}.</p>
     * @param date the date to get the week of the year
     * @return the numerical value of the week of the year staring with 1 for first week.
     */
    public static Integer weekOfYear(final Date date) {
        if (date == null) {
            return null;
        }
        return date.toLocalDate().get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
    }
}
