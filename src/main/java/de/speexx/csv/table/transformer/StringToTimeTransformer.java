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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

class StringToTimeTransformer implements TypeTransformer<String, Time> {
    
    static final DateTimeFormatter LAZY_ISO_LOCAL_TIME = new DateTimeFormatterBuilder()
                .appendValue(HOUR_OF_DAY, 1, 2, SignStyle.NORMAL)
                .appendLiteral(':')
                .appendValue(MINUTE_OF_HOUR, 1, 2, SignStyle.NORMAL)
                .optionalStart()
                .appendLiteral(':')
                .appendValue(SECOND_OF_MINUTE, 1, 2, SignStyle.NORMAL)
                .toFormatter();

    static final DateTimeFormatter LAZY_DOT_LOCAL_TIME = new DateTimeFormatterBuilder()
                .appendValue(HOUR_OF_DAY, 1, 2, SignStyle.NORMAL)
                .appendLiteral('.')
                .appendValue(MINUTE_OF_HOUR, 1, 2, SignStyle.NORMAL)
                .optionalStart()
                .appendLiteral('.')
                .appendValue(SECOND_OF_MINUTE, 1, 2, SignStyle.NORMAL)
                .toFormatter();
    
    private final Collection<DateTimeFormatter> formatter;
    
    public StringToTimeTransformer() {
        this(Arrays.asList(DateTimeFormatter.ISO_LOCAL_DATE, LAZY_ISO_LOCAL_TIME, LAZY_DOT_LOCAL_TIME));
    }

    public StringToTimeTransformer(final Collection<DateTimeFormatter> formatter) {
        this.formatter = Objects.requireNonNull(formatter, "formatter is null");
    }

    @Override
    public Optional<Time> transform(final String s) throws TransformationException {
        if (Objects.isNull(s)) {
            return Optional.empty();
        }
        assert this.formatter != null;
        Exception ex = null;
        for (final DateTimeFormatter fmt : this.formatter) {
            try {
                final LocalTime time = LocalTime.parse(s, fmt);
                return Optional.of(Time.valueOf(time));
            } catch (final Exception e) {
                ex = e;
            }
        }
        if (Objects.nonNull(ex)) {
            throw new TransformationException("Unable to parse time string '" + s + "'", ex);
        }
        return Optional.empty();
    }
}
