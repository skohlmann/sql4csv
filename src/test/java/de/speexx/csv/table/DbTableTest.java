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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.concurrent.atomic.AtomicInteger;


public class DbTableTest {
    
    @Test
    public void loadSimpleTable() throws Exception {
        try (final InputStream in = DbTableTest.class.getClassLoader().getResourceAsStream("de/speexx/csv/table/simple.csv");
             final Reader reader = new InputStreamReader(in);
             final CsvReader csvReader = new CsvReader(reader)) {
            final DbTable table = new DbTable("test");
            table.init(csvReader);
            
            final RowReader rows = table.executeSql("select * from test");
         
            assertEquals(2, rows.getEntryDescriptors().size());
            assertAll("type", 
                    () -> assertEquals(EntryDescriptor.Type.STRING, rows.getEntryDescriptors().get(0).getType()),
                    () -> assertEquals(EntryDescriptor.Type.STRING, rows.getEntryDescriptors().get(1).getType()));
            assertAll("name", 
                    () -> assertEquals("data1", rows.getEntryDescriptors().get(0).getName()),
                    () -> assertEquals("data2", rows.getEntryDescriptors().get(1).getName()));
            
            final AtomicInteger rowCount = new AtomicInteger();
            rows.forEach(row -> {
                assertEquals(2, row.size());
                final AtomicInteger entryCount = new AtomicInteger();
                row.forEach(entry -> {
                    final EntryDescriptor desc = entry.getDescriptor();
                    assertEquals(EntryDescriptor.Type.STRING, desc.getType());
                    if (entryCount.get() == 0) {
                        assertEquals("data1", desc.getName());
                    } else {
                        assertEquals("data2", desc.getName());
                    }
                    if (entryCount.get() == 0 && rowCount.get() == 0) {
                        assertEquals("entry11", entry.getValue());
                    } else if (entryCount.get() == 1 && rowCount.get() == 0) {
                        assertEquals("entry12", entry.getValue());
                    } else if (entryCount.get() == 0 && rowCount.get() == 1) {
                        assertEquals("entry21", entry.getValue());
                    } else {
                        assertEquals("entry22", entry.getValue());
                    }
                    entryCount.incrementAndGet();
                });
                rowCount.incrementAndGet();
            });
        }
    }
    
    @Test
    public void changeColumnType() throws Exception {        
        try (final InputStream in = DbTableTest.class.getClassLoader().getResourceAsStream("de/speexx/csv/table/typechange.csv");
             final Reader reader = new InputStreamReader(in);
             final CsvReader csvReader = new CsvReader(reader)) {

            final DbTable table = new DbTable("test");
            table.init(csvReader);

            table.changeColumnType("sint", EntryDescriptor.Type.INTEGER);
            final List<EntryDescriptor> descriptors = table.getEntryDescriptors();
            final EntryDescriptor descriptor = DbTable.findEntryDescriptorForName(descriptors, "sint");

            assertEquals(EntryDescriptor.Type.INTEGER, descriptor.getType());
            
            final RowReader rows = table.executeSql("select sint from test");
            assertEquals(1, rows.getEntryDescriptors().size());
            rows.forEach(row -> row.forEach(entry -> assertEquals(123L, entry.getValue())));
        }
    }
}
