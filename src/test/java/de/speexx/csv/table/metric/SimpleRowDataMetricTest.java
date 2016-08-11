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
package de.speexx.csv.table.metric;

import de.speexx.csv.table.Entry;
import de.speexx.csv.table.EntryDescriptor;
import de.speexx.csv.table.Row;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class SimpleRowDataMetricTest {

    private static final EntryDescriptor SIMPLE_DESCRIPTOR = new EntryDescriptor() {
        @Override public EntryDescriptor.Type getType() {return EntryDescriptor.Type.STRING;}
        @Override public String getName() {return "string";}
    };
    
    @Test
    public void integerColumnWithIntegerOnly() {
        // Prepare
        final List<Entry> entries = Arrays.asList(
            new Entry<String>() {
                @Override public EntryDescriptor getDescriptor() {return SIMPLE_DESCRIPTOR;}
                @Override public String getValue() {return "1";}
            },
            new Entry<String>() {
                @Override public EntryDescriptor getDescriptor() {return SIMPLE_DESCRIPTOR;}
                @Override public String getValue() {return "2";}
            }
        );
        final Row row = new Row() {
            @Override public int size() {return entries.size();}
            @Override public Iterator<Entry> iterator() {return entries.iterator();}
        };
        
        // Execute
        final SimpleRowDataMetric metric = new SimpleRowDataMetric();
        metric.collectRowData(row);
        
        // Check
        final Map<EntryDescriptor.Type, AtomicInteger> typeCount = metric.getTypeMetric().getNameTypeFrequence().get(SIMPLE_DESCRIPTOR.getName());
        assertEquals(2, typeCount.get(EntryDescriptor.Type.INTEGER).get());
        assertEquals(EntryDescriptor.Type.INTEGER, metric.getMostSignificantTypeForName(SIMPLE_DESCRIPTOR.getName()).get());

        assertNull(typeCount.get(EntryDescriptor.Type.STRING));
        assertNull(typeCount.get(EntryDescriptor.Type.DECIMAL));
        assertNull(typeCount.get(EntryDescriptor.Type.DATE));
        assertNull(typeCount.get(EntryDescriptor.Type.TIME));
        assertNull(typeCount.get(EntryDescriptor.Type.DATETIME));
    }

    @Test
    public void decimalColumnWithDecimalOnly() {
        // Prepare
        final List<Entry> entries = Arrays.asList(
            new Entry<String>() {
                @Override public EntryDescriptor getDescriptor() {return SIMPLE_DESCRIPTOR;}
                @Override public String getValue() {return "1.1";}
            },
            new Entry<String>() {
                @Override public EntryDescriptor getDescriptor() {return SIMPLE_DESCRIPTOR;}
                @Override public String getValue() {return "2.0";}
            }
        );
        final Row row = new Row() {
            @Override public int size() {return entries.size();}
            @Override public Iterator<Entry> iterator() {return entries.iterator();}
        };
        
        // Execute
        final SimpleRowDataMetric metric = new SimpleRowDataMetric();
        metric.collectRowData(row);
        
        // Check
        final Map<EntryDescriptor.Type, AtomicInteger> typeCount = metric.getTypeMetric().getNameTypeFrequence().get(SIMPLE_DESCRIPTOR.getName());
        assertEquals(2, typeCount.get(EntryDescriptor.Type.DECIMAL).get());
        assertEquals(EntryDescriptor.Type.DECIMAL, metric.getMostSignificantTypeForName(SIMPLE_DESCRIPTOR.getName()).get());

        assertNull(typeCount.get(EntryDescriptor.Type.STRING));
        assertNull(typeCount.get(EntryDescriptor.Type.INTEGER));
        assertNull(typeCount.get(EntryDescriptor.Type.DATE));
        assertNull(typeCount.get(EntryDescriptor.Type.TIME));
        assertNull(typeCount.get(EntryDescriptor.Type.DATETIME));
    }

    @Test
    public void dateColumnWithDateOnly() {
        // Prepare
        final List<Entry> entries = Arrays.asList(
            new Entry<String>() {
                @Override public EntryDescriptor getDescriptor() {return SIMPLE_DESCRIPTOR;}
                @Override public String getValue() {return "2016-1-1";}
            },
            new Entry<String>() {
                @Override public EntryDescriptor getDescriptor() {return SIMPLE_DESCRIPTOR;}
                @Override public String getValue() {return "12/31/2016";}
            }
        );
        final Row row = new Row() {
            @Override public int size() {return entries.size();}
            @Override public Iterator<Entry> iterator() {return entries.iterator();}
        };
        
        // Execute
        final SimpleRowDataMetric metric = new SimpleRowDataMetric();
        metric.collectRowData(row);
        
        // Check
        final Map<EntryDescriptor.Type, AtomicInteger> typeCount = metric.getTypeMetric().getNameTypeFrequence().get(SIMPLE_DESCRIPTOR.getName());
        assertEquals(2, typeCount.get(EntryDescriptor.Type.DATE).get());
        assertEquals(EntryDescriptor.Type.DATE, metric.getMostSignificantTypeForName(SIMPLE_DESCRIPTOR.getName()).get());

        assertNull(typeCount.get(EntryDescriptor.Type.STRING));
        assertNull(typeCount.get(EntryDescriptor.Type.INTEGER));
        assertNull(typeCount.get(EntryDescriptor.Type.DECIMAL));
        assertNull(typeCount.get(EntryDescriptor.Type.TIME));
        assertNull(typeCount.get(EntryDescriptor.Type.DATETIME));
    }

    @Test
    public void timeColumnWithTimeOnly() {
        // Prepare
        final List<Entry> entries = Arrays.asList(
            new Entry<String>() {
                @Override public EntryDescriptor getDescriptor() {return SIMPLE_DESCRIPTOR;}
                @Override public String getValue() {return "1:1";}
            },
            new Entry<String>() {
                @Override public EntryDescriptor getDescriptor() {return SIMPLE_DESCRIPTOR;}
                @Override public String getValue() {return "0:0:0.000";}
            }
        );
        final Row row = new Row() {
            @Override public int size() {return entries.size();}
            @Override public Iterator<Entry> iterator() {return entries.iterator();}
        };
        
        // Execute
        final SimpleRowDataMetric metric = new SimpleRowDataMetric();
        metric.collectRowData(row);
        
        // Check
        final Map<EntryDescriptor.Type, AtomicInteger> typeCount = metric.getTypeMetric().getNameTypeFrequence().get(SIMPLE_DESCRIPTOR.getName());
        assertEquals(2, typeCount.get(EntryDescriptor.Type.TIME).get());
        assertEquals(EntryDescriptor.Type.TIME, metric.getMostSignificantTypeForName(SIMPLE_DESCRIPTOR.getName()).get());

        assertNull(typeCount.get(EntryDescriptor.Type.STRING));
        assertNull(typeCount.get(EntryDescriptor.Type.INTEGER));
        assertNull(typeCount.get(EntryDescriptor.Type.DECIMAL));
        assertNull(typeCount.get(EntryDescriptor.Type.DATE));
        assertNull(typeCount.get(EntryDescriptor.Type.DATETIME));
    }

    @Test
    public void datetimeColumnWithDatetimeOnly() {
        // Prepare
        final List<Entry> entries = Arrays.asList(
            new Entry<String>() {
                @Override public EntryDescriptor getDescriptor() {return SIMPLE_DESCRIPTOR;}
                @Override public String getValue() {return "12/31/2015 1:1";}
            },
            new Entry<String>() {
                @Override public EntryDescriptor getDescriptor() {return SIMPLE_DESCRIPTOR;}
                @Override public String getValue() {return "1.1.1969T0:0:0.000";}
            }
        );
        final Row row = new Row() {
            @Override public int size() {return entries.size();}
            @Override public Iterator<Entry> iterator() {return entries.iterator();}
        };
        
        // Execute
        final SimpleRowDataMetric metric = new SimpleRowDataMetric();
        metric.collectRowData(row);
        
        // Check
        final Map<EntryDescriptor.Type, AtomicInteger> typeCount = metric.getTypeMetric().getNameTypeFrequence().get(SIMPLE_DESCRIPTOR.getName());
        assertEquals(2, typeCount.get(EntryDescriptor.Type.DATETIME).get());
        assertEquals(EntryDescriptor.Type.DATETIME, metric.getMostSignificantTypeForName(SIMPLE_DESCRIPTOR.getName()).get());

        assertNull(typeCount.get(EntryDescriptor.Type.STRING));
        assertNull(typeCount.get(EntryDescriptor.Type.INTEGER));
        assertNull(typeCount.get(EntryDescriptor.Type.DECIMAL));
        assertNull(typeCount.get(EntryDescriptor.Type.DATE));
        assertNull(typeCount.get(EntryDescriptor.Type.TIME));
    }

    @Test
    public void stringColumnWithStringOnly() {
        // Prepare
        final List<Entry> entries = Arrays.asList(
            new Entry<String>() {
                @Override public EntryDescriptor getDescriptor() {return SIMPLE_DESCRIPTOR;}
                @Override public String getValue() {return " ";}
            },
            new Entry<String>() {
                @Override public EntryDescriptor getDescriptor() {return SIMPLE_DESCRIPTOR;}
                @Override public String getValue() {return "";}
            },
            new Entry<String>() {
                @Override public EntryDescriptor getDescriptor() {return SIMPLE_DESCRIPTOR;}
                @Override public String getValue() {return "abc";}
            }
        );
        final Row row = new Row() {
            @Override public int size() {return entries.size();}
            @Override public Iterator<Entry> iterator() {return entries.iterator();}
        };
        
        // Execute
        final SimpleRowDataMetric metric = new SimpleRowDataMetric();
        metric.collectRowData(row);
        
        // Check
        final Map<EntryDescriptor.Type, AtomicInteger> typeCount = metric.getTypeMetric().getNameTypeFrequence().get(SIMPLE_DESCRIPTOR.getName());
        assertEquals(3, typeCount.get(EntryDescriptor.Type.STRING).get());
        assertEquals(EntryDescriptor.Type.STRING, metric.getMostSignificantTypeForName(SIMPLE_DESCRIPTOR.getName()).get());

        assertNull(typeCount.get(EntryDescriptor.Type.DATETIME));
        assertNull(typeCount.get(EntryDescriptor.Type.INTEGER));
        assertNull(typeCount.get(EntryDescriptor.Type.DECIMAL));
        assertNull(typeCount.get(EntryDescriptor.Type.DATE));
        assertNull(typeCount.get(EntryDescriptor.Type.TIME));
    }

    @Test
    public void allTypesColumn() {
        // Prepare
        final List<Entry> entries = Arrays.asList(
            new Entry<String>() {
                @Override public EntryDescriptor getDescriptor() {return SIMPLE_DESCRIPTOR;}
                @Override public String getValue() {return "1";}
            },
            new Entry<String>() {
                @Override public EntryDescriptor getDescriptor() {return SIMPLE_DESCRIPTOR;}
                @Override public String getValue() {return "";}
            },
            new Entry<String>() {
                @Override public EntryDescriptor getDescriptor() {return SIMPLE_DESCRIPTOR;}
                @Override public String getValue() {return "false";}
            },
            new Entry<String>() {
                @Override public EntryDescriptor getDescriptor() {return SIMPLE_DESCRIPTOR;}
                @Override public String getValue() {return " ";}
            },
            new Entry<String>() {
                @Override public EntryDescriptor getDescriptor() {return SIMPLE_DESCRIPTOR;}
                @Override public String getValue() {return "1:1";}
            },
            new Entry<String>() {
                @Override public EntryDescriptor getDescriptor() {return SIMPLE_DESCRIPTOR;}
                @Override public String getValue() {return "1.1";}
            },
            new Entry<String>() {
                @Override public EntryDescriptor getDescriptor() {return SIMPLE_DESCRIPTOR;}
                @Override public String getValue() {return "2016-08-14";}
            },
            new Entry<String>() {
                @Override public EntryDescriptor getDescriptor() {return SIMPLE_DESCRIPTOR;}
                @Override public String getValue() {return "2016-08-14T23:59:59.999";}
            }
        );
        final Row row = new Row() {
            @Override public int size() {return entries.size();}
            @Override public Iterator<Entry> iterator() {return entries.iterator();}
        };
        
        // Execute
        final SimpleRowDataMetric metric = new SimpleRowDataMetric();
        metric.collectRowData(row);
        
        // Check
        final Map<EntryDescriptor.Type, AtomicInteger> typeCount = metric.getTypeMetric().getNameTypeFrequence().get(SIMPLE_DESCRIPTOR.getName());
        assertEquals(1, typeCount.get(EntryDescriptor.Type.DECIMAL).get());
        assertEquals(1, typeCount.get(EntryDescriptor.Type.INTEGER).get());
        assertEquals(1, typeCount.get(EntryDescriptor.Type.DATE).get());
        assertEquals(1, typeCount.get(EntryDescriptor.Type.TIME).get());
        assertEquals(1, typeCount.get(EntryDescriptor.Type.DATETIME).get());
        assertEquals(3, typeCount.get(EntryDescriptor.Type.STRING).get());
        assertEquals(EntryDescriptor.Type.STRING, metric.getMostSignificantTypeForName(SIMPLE_DESCRIPTOR.getName()).get());
    }

    @Test
    public void integerColumnWithIntegerAndDecimal() {
        // Prepare
        final List<Entry> entries = Arrays.asList(
            new Entry<String>() {
                @Override public EntryDescriptor getDescriptor() {return SIMPLE_DESCRIPTOR;}
                @Override public String getValue() {return "1";}
            },
            new Entry<String>() {
                @Override public EntryDescriptor getDescriptor() {return SIMPLE_DESCRIPTOR;}
                @Override public String getValue() {return "2.0";}
            }
        );
        final Row row = new Row() {
            @Override public int size() {return entries.size();}
            @Override public Iterator<Entry> iterator() {return entries.iterator();}
        };
        
        // Execute
        final SimpleRowDataMetric metric = new SimpleRowDataMetric();
        metric.collectRowData(row);
        
        // Check
        final Map<EntryDescriptor.Type, AtomicInteger> typeCount = metric.getTypeMetric().getNameTypeFrequence().get(SIMPLE_DESCRIPTOR.getName());
        assertEquals(1, typeCount.get(EntryDescriptor.Type.DECIMAL).get());
        assertEquals(1, typeCount.get(EntryDescriptor.Type.INTEGER).get());
        assertEquals(EntryDescriptor.Type.DECIMAL, metric.getMostSignificantTypeForName(SIMPLE_DESCRIPTOR.getName()).get());

        assertNull(typeCount.get(EntryDescriptor.Type.STRING));
        assertNull(typeCount.get(EntryDescriptor.Type.DATE));
        assertNull(typeCount.get(EntryDescriptor.Type.TIME));
        assertNull(typeCount.get(EntryDescriptor.Type.DATETIME));
    }
}
