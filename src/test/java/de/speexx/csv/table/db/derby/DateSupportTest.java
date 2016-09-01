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
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class DateSupportTest {
    
    @Test
    public void dayOfWeekForMonday() {
        assertEquals(new Integer(1), DateSupport.dayOfWeek(Date.valueOf(LocalDate.of(2016, 8, 29))));
    }

    @Test
    public void dayOfWeekForSunday() {
        assertEquals(new Integer(7), DateSupport.dayOfWeek(Date.valueOf(LocalDate.of(2016, 9, 4))));
    }

    @Test
    public void dayOfWeekForNull() {
        assertNull(DateSupport.dayOfWeek(null));
    }
    
    @Test
    public void weekOfYearForLastWeek2015() {
        assertEquals(new Integer(53), DateSupport.weekOfYear(Date.valueOf(LocalDate.of(2015, 12, 31))));
    }
    
    @Test
    public void weekOfYearForFirstWeek2011() {
        assertEquals(new Integer(1), DateSupport.weekOfYear(Date.valueOf(LocalDate.of(2011, 1, 9))));
    }

    @Test
    public void weekOfYearForNull() {
        assertNull(DateSupport.weekOfYear(null));
    }
}
