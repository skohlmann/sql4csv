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
package de.speexx.csv.table.app.sql;

import com.beust.jcommander.Parameter;
import de.speexx.csv.table.util.UuidSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static java.util.stream.Collectors.joining;


public final class SelectData implements SelectQuery {

    private static final Logger LOG = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    
    private static final String FROM_CLAUSE = "from";

    @Parameter(description = "Query", required = true)
    private final List<String> queryParts = new ArrayList<>();

    public SelectQueryData getQueryData() {

        final AtomicBoolean fromFound = new AtomicBoolean(false);
        final AtomicBoolean fromWasLast = new AtomicBoolean(false);
        final AtomicReference<String> originalFrom = new AtomicReference<>(null);
        final String newTableName = "t" + UuidSupport.shortUuid();
        
        final List<String> newQuery = this.queryParts.stream().map(qp -> {
            if (fromFound.get() && fromWasLast.get()) {
                originalFrom.set(qp);
                fromWasLast.set(false);
                return newTableName;
            }
            if (FROM_CLAUSE.equalsIgnoreCase(qp)) {
                fromFound.set(true);
                fromWasLast.set(true);
            }
            return qp;
        }).collect(Collectors.toList());

        //LOG.info("ORIGINAL QUERY: {}", SelectData.this.getQuery());
        //LOG.info("ADJUSTED QUERY: {}", stringListToString(newQuery));

        return new SelectQueryData() {
            
            @Override
            public SelectQuery getOriginalQuery() {
                return SelectQuery.of(SelectData.this.getQuery());
            }

            @Override
            public SelectQuery getAdjustedQuery() {
                return SelectQuery.of(stringListToString(newQuery));
            }

            @Override
            public List<FromInfo> getFromInfo() {
                return Collections.unmodifiableList(Arrays.asList(new FromInfo() {
                    @Override
                    public String getOriginalFrom() {
                        return originalFrom.get();
                    }

                    @Override
                    public String getAdjustedFrom() {
                        return newTableName;
                    }
                    @Override
                    public String toString() {
                        return new StringBuilder("FromInfo{OriginalFrom=")
                                .append(getOriginalFrom())
                                .append(" - AdjustedFrom=")
                                .append(getAdjustedFrom())
                                .append('}')
                                .toString();
                    }
                }));
            }
            
            @Override
            public String toString() {
                return new StringBuilder("SelectQueryData{AdjustedQuery=")
                        .append(getAdjustedQuery())
                        .append(" - OriginalQuery=")
                        .append(getOriginalQuery())
                        .append(" - FromInfo=")
                        .append(getFromInfo())
                        .append('}')
                        .toString();
            }
        };
        
    }

    @Override
    public String toString() {
        return "SelectData{" + "queryParts=" + queryParts + '}';
    }

    @Override
    public String getQuery() {
        return stringListToString(this.queryParts);
    }
    
    String stringListToString(final List<String> list) {
        assert Objects.nonNull(list) : "list is null";
        return list.stream().collect(joining(" "));
    }
    
    /* for testing only */
    void setQueryParts(final String... parts) {
        Arrays.stream(parts).forEach(part -> this.queryParts.add(part));
    }
}
