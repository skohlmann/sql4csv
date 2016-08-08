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

import java.util.Iterator;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class OriginalReplacementMapTest {

    @Test
    public void lengthSortedOriginalIterator() {
        final DbTable.OriginalReplacementMap map = new DbTable.OriginalReplacementMap();
        map.addOriginal("12");
        map.addOriginal("1234");
        map.addOriginal("1");
        map.addOriginal("123");
        
        final Iterator<String> itr = map.originals();
        assertAll("original length", 
                () -> assertEquals("1234", itr.next()),
                () -> assertEquals("123", itr.next()),
                () -> assertEquals("12", itr.next()),
                () -> assertEquals("1", itr.next()));
    }
}
