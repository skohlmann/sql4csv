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
package de.speexx.csv.table;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

final class TypeIdentifier {
    
    private static final String LAZY_ISO_LOCAL_DATE_REGEX = "\\d{4}-[01]?\\d-[0-3]?\\d";
    private static final String LAZY_GERMAN_LIKE_LOCAL_DATE_REGEX = "[0-3]?\\d\\.[01]?\\d\\.\\d{4}";
    private static final String LAZY_US_LIKE_LOCAL_DATE_REGEX = "[01]?\\d\\/[0-3]?\\d/\\d{4}";

    private static final Pattern LAZY_DATE_PATTERN =
            Pattern.compile("^" + LAZY_ISO_LOCAL_DATE_REGEX
                                + "|" + LAZY_GERMAN_LIKE_LOCAL_DATE_REGEX
                                + "|" + LAZY_US_LIKE_LOCAL_DATE_REGEX);
    
    private static final String LAZY_ISO_LOCAL_TIME_WITH_MILLIS_REGEX = "[0-2]?\\d:[0-5]?\\d:[0-5]?\\d\\.\\d+";
    private static final String LAZY_ISO_LOCAL_TIME_WITH_NO_MILLIS_REGEX = "[0-2]?\\d:[0-5]?\\d:[0-5]?\\d";
    private static final String LAZY_ISO_LOCAL_TIME_WITH_NO_SECONDS_REGEX = "[0-2]?\\d:[0-5]?\\d";

    private static final Pattern LAZY_TIME_PATTERN =
            Pattern.compile("^" + LAZY_ISO_LOCAL_TIME_WITH_MILLIS_REGEX
                                + "|" + LAZY_ISO_LOCAL_TIME_WITH_NO_MILLIS_REGEX
                                + "|" + LAZY_ISO_LOCAL_TIME_WITH_NO_SECONDS_REGEX);

    private static final Pattern LAZY_DATETIME_PATTERN =
            Pattern.compile("^(" 
                                 + LAZY_ISO_LOCAL_DATE_REGEX
                                 + "|" + LAZY_GERMAN_LIKE_LOCAL_DATE_REGEX
                                 + "|" + LAZY_US_LIKE_LOCAL_DATE_REGEX
                            + ")[T\\s](" 
                                 + LAZY_ISO_LOCAL_TIME_WITH_MILLIS_REGEX
                                 + "|" + LAZY_ISO_LOCAL_TIME_WITH_NO_MILLIS_REGEX
                                 + "|" + LAZY_ISO_LOCAL_TIME_WITH_NO_SECONDS_REGEX
                            + ")");
    
    private static final Pattern INTEGER_PATTERN = Pattern.compile("^([-+]?0)|([-+]?[1-9]\\d*)");
    private static final Pattern DECIMAL_PATTERN = Pattern.compile("^([-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?)|([Nn][aA][Nn])");
    
    public static boolean isDateType(final String value) {
        return value != null ? LAZY_DATE_PATTERN.matcher(value).matches() : false;
    }

    public static boolean isTimeType(final String value) {
        return value != null ? LAZY_TIME_PATTERN.matcher(value).matches() : false;
    }

    public static boolean isDatetimeType(final String value) {
        if (value == null) {
            return false;
        }

        if (!LAZY_DATETIME_PATTERN.matcher(value).matches()) {
            try {
                DateTimeFormatter.RFC_1123_DATE_TIME.parse(value);
            } catch (final DateTimeParseException ex) {
                return false;
            }
        }
        return true;
    }
    
    public static boolean isIntegerType(final String value) {
        return value != null ? INTEGER_PATTERN.matcher(value).matches() : false;
    }
    
    public static boolean isDecimalType(final String value) {
        return value != null ? DECIMAL_PATTERN.matcher(value).matches() : false;
    }

    public static boolean isStringType(final String value) {
        return value != null;
    }
}
