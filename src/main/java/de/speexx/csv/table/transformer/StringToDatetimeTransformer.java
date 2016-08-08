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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;


class StringToDatetimeTransformer implements TypeTransformer<String, Timestamp> {

    static final DateTimeFormatter LAZY_SPACE_DELIMITED_ISO_LOCAL_DATETIME = new DateTimeFormatterBuilder()
                .append(StringToDateTransformer.LAZY_ISO_LOCAL_DATE)
                .appendLiteral(' ')
                .append(StringToTimeTransformer.LAZY_ISO_LOCAL_TIME)
                .toFormatter();

    static final DateTimeFormatter LAZY_T_DELIMITED_ISO_LOCAL_DATETIME = new DateTimeFormatterBuilder()
                .append(StringToDateTransformer.LAZY_ISO_LOCAL_DATE)
                .appendLiteral('T')
                .append(StringToTimeTransformer.LAZY_ISO_LOCAL_TIME)
                .parseCaseInsensitive()
                .toFormatter();

    static final DateTimeFormatter LAZY_HYPHEN_DELIMITED_ISO_LOCAL_DATETIME = new DateTimeFormatterBuilder()
                .append(StringToDateTransformer.LAZY_ISO_LOCAL_DATE)
                .appendLiteral('-')
                .append(StringToTimeTransformer.LAZY_ISO_LOCAL_TIME)
                .toFormatter();

    static final DateTimeFormatter LAZY_SPACE_DELIMITED_ISO_LOCAL_DATE_WITH_DOT_DELIMITED_TIME = new DateTimeFormatterBuilder()
                .append(StringToDateTransformer.LAZY_ISO_LOCAL_DATE)
                .appendLiteral(' ')
                .append(StringToTimeTransformer.LAZY_DOT_LOCAL_TIME)
                .toFormatter();

    static final DateTimeFormatter LAZY_T_DELIMITED_ISO_LOCAL_DATE_WITH_DOT_DELIMITED_TIME = new DateTimeFormatterBuilder()
                .append(StringToDateTransformer.LAZY_ISO_LOCAL_DATE)
                .appendLiteral('T')
                .append(StringToTimeTransformer.LAZY_DOT_LOCAL_TIME)
                .parseCaseInsensitive()
                .toFormatter();

    static final DateTimeFormatter LAZY_HYPHEN_DELIMITED_ISO_LOCAL_DATE_WITH_DOT_DELIMITED_TIME = new DateTimeFormatterBuilder()
                .append(StringToDateTransformer.LAZY_ISO_LOCAL_DATE)
                .appendLiteral('-')
                .append(StringToTimeTransformer.LAZY_DOT_LOCAL_TIME)
                .toFormatter();

    //---
    
    static final DateTimeFormatter LAZY_SPACE_DELIMITED_US_LIKE_LOCAL_DATETIME = new DateTimeFormatterBuilder()
                .append(StringToDateTransformer.LAZY_ISO_LOCAL_DATE)
                .appendLiteral(' ')
                .append(StringToTimeTransformer.LAZY_ISO_LOCAL_TIME)
                .toFormatter();

    static final DateTimeFormatter LAZY_T_DELIMITED_US_LIKE_LOCAL_DATETIME = new DateTimeFormatterBuilder()
                .append(StringToDateTransformer.LAZY_US_LIKE_LOCAL_DATE)
                .appendLiteral('T')
                .append(StringToTimeTransformer.LAZY_ISO_LOCAL_TIME)
                .parseCaseInsensitive()
                .toFormatter();

    static final DateTimeFormatter LAZY_HYPHEN_DELIMITED_US_LIKE_LOCAL_DATETIME = new DateTimeFormatterBuilder()
                .append(StringToDateTransformer.LAZY_US_LIKE_LOCAL_DATE)
                .appendLiteral('-')
                .append(StringToTimeTransformer.LAZY_ISO_LOCAL_TIME)
                .toFormatter();

    static final DateTimeFormatter LAZY_SPACE_DELIMITED_US_LIKE_LOCAL_DATE_WITH_DOT_DELIMITED_TIME = new DateTimeFormatterBuilder()
                .append(StringToDateTransformer.LAZY_US_LIKE_LOCAL_DATE)
                .appendLiteral(' ')
                .append(StringToTimeTransformer.LAZY_DOT_LOCAL_TIME)
                .toFormatter();

    static final DateTimeFormatter LAZY_T_DELIMITED_US_LIKE_LOCAL_DATE_WITH_DOT_DELIMITED_TIME = new DateTimeFormatterBuilder()
                .append(StringToDateTransformer.LAZY_US_LIKE_LOCAL_DATE)
                .appendLiteral('T')
                .append(StringToTimeTransformer.LAZY_DOT_LOCAL_TIME)
                .parseCaseInsensitive()
                .toFormatter();

    static final DateTimeFormatter LAZY_HYPHEN_DELIMITED_US_LIKE_LOCAL_DATE_WITH_DOT_DELIMITED_TIME = new DateTimeFormatterBuilder()
                .append(StringToDateTransformer.LAZY_US_LIKE_LOCAL_DATE)
                .appendLiteral('-')
                .append(StringToTimeTransformer.LAZY_DOT_LOCAL_TIME)
                .toFormatter();

    //---
    
    static final DateTimeFormatter LAZY_SPACE_DELIMITED_GERMAN_LIKE_LOCAL_DATETIME = new DateTimeFormatterBuilder()
                .append(StringToDateTransformer.LAZY_GERMAN_LIKE_LOCAL_DATE)
                .appendLiteral(' ')
                .append(StringToTimeTransformer.LAZY_ISO_LOCAL_TIME)
                .toFormatter();

    static final DateTimeFormatter LAZY_T_DELIMITED_GERMAN_LIKE_LOCAL_DATETIME = new DateTimeFormatterBuilder()
                .append(StringToDateTransformer.LAZY_GERMAN_LIKE_LOCAL_DATE)
                .appendLiteral('T')
                .append(StringToTimeTransformer.LAZY_ISO_LOCAL_TIME)
                .parseCaseInsensitive()
                .toFormatter();

    static final DateTimeFormatter LAZY_HYPHEN_DELIMITED_GERMAN_LIKE_LOCAL_DATETIME = new DateTimeFormatterBuilder()
                .append(StringToDateTransformer.LAZY_GERMAN_LIKE_LOCAL_DATE)
                .appendLiteral('-')
                .append(StringToTimeTransformer.LAZY_ISO_LOCAL_TIME)
                .toFormatter();

    static final DateTimeFormatter LAZY_SPACE_DELIMITED_GERMAN_LIKE_LOCAL_DATE_WITH_DOT_DELIMITED_TIME = new DateTimeFormatterBuilder()
                .append(StringToDateTransformer.LAZY_GERMAN_LIKE_LOCAL_DATE)
                .appendLiteral(' ')
                .append(StringToTimeTransformer.LAZY_DOT_LOCAL_TIME)
                .toFormatter();

    static final DateTimeFormatter LAZY_T_DELIMITED_GERMAN_LIKE_LOCAL_DATE_WITH_DOT_DELIMITED_TIME = new DateTimeFormatterBuilder()
                .append(StringToDateTransformer.LAZY_GERMAN_LIKE_LOCAL_DATE)
                .appendLiteral('T')
                .append(StringToTimeTransformer.LAZY_DOT_LOCAL_TIME)
                .parseCaseInsensitive()
                .toFormatter();

    static final DateTimeFormatter LAZY_HYPHEN_DELIMITED_GERMAN_LIKE_LOCAL_DATE_WITH_DOT_DELIMITED_TIME = new DateTimeFormatterBuilder()
                .append(StringToDateTransformer.LAZY_GERMAN_LIKE_LOCAL_DATE)
                .appendLiteral('-')
                .append(StringToTimeTransformer.LAZY_DOT_LOCAL_TIME)
                .toFormatter();

    private final Collection<DateTimeFormatter> formatter;

    public StringToDatetimeTransformer() {
        this(Arrays.asList(DateTimeFormatter.ISO_LOCAL_DATE_TIME,
                DateTimeFormatter.ISO_INSTANT,
                
                LAZY_SPACE_DELIMITED_ISO_LOCAL_DATETIME,
                LAZY_T_DELIMITED_ISO_LOCAL_DATETIME,
                LAZY_HYPHEN_DELIMITED_ISO_LOCAL_DATETIME,
                LAZY_SPACE_DELIMITED_ISO_LOCAL_DATE_WITH_DOT_DELIMITED_TIME,
                LAZY_T_DELIMITED_ISO_LOCAL_DATE_WITH_DOT_DELIMITED_TIME,
                LAZY_HYPHEN_DELIMITED_ISO_LOCAL_DATE_WITH_DOT_DELIMITED_TIME,
                
                LAZY_SPACE_DELIMITED_US_LIKE_LOCAL_DATETIME,
                LAZY_T_DELIMITED_US_LIKE_LOCAL_DATETIME,
                LAZY_HYPHEN_DELIMITED_US_LIKE_LOCAL_DATETIME,
                LAZY_SPACE_DELIMITED_US_LIKE_LOCAL_DATE_WITH_DOT_DELIMITED_TIME,
                LAZY_T_DELIMITED_US_LIKE_LOCAL_DATE_WITH_DOT_DELIMITED_TIME,
                LAZY_HYPHEN_DELIMITED_US_LIKE_LOCAL_DATE_WITH_DOT_DELIMITED_TIME,
                
                LAZY_SPACE_DELIMITED_GERMAN_LIKE_LOCAL_DATETIME,
                LAZY_T_DELIMITED_GERMAN_LIKE_LOCAL_DATETIME,
                LAZY_HYPHEN_DELIMITED_GERMAN_LIKE_LOCAL_DATETIME,
                LAZY_SPACE_DELIMITED_GERMAN_LIKE_LOCAL_DATE_WITH_DOT_DELIMITED_TIME,
                LAZY_T_DELIMITED_GERMAN_LIKE_LOCAL_DATE_WITH_DOT_DELIMITED_TIME,
                LAZY_HYPHEN_DELIMITED_GERMAN_LIKE_LOCAL_DATE_WITH_DOT_DELIMITED_TIME,
                
                DateTimeFormatter.RFC_1123_DATE_TIME
            )
        );
    }

    public StringToDatetimeTransformer(final Collection<DateTimeFormatter> formatter) {
        this.formatter = Objects.requireNonNull(formatter, "formatter is null");
    }

    @Override
    public Optional<Timestamp> transform(final String s) throws TransformationException {
        if (Objects.isNull(s)) {
            return Optional.empty();
        }
        assert this.formatter != null;
        Exception ex = null;
        for (final DateTimeFormatter fmt : this.formatter) {
            try {
                final LocalDateTime date = LocalDateTime.parse(s, fmt);
                return Optional.of(Timestamp.valueOf(date));
            } catch (final Exception e) {
                ex = e;
            }
        }
        if (Objects.nonNull(ex)) {
            throw new TransformationException("Unable to parse datetime string '" + s + "'", ex);
        }
        return Optional.empty();
    }
}
