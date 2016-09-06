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

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;
import de.speexx.csv.table.app.sql.SelectData;

public class Configuration {
    
    @Parameter(names={"-n", "--no-header"}, description="If set no column name header is in the output.")
    private boolean withoutHeader = false;
    
    @Parameter(names={"-t", "--no-type"}, description="If set no automated type detection is performed.")
    private boolean withoutTypeDetections = false;

    @Parameter(names={"-v", "--verbose"}, description="Print out more information.")
    private boolean verbose = false;
    
    @Parameter(names={"-h", "--help"}, description="Prints a help reference.", help = true)
    private boolean help = false;
    
    @ParametersDelegate
    private final SelectData selectData = new SelectData();

    
    public SelectData getQueryData() {
        return this.selectData;
    }

    public boolean isWithoutHeader() {
        return this.withoutHeader;
    }

    public boolean isWithoutTypeDetections() {
        return this.withoutTypeDetections;
    }

    public boolean isVerbose() {
        return this.verbose;
    }

    public boolean isHelp() {
        return help;
    }
}
