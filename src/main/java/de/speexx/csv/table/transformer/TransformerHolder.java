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

final class TransformerHolder {
    
    public final static StringToDateTransformer STRING_TO_DATE = new StringToDateTransformer();
    public final static StringToDatetimeTransformer STRING_TO_DATETIME = new StringToDatetimeTransformer();
    public final static StringToTimeTransformer STRING_TO_TIME = new StringToTimeTransformer();
    public final static StringToDoubleTransformer STRING_TO_DOUBLE = new StringToDoubleTransformer();
    public final static StringToLongTransformer STRING_TO_LONG = new StringToLongTransformer();
    
    public final static DecimalToStringTransformer DECIMAL_TO_STRING = new DecimalToStringTransformer();
    public final static IntegerToStringTransformer INTEGER_TO_STRING = new IntegerToStringTransformer();
    public final static DateToStringTransformer DATE_TO_STRING = new DateToStringTransformer();
    public final static TimestampToStringTransformer TIMESTAMP_TO_STRING = new TimestampToStringTransformer();
    public final static TimeToStringTransformer TIME_TO_STRING = new TimeToStringTransformer();
    
    public final static DoNothingTransformer DO_NOTHING = new DoNothingTransformer();
}
