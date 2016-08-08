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

import java.sql.Time;
import java.time.LocalTime;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;


public class StringToTimeTransformerTest {
    
    @Test
    public void testCorrectIsoTimeLong() {
        final Time time = new StringToTimeTransformer().transform("10:11:12").get();
        final LocalTime local = time.toLocalTime();

        assertAll("iso time", 
                () -> assertEquals(10, local.getHour()),
                () -> assertEquals(11, local.getMinute()),
                () -> assertEquals(12, local.getSecond()));
    }
    
    @Test
    public void testCorrectIsoTimeShort() {
        final Time time = new StringToTimeTransformer().transform("10:11").get();
        final LocalTime local = time.toLocalTime();

        assertAll("iso time", 
                () -> assertEquals(10, local.getHour()),
                () -> assertEquals(11, local.getMinute()),
                () -> assertEquals(0, local.getSecond()));
    }
    
    @Test
    public void testLazyIsoTimeLong() {
        final Time time = new StringToTimeTransformer().transform("1:2:3").get();
        final LocalTime local = time.toLocalTime();

        assertAll("lazy time", 
                () -> assertEquals(1, local.getHour()),
                () -> assertEquals(2, local.getMinute()),
                () -> assertEquals(3, local.getSecond()));
    }
    
    @Test
    public void testLazyIsoTimeShort() {
        final Time time = new StringToTimeTransformer().transform("8:9").get();
        final LocalTime local = time.toLocalTime();

        assertAll("lazy time", 
                () -> assertEquals(8, local.getHour()),
                () -> assertEquals(9, local.getMinute()),
                () -> assertEquals(0, local.getSecond()));
    }

    @Test
    public void testLazyDotTimeLong() {
        final Time time = new StringToTimeTransformer().transform("4.5.6").get();
        final LocalTime local = time.toLocalTime();

        assertAll("lazy time", 
                () -> assertEquals(4, local.getHour()),
                () -> assertEquals(5, local.getMinute()),
                () -> assertEquals(6, local.getSecond()));
    }

    @Test
    public void testLazyDotTimeShort() {
        final Time time = new StringToTimeTransformer().transform("4.5").get();
        final LocalTime local = time.toLocalTime();

        assertAll("lazy time", 
                () -> assertEquals(4, local.getHour()),
                () -> assertEquals(5, local.getMinute()),
                () -> assertEquals(0, local.getSecond()));
    }

    @Test
    public void testLazyDotTimeLongLeadingZero() {
        final Time time = new StringToTimeTransformer().transform("04.05.06").get();
        final LocalTime local = time.toLocalTime();

        assertAll("lazy time", 
                () -> assertEquals(4, local.getHour()),
                () -> assertEquals(5, local.getMinute()),
                () -> assertEquals(6, local.getSecond()));
    }

    @Test
    public void testLazyDotTimeShortLeadingZero() {
        final Time time = new StringToTimeTransformer().transform("04.05").get();
        final LocalTime local = time.toLocalTime();

        assertAll("lazy time", 
                () -> assertEquals(4, local.getHour()),
                () -> assertEquals(5, local.getMinute()),
                () -> assertEquals(0, local.getSecond()));
    }

    @Test
    public void testLazyDotTimeLong2() {
        final Time time = new StringToTimeTransformer().transform("14.05.16").get();
        final LocalTime local = time.toLocalTime();

        assertAll("lazy time", 
                () -> assertEquals(14, local.getHour()),
                () -> assertEquals(5, local.getMinute()),
                () -> assertEquals(16, local.getSecond()));
    }
}
