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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Describes a entry by {@linkplain #getName() name} and {@linkplain #getType() type}.
 */
public interface EntryDescriptor {
    
    final static Logger LOG = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

    /**
     * The type describes a type of an entry. The description of the type is
     * abstract but all types a backed by concrete Java classes.
     */
    public enum Type {
        /** The implementations backed this type by the Java type {@link Integer}. */
        INTEGER("integer",   "BIGINT",    Types.BIGINT,    TypeIdentifier::isIntegerType,  Integer.class, Long.class, BigInteger.class, Short.class, Byte.class),
        /** The implementations backed this type by the Java type {@link Double}. */
        DECIMAL("decimal",   "DOUBLE",    Types.DOUBLE,    TypeIdentifier::isDecimalType,  Double.class, Float.class, BigDecimal.class),
        /** A string is a string is a string. */
        STRING("string",     "VARCHAR",   Types.VARCHAR,   TypeIdentifier::isStringType,   String.class),
        /** The implementations backed this type by the Java type {@link LocalDate}. */
        DATE("date",         "DATE",      Types.DATE,      TypeIdentifier::isDateType,     Date.class, java.util.Date.class, LocalDate.class),
        /** The implementations backed this type by the Java type {@link LocalDateTime}. */
        DATETIME("datetime", "TIMESTAMP", Types.TIMESTAMP, TypeIdentifier::isDatetimeType, Timestamp.class, LocalDateTime.class),
        /** The implementations backed this type by the Java type {@link LocalTime}. */
        TIME("time",         "TIME",      Types.TIME,      TypeIdentifier::isTimeType,     Time.class, LocalTime.class);

        private static final Map<Class<?>, Type> TYPE_FOR_CLASS_LOOKUP = new HashMap<>();

        private final String typeName;
        private final String sqlTypeName;
        private final int sqlType;
        private final Set<Class<?>> supportedJavaClassesForType;
        private final Predicate<String> checker;

        Type(final String name, final String sqlTypeName, final int sqlType, final Predicate<String> checker, final Class<?>... supportedClasses) {
            this.typeName = name;
            this.sqlTypeName = sqlTypeName;
            this.sqlType = sqlType;
            this.checker = checker;
            this.supportedJavaClassesForType = new HashSet<>(Arrays.asList(supportedClasses));
        }

        /**
         * Get a type for a Java class.
         * @param clazz the class to get the type for
         * @return if it is not possible to detect a type for the given class
         *         the value of the Optional is {@code null}
         */
        public static Optional<Type> typeForClass(final Class<?> clazz) {
            if (Objects.isNull(clazz)) {
                return Optional.empty();
            }
            if (TYPE_FOR_CLASS_LOOKUP.isEmpty()) {
                Arrays.stream(values()).forEach(type -> {
                    type.supportedJavaClassesForType.forEach(cl -> TYPE_FOR_CLASS_LOOKUP.put(cl, type));
                });
            }
            return Optional.of(TYPE_FOR_CLASS_LOOKUP.get(clazz));
        }
        
        /** Checks for the given string to be corresponding to the given Type. 
         * @param value the string value to test
         * @return <tt>true</tt> if the given <em>value</em> is of this type. <code>false</code> otherwise.
         */
        public boolean isTypeMatch(final String value) {
            return this.checker.test(value);
        }

        /**
         * A string representation of the type.
         * @return the name of the type
         */
        public String getTypeName() {
            return this.typeName;
        }
        
        /**
         * The SQL name for the type.
         * @return the SQL type name
         */
        public String getSqlTypeName() {
            return this.sqlTypeName;
        }

        /**
         * Detects the type for a given {@linkplain Types sql type value}.
         * @param sqlType the {@linkplain Types sql type}
         * @return if and only if it is not possible to detect a type for the given
         *         {@linkplain Types sql type} the value of the Optional is {@code null}
         */
        public static Optional<Type> getTypeForSqlType(final int sqlType) {
            final Type[] types = values();
            for (final Type type : types) {
                if (type.sqlType == sqlType) {
                    return Optional.of(type);
                }
            }
            // For special database types:
            if (sqlType == Types.INTEGER) {
                return Optional.of(INTEGER);
            }
            return Optional.empty();
        }
    }

    /** The type of an entry.
     * @return never {@code null} */
    Type getType();

    /** The name of an entry.
     * @return never {@code null} */
    String getName();
}
