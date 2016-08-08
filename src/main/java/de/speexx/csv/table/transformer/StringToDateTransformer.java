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
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Collection;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.YEAR;

import java.time.format.SignStyle;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;


class StringToDateTransformer implements TypeTransformer<String, Date> {

    static final DateTimeFormatter LAZY_ISO_LOCAL_DATE = new DateTimeFormatterBuilder()
                .appendValue(YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
                .appendLiteral('-')
                .appendValue(MONTH_OF_YEAR, 1, 2, SignStyle.NORMAL)
                .appendLiteral('-')
                .appendValue(DAY_OF_MONTH, 1, 2, SignStyle.NORMAL)
                .toFormatter();

    static final DateTimeFormatter LAZY_GERMAN_LIKE_LOCAL_DATE = new DateTimeFormatterBuilder()
                .appendValue(DAY_OF_MONTH, 1, 2, SignStyle.NORMAL)
                .appendLiteral('.')
                .appendValue(MONTH_OF_YEAR, 1, 2, SignStyle.NORMAL)
                .appendLiteral('.')
                .appendValue(YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
                .toFormatter();
    
    static final DateTimeFormatter LAZY_US_LIKE_LOCAL_DATE = new DateTimeFormatterBuilder()
                .appendValue(MONTH_OF_YEAR, 1, 2, SignStyle.NORMAL)
                .appendLiteral('/')
                .appendValue(DAY_OF_MONTH, 1, 2, SignStyle.NORMAL)
                .appendLiteral('/')
                .appendValue(YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
                .toFormatter();
    
    private final Collection<DateTimeFormatter> formatter;
    
    public StringToDateTransformer() {
        this(Arrays.asList(DateTimeFormatter.ISO_LOCAL_DATE, LAZY_ISO_LOCAL_DATE, LAZY_GERMAN_LIKE_LOCAL_DATE, LAZY_US_LIKE_LOCAL_DATE));
    }

    public StringToDateTransformer(final Collection<DateTimeFormatter> formatter) {
        this.formatter = Objects.requireNonNull(formatter, "formatter is null");
    }

    @Override
    public Optional<Date> transform(final String s) throws TransformationException {
        if (Objects.isNull(s)) {
            return Optional.empty();
        }
        assert this.formatter != null;
        Exception ex = null;
        for (final DateTimeFormatter fmt : this.formatter) {
            try {
                final LocalDate date = LocalDate.parse(s, fmt);
                return Optional.of(Date.valueOf(date));
            } catch (final Exception e) {
                ex = e;
            }
        }
        if (Objects.nonNull(ex)) {
            throw new TransformationException("Unable to parse date string '" + s + "'", ex);
        }
        return Optional.empty();
    }
}
