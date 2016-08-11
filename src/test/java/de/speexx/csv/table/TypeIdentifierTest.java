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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertAll;
import org.junit.jupiter.api.Test;


public class TypeIdentifierTest {
    
    @Test
    public void validDateStyle() {
        assertAll("valid date formats",
            () -> assertTrue(TypeIdentifier.isDateType("2016-08-02")),
            () -> assertTrue(TypeIdentifier.isDateType("2016-08-12")),
            () -> assertTrue(TypeIdentifier.isDateType("2016-08-22")),
            () -> assertTrue(TypeIdentifier.isDateType("2016-08-31")),
            () -> assertTrue(TypeIdentifier.isDateType("2016-11-02")),
            () -> assertTrue(TypeIdentifier.isDateType("2016-11-12")),
            () -> assertTrue(TypeIdentifier.isDateType("2016-11-22")),
            () -> assertTrue(TypeIdentifier.isDateType("2016-11-31")),
            () -> assertTrue(TypeIdentifier.isDateType("2016-8-5")),
            () -> assertTrue(TypeIdentifier.isDateType("2016-8-10")),
            () -> assertTrue(TypeIdentifier.isDateType("2016-8-21")),
            () -> assertTrue(TypeIdentifier.isDateType("2016-8-31")),
            () -> assertTrue(TypeIdentifier.isDateType("2016-1-01")),
            () -> assertTrue(TypeIdentifier.isDateType("2016-12-01")),

            () -> assertTrue(TypeIdentifier.isDateType("1.1.1970")),
            () -> assertTrue(TypeIdentifier.isDateType("1.01.1970")),
            () -> assertTrue(TypeIdentifier.isDateType("1.12.1970")),
            () -> assertTrue(TypeIdentifier.isDateType("31.12.2018")),
            () -> assertTrue(TypeIdentifier.isDateType("21.12.2018")),
            () -> assertTrue(TypeIdentifier.isDateType("11.12.2018")),
            () -> assertTrue(TypeIdentifier.isDateType("1.02.2018")),
            () -> assertTrue(TypeIdentifier.isDateType("01.2.2018")),
            () -> assertTrue(TypeIdentifier.isDateType("01.02.2016")),
            
            () -> assertTrue(TypeIdentifier.isDateType("1/1/1970")),
            () -> assertTrue(TypeIdentifier.isDateType("1/01/1970")),
            () -> assertTrue(TypeIdentifier.isDateType("1/12/1970")),
            () -> assertTrue(TypeIdentifier.isDateType("12/31/2018")),
            () -> assertTrue(TypeIdentifier.isDateType("12/21/2018")),
            () -> assertTrue(TypeIdentifier.isDateType("02/11/2018")),
            () -> assertTrue(TypeIdentifier.isDateType("01/2/2018")),
            () -> assertTrue(TypeIdentifier.isDateType("01/02/2016"))
        );        
    }

    @Test
    public void invalidDateStyle() {
        assertAll("invalid date formats",
            () -> assertFalse(TypeIdentifier.isDateType("2016-28-02")),
            () -> assertFalse(TypeIdentifier.isDateType("2016-18-42")),

            () -> assertFalse(TypeIdentifier.isDateType("42.12.2016")),
            () -> assertFalse(TypeIdentifier.isDateType("12.20.2016")),

            () -> assertFalse(TypeIdentifier.isDateType("2/42/2016")),
            () -> assertFalse(TypeIdentifier.isDateType("20/2/2016"))
        );
    }

    @Test
    public void validTimeStyle() {
        assertAll("valid time formats",
            () -> assertTrue(TypeIdentifier.isTimeType("01:01:01.12345")),
            () -> assertTrue(TypeIdentifier.isTimeType("11:01:02.12345")),
            () -> assertTrue(TypeIdentifier.isTimeType("21:01:03.12345")),
            () -> assertTrue(TypeIdentifier.isTimeType("01:11:04.12345")),
            () -> assertTrue(TypeIdentifier.isTimeType("01:21:05.12345")),
            () -> assertTrue(TypeIdentifier.isTimeType("01:31:06.12345")),
            () -> assertTrue(TypeIdentifier.isTimeType("01:41:07.12345")),
            () -> assertTrue(TypeIdentifier.isTimeType("01:59:08.12345")),
            () -> assertTrue(TypeIdentifier.isTimeType("01:11:19.12345")),
            () -> assertTrue(TypeIdentifier.isTimeType("01:22:20.12345")),
            () -> assertTrue(TypeIdentifier.isTimeType("01:34:31.12345")),
            () -> assertTrue(TypeIdentifier.isTimeType("01:44:41.12345")),
            () -> assertTrue(TypeIdentifier.isTimeType("01:59:59.12345")),
            () -> assertTrue(TypeIdentifier.isTimeType("00:00:00.00000")),

            () -> assertTrue(TypeIdentifier.isTimeType("1:1:1.12345")),
            () -> assertTrue(TypeIdentifier.isTimeType("2:1:1.12345")),
            () -> assertTrue(TypeIdentifier.isTimeType("3:1:1.12345")),
            () -> assertTrue(TypeIdentifier.isTimeType("4:1:1.12345")),
            () -> assertTrue(TypeIdentifier.isTimeType("5:1:1.12345")),
            () -> assertTrue(TypeIdentifier.isTimeType("6:1:1.12345")),
            () -> assertTrue(TypeIdentifier.isTimeType("7:1:1.12345")),
            () -> assertTrue(TypeIdentifier.isTimeType("8:1:1.12345")),
            () -> assertTrue(TypeIdentifier.isTimeType("9:1:1.12345")),
            () -> assertTrue(TypeIdentifier.isTimeType("0:0:0.0")),

            () -> assertTrue(TypeIdentifier.isTimeType("2:2:1.12345")),
            () -> assertTrue(TypeIdentifier.isTimeType("3:3:1.12345")),
            () -> assertTrue(TypeIdentifier.isTimeType("4:4:1.12345")),
            () -> assertTrue(TypeIdentifier.isTimeType("5:5:1.12345")),
            () -> assertTrue(TypeIdentifier.isTimeType("6:6:1.12345")),
            () -> assertTrue(TypeIdentifier.isTimeType("7:7:1.12345")),
            () -> assertTrue(TypeIdentifier.isTimeType("8:8:1.12345")),
            () -> assertTrue(TypeIdentifier.isTimeType("9:9:1.12345")),
            () -> assertTrue(TypeIdentifier.isTimeType("0:0:1.12345")),
            () -> assertTrue(TypeIdentifier.isTimeType("0:0:0.0")),

            () -> assertTrue(TypeIdentifier.isTimeType("2:2:2.12345")),
            () -> assertTrue(TypeIdentifier.isTimeType("3:3:3.12345")),
            () -> assertTrue(TypeIdentifier.isTimeType("4:4:4.12345")),
            () -> assertTrue(TypeIdentifier.isTimeType("5:5:5.12345")),
            () -> assertTrue(TypeIdentifier.isTimeType("6:6:6.12345")),
            () -> assertTrue(TypeIdentifier.isTimeType("7:7:7.12345")),
            () -> assertTrue(TypeIdentifier.isTimeType("8:8:8.12345")),
            () -> assertTrue(TypeIdentifier.isTimeType("9:9:9.12345")),
            () -> assertTrue(TypeIdentifier.isTimeType("0:0:0.12345")),
            () -> assertTrue(TypeIdentifier.isTimeType("0:0:0.0")),

            () -> assertTrue(TypeIdentifier.isTimeType("01:01:01")),
            () -> assertTrue(TypeIdentifier.isTimeType("11:01:02")),
            () -> assertTrue(TypeIdentifier.isTimeType("21:01:03")),
            () -> assertTrue(TypeIdentifier.isTimeType("01:11:04")),
            () -> assertTrue(TypeIdentifier.isTimeType("01:21:05")),
            () -> assertTrue(TypeIdentifier.isTimeType("01:31:06")),
            () -> assertTrue(TypeIdentifier.isTimeType("01:41:07")),
            () -> assertTrue(TypeIdentifier.isTimeType("01:59:08")),
            () -> assertTrue(TypeIdentifier.isTimeType("01:11:19")),
            () -> assertTrue(TypeIdentifier.isTimeType("01:22:20")),
            () -> assertTrue(TypeIdentifier.isTimeType("01:34:31")),
            () -> assertTrue(TypeIdentifier.isTimeType("01:44:41")),
            () -> assertTrue(TypeIdentifier.isTimeType("01:59:59")),
            () -> assertTrue(TypeIdentifier.isTimeType("00:00:00")),

            () -> assertTrue(TypeIdentifier.isTimeType("1:1:1")),
            () -> assertTrue(TypeIdentifier.isTimeType("2:1:1")),
            () -> assertTrue(TypeIdentifier.isTimeType("3:1:1")),
            () -> assertTrue(TypeIdentifier.isTimeType("4:1:1")),
            () -> assertTrue(TypeIdentifier.isTimeType("5:1:1")),
            () -> assertTrue(TypeIdentifier.isTimeType("6:1:1")),
            () -> assertTrue(TypeIdentifier.isTimeType("7:1:1")),
            () -> assertTrue(TypeIdentifier.isTimeType("8:1:1")),
            () -> assertTrue(TypeIdentifier.isTimeType("9:1:1")),
            () -> assertTrue(TypeIdentifier.isTimeType("0:0:0")),

            () -> assertTrue(TypeIdentifier.isTimeType("2:2:1")),
            () -> assertTrue(TypeIdentifier.isTimeType("3:3:1")),
            () -> assertTrue(TypeIdentifier.isTimeType("4:4:1")),
            () -> assertTrue(TypeIdentifier.isTimeType("5:5:1")),
            () -> assertTrue(TypeIdentifier.isTimeType("6:6:1")),
            () -> assertTrue(TypeIdentifier.isTimeType("7:7:1")),
            () -> assertTrue(TypeIdentifier.isTimeType("8:8:1")),
            () -> assertTrue(TypeIdentifier.isTimeType("9:9:1")),
            () -> assertTrue(TypeIdentifier.isTimeType("0:0:1")),

            () -> assertTrue(TypeIdentifier.isTimeType("2:2:2")),
            () -> assertTrue(TypeIdentifier.isTimeType("3:3:3")),
            () -> assertTrue(TypeIdentifier.isTimeType("4:4:4")),
            () -> assertTrue(TypeIdentifier.isTimeType("5:5:5")),
            () -> assertTrue(TypeIdentifier.isTimeType("6:6:6")),
            () -> assertTrue(TypeIdentifier.isTimeType("7:7:7")),
            () -> assertTrue(TypeIdentifier.isTimeType("8:8:8")),
            () -> assertTrue(TypeIdentifier.isTimeType("9:9:9")),
//
            () -> assertTrue(TypeIdentifier.isTimeType("01:01")),
            () -> assertTrue(TypeIdentifier.isTimeType("11:01")),
            () -> assertTrue(TypeIdentifier.isTimeType("21:01")),
            () -> assertTrue(TypeIdentifier.isTimeType("01:11")),
            () -> assertTrue(TypeIdentifier.isTimeType("01:21")),
            () -> assertTrue(TypeIdentifier.isTimeType("01:31")),
            () -> assertTrue(TypeIdentifier.isTimeType("01:41")),
            () -> assertTrue(TypeIdentifier.isTimeType("01:59")),
            () -> assertTrue(TypeIdentifier.isTimeType("01:11")),
            () -> assertTrue(TypeIdentifier.isTimeType("01:22")),
            () -> assertTrue(TypeIdentifier.isTimeType("01:34")),
            () -> assertTrue(TypeIdentifier.isTimeType("01:44")),
            () -> assertTrue(TypeIdentifier.isTimeType("01:59")),
            () -> assertTrue(TypeIdentifier.isTimeType("00:00")),

            () -> assertTrue(TypeIdentifier.isTimeType("1:1")),
            () -> assertTrue(TypeIdentifier.isTimeType("2:1")),
            () -> assertTrue(TypeIdentifier.isTimeType("3:1")),
            () -> assertTrue(TypeIdentifier.isTimeType("4:1")),
            () -> assertTrue(TypeIdentifier.isTimeType("5:1")),
            () -> assertTrue(TypeIdentifier.isTimeType("6:1")),
            () -> assertTrue(TypeIdentifier.isTimeType("7:1")),
            () -> assertTrue(TypeIdentifier.isTimeType("8:1")),
            () -> assertTrue(TypeIdentifier.isTimeType("9:1")),
            () -> assertTrue(TypeIdentifier.isTimeType("0:0")),

            () -> assertTrue(TypeIdentifier.isTimeType("2:2")),
            () -> assertTrue(TypeIdentifier.isTimeType("3:3")),
            () -> assertTrue(TypeIdentifier.isTimeType("4:4")),
            () -> assertTrue(TypeIdentifier.isTimeType("5:5")),
            () -> assertTrue(TypeIdentifier.isTimeType("6:6")),
            () -> assertTrue(TypeIdentifier.isTimeType("7:7")),
            () -> assertTrue(TypeIdentifier.isTimeType("8:8")),
            () -> assertTrue(TypeIdentifier.isTimeType("9:9")),

            () -> assertTrue(TypeIdentifier.isTimeType("3:3")),
            () -> assertTrue(TypeIdentifier.isTimeType("4:4")),
            () -> assertTrue(TypeIdentifier.isTimeType("5:5")),
            () -> assertTrue(TypeIdentifier.isTimeType("6:6")),
            () -> assertTrue(TypeIdentifier.isTimeType("7:7")),
            () -> assertTrue(TypeIdentifier.isTimeType("8:8")),
            () -> assertTrue(TypeIdentifier.isTimeType("9:9"))
        );
    }

    @Test
    public void invalidTimeStyle() {
        assertAll("invalid time formats",
            () -> assertFalse(TypeIdentifier.isTimeType("30:00:00")),
            () -> assertFalse(TypeIdentifier.isTimeType("20:60:00")),
            () -> assertFalse(TypeIdentifier.isTimeType("24:59:60")),

            () -> assertFalse(TypeIdentifier.isTimeType("30:00")),
            () -> assertFalse(TypeIdentifier.isTimeType("20:60"))
        );
    }

    @Test
    public void validDatetimeStyle() {
        assertAll("valid datetime formats",
            () -> assertTrue(TypeIdentifier.isDatetimeType("2016-1-1 1:1")),
            () -> assertTrue(TypeIdentifier.isDatetimeType("2016-01-08T1:1")),
            () -> assertTrue(TypeIdentifier.isDatetimeType("2016-12-11\t1:1")),

            () -> assertTrue(TypeIdentifier.isDatetimeType("31.12.2016 24:59:59")),
            () -> assertTrue(TypeIdentifier.isDatetimeType("01.1.2016T02:10")),
            () -> assertTrue(TypeIdentifier.isDatetimeType("12.12.2016\t01:01:12")),

            () -> assertTrue(TypeIdentifier.isDatetimeType("12/31/2016 0:0:0.0000")),
            () -> assertTrue(TypeIdentifier.isDatetimeType("1/01/2016T02:10")),
            () -> assertTrue(TypeIdentifier.isDatetimeType("12/31/2016\t01:01:12")),

            () -> assertTrue(TypeIdentifier.isDatetimeType("Tue, 3 Jun 2008 11:05:30 GMT"))
        );
    }

    @Test
    public void invalidDatetimeStyle() {
        assertAll("invalid datetime formats",
            () -> assertFalse(TypeIdentifier.isDatetimeType("2016-1-1B1:1"))
        );
    }

    @Test
    public void validIntegerStyle() {
        assertAll("valid integer formats",
            () -> assertTrue(TypeIdentifier.isIntegerType("0")),
            () -> assertTrue(TypeIdentifier.isIntegerType("-0")),
            () -> assertTrue(TypeIdentifier.isIntegerType("+0")),

            () -> assertTrue(TypeIdentifier.isIntegerType("1")),
            () -> assertTrue(TypeIdentifier.isIntegerType("-1")),
            () -> assertTrue(TypeIdentifier.isIntegerType("+1")),

            () -> assertTrue(TypeIdentifier.isIntegerType("9")),
            () -> assertTrue(TypeIdentifier.isIntegerType("-9")),
            () -> assertTrue(TypeIdentifier.isIntegerType("+9")),

            () -> assertTrue(TypeIdentifier.isIntegerType("90")),
            () -> assertTrue(TypeIdentifier.isIntegerType("10")),
            () -> assertTrue(TypeIdentifier.isIntegerType("-99")),
            () -> assertTrue(TypeIdentifier.isIntegerType("+99"))
        );
    }

    @Test
    public void invalidIntegerStyle() {
        assertAll("invalid integer formats",
            () -> assertFalse(TypeIdentifier.isIntegerType("01")),
            () -> assertFalse(TypeIdentifier.isIntegerType("-01")),
            () -> assertFalse(TypeIdentifier.isIntegerType("+01"))
        );
    }

    @Test
    public void validDecimalStyle() {
        assertAll("valid decimal formats",
            () -> assertTrue(TypeIdentifier.isDecimalType("0")),
            () -> assertTrue(TypeIdentifier.isDecimalType("-0")),
            () -> assertTrue(TypeIdentifier.isDecimalType("+0")),

            () -> assertTrue(TypeIdentifier.isDecimalType("1")),
            () -> assertTrue(TypeIdentifier.isDecimalType("-1")),
            () -> assertTrue(TypeIdentifier.isDecimalType("+1")),

            () -> assertTrue(TypeIdentifier.isDecimalType("9")),
            () -> assertTrue(TypeIdentifier.isDecimalType("-9")),
            () -> assertTrue(TypeIdentifier.isDecimalType("+9")),

            () -> assertTrue(TypeIdentifier.isDecimalType("90")),
            () -> assertTrue(TypeIdentifier.isDecimalType("10")),
            () -> assertTrue(TypeIdentifier.isDecimalType("-99")),
            () -> assertTrue(TypeIdentifier.isDecimalType("+99")),
            () -> assertTrue(TypeIdentifier.isDecimalType("01")),
            () -> assertTrue(TypeIdentifier.isDecimalType("-09")),
            () -> assertTrue(TypeIdentifier.isDecimalType("-00")),
            () -> assertTrue(TypeIdentifier.isDecimalType("+09")),

            () -> assertTrue(TypeIdentifier.isDecimalType("0.0")),
            () -> assertTrue(TypeIdentifier.isDecimalType("-0.19")),
            () -> assertTrue(TypeIdentifier.isDecimalType("+0.99")),

            () -> assertTrue(TypeIdentifier.isDecimalType("0.0e0")),
            () -> assertTrue(TypeIdentifier.isDecimalType("-0.19E1")),
            () -> assertTrue(TypeIdentifier.isDecimalType("+0.99e-101")),
            () -> assertTrue(TypeIdentifier.isDecimalType("+0.99E+999")),
            () -> assertTrue(TypeIdentifier.isDecimalType("0.0E0")),
            () -> assertTrue(TypeIdentifier.isDecimalType("-0.19e1")),
            () -> assertTrue(TypeIdentifier.isDecimalType("+0.99E-101")),
            () -> assertTrue(TypeIdentifier.isDecimalType("+0.99e+999")),

            () -> assertTrue(TypeIdentifier.isDecimalType("nan")),
            () -> assertTrue(TypeIdentifier.isDecimalType("NaN"))
        );
    }
}
