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
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class DateAndTimeSupportTest {
    
    @Test
    public void dayOfWeekForMonday4Date() {
        assertEquals(new Integer(1), DateAndTimeSupport.dayOfWeekForDate(Date.valueOf(LocalDate.of(2016, 8, 29))));
    }

    @Test
    public void dayOfWeekForSunday4Date() {
        assertEquals(new Integer(7), DateAndTimeSupport.dayOfWeekForDate(Date.valueOf(LocalDate.of(2016, 9, 4))));
    }

    @Test
    public void dayOfWeekForNull4Date() {
        assertNull(DateAndTimeSupport.dayOfWeekForDate((Date) null));
    }
    
    @Test
    public void weekOfYearForLastWeek2015ForDate() {
        assertEquals(new Integer(53), DateAndTimeSupport.weekOfYearForDate(Date.valueOf(LocalDate.of(2015, 12, 31))));
    }
    
    @Test
    public void weekOfYearForFirstWeek2011ForDate() {
        assertEquals(new Integer(1), DateAndTimeSupport.weekOfYearForDate(Date.valueOf(LocalDate.of(2011, 1, 9))));
    }

    @Test
    public void weekOfYearForNull4Date() {
        assertNull(DateAndTimeSupport.weekOfYearForDate((Date) null));
    }

    @Test
    public void dayOfWeekForMonday4Timestamp() {
        assertEquals(new Integer(1), DateAndTimeSupport.dayOfWeekForTimestamp(Timestamp.valueOf(LocalDateTime.of(2016, 8, 29, 1, 2, 3))));
    }

    @Test
    public void dayOfWeekForSunday4Timestamp() {
        assertEquals(new Integer(7), DateAndTimeSupport.dayOfWeekForTimestamp(Timestamp.valueOf(LocalDateTime.of(2016, 9, 4, 1, 2, 3))));
    }

    @Test
    public void dayOfWeekForNull4Timestamp() {
        assertNull(DateAndTimeSupport.dayOfWeekForTimestamp((Timestamp) null));
    }
    
    @Test
    public void weekOfYearForLastWeek2015ForTimestamp() {
        assertEquals(new Integer(53), DateAndTimeSupport.weekOfYearForTimestamp(Timestamp.valueOf(LocalDateTime.of(2015, 12, 31, 1, 2, 3))));
    }
    
    @Test
    public void weekOfYearForFirstWeek2011ForTimestamp() {
        assertEquals(new Integer(1), DateAndTimeSupport.weekOfYearForTimestamp(Timestamp.valueOf(LocalDateTime.of(2011, 1, 9, 1, 2, 3))));
    }

    @Test
    public void weekOfYearForNull4Timestamp() {
        assertNull(DateAndTimeSupport.weekOfYearForTimestamp((Timestamp) null));
    }
}
