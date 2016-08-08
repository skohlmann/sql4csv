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
package de.speexx.csv.table.app;

import com.beust.jcommander.JCommander;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class ConfigurationTest {

    @Test
    public void simpleQuotedQuery() {
        final Configuration conf = new Configuration();
        final JCommander jc = new JCommander(conf);
        
        jc.parse("Select", "*", "from", "'dummy host'");
        assertEquals("Select * from 'dummy host'", conf.getQueryData().getQueryData().getOriginalQuery().getQuery());
    }
}
