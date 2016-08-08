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
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.expectThrows;
import org.junit.jupiter.api.Test;

public class StringToDateTransformerTest {
    
    @Test
    public void testLazyWithCorrectIsoDate() {
        final Date date = new StringToDateTransformer().transform("2016-07-09").get();
        final LocalDate local = date.toLocalDate();

        assertAll("iso date", 
                () -> assertEquals(2016, local.getYear()),
                () -> assertEquals(7, local.getMonthValue()),
                () -> assertEquals(9, local.getDayOfMonth()));
    }
    
    @Test
    public void testLazyWithInCorrectIsoDate() {
        final Date date = new StringToDateTransformer().transform("2016-7-9").get();
        final LocalDate local = date.toLocalDate();

        assertAll("iso like date", 
                () -> assertEquals(2016, local.getYear()),
                () -> assertEquals(7, local.getMonthValue()),
                () -> assertEquals(9, local.getDayOfMonth()));
    }
    
    @Test
    public void testLazyWithMixedCorrectIsoDate1() {
        final Date date = new StringToDateTransformer().transform("2016-07-9").get();
        final LocalDate local = date.toLocalDate();

        assertAll("iso like date", 
                () -> assertEquals(2016, local.getYear()),
                () -> assertEquals(7, local.getMonthValue()),
                () -> assertEquals(9, local.getDayOfMonth()));
    }
    
    @Test
    public void testLazyWithMixedCorrectIsoDate2() {
        final Date date = new StringToDateTransformer().transform("2016-7-09").get();
        final LocalDate local = date.toLocalDate();

        assertAll("iso like date", 
                () -> assertEquals(2016, local.getYear()),
                () -> assertEquals(7, local.getMonthValue()),
                () -> assertEquals(9, local.getDayOfMonth()));
    }
    
    @Test
    public void testLazyGermanLikeDotDateLong2() {
        final Date date = new StringToDateTransformer().transform("05.12.1974").get();
        final LocalDate local = date.toLocalDate();

        assertAll("german like date", 
                () -> assertEquals(1974, local.getYear()),
                () -> assertEquals(12, local.getMonthValue()),
                () -> assertEquals(5, local.getDayOfMonth()));
    }
    
    
    @Test
    public void testLazyGermanLikeDotDateLong1() {
        final Date date = new StringToDateTransformer().transform("15.05.1969").get();
        final LocalDate local = date.toLocalDate();

        assertAll("german like date", 
                () -> assertEquals(1969, local.getYear()),
                () -> assertEquals(5, local.getMonthValue()),
                () -> assertEquals(15, local.getDayOfMonth()));
    }
    
    @Test
    public void testLazyGermanLikeDotDateShort2() {
        final Date date = new StringToDateTransformer().transform("5.12.1974").get();
        final LocalDate local = date.toLocalDate();

        assertAll("german like date", 
                () -> assertEquals(1974, local.getYear()),
                () -> assertEquals(12, local.getMonthValue()),
                () -> assertEquals(5, local.getDayOfMonth()));
    }
    
    
    @Test
    public void testLazyGermanLikeDotDateShort1() {
        final Date date = new StringToDateTransformer().transform("15.5.1969").get();
        final LocalDate local = date.toLocalDate();

        assertAll("german like date", 
                () -> assertEquals(1969, local.getYear()),
                () -> assertEquals(5, local.getMonthValue()),
                () -> assertEquals(15, local.getDayOfMonth()));
    }
    
    @Test
    public void testLazyUsLikeDotDateLong2() {
        final Date date = new StringToDateTransformer().transform("07/25/2008").get();
        final LocalDate local = date.toLocalDate();

        assertAll("us like date", 
                () -> assertEquals(2008, local.getYear()),
                () -> assertEquals(7, local.getMonthValue()),
                () -> assertEquals(25, local.getDayOfMonth()));
    }
    
    @Test
    public void testLazyUsLikeDotDateLong1() {
        final Date date = new StringToDateTransformer().transform("12/06/1991").get();
        final LocalDate local = date.toLocalDate();

        assertAll("us like date", 
                () -> assertEquals(1991, local.getYear()),
                () -> assertEquals(12, local.getMonthValue()),
                () -> assertEquals(6, local.getDayOfMonth()));
    }
    
    @Test
    public void testLazyUsLikeDotDateShort2() {
        final Date date = new StringToDateTransformer().transform("7/25/2008").get();
        final LocalDate local = date.toLocalDate();

        assertAll("us like date", 
                () -> assertEquals(2008, local.getYear()),
                () -> assertEquals(7, local.getMonthValue()),
                () -> assertEquals(25, local.getDayOfMonth()));
    }
    
    @Test
    public void testLazyUsLikeDotDateShort1() {
        final Date date = new StringToDateTransformer().transform("12/6/1991").get();
        final LocalDate local = date.toLocalDate();

        assertAll("us like date", 
                () -> assertEquals(1991, local.getYear()),
                () -> assertEquals(12, local.getMonthValue()),
                () -> assertEquals(6, local.getDayOfMonth()));
    }
    
    @Test
    public void testLazyIllegalIsoDate() {
        final Throwable exception = expectThrows(TransformationException.class, () -> {
            new StringToDateTransformer().transform("ABCD-7-09");
        });
        assertEquals("Unable to parse date string 'ABCD-7-09'", exception.getMessage());
    }
}
