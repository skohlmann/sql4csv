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

import de.speexx.csv.table.transformer.TypeTransformer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.speexx.csv.table.EntryDescriptor.Type.DATE;
import static de.speexx.csv.table.EntryDescriptor.Type.DATETIME;
import static de.speexx.csv.table.EntryDescriptor.Type.DECIMAL;
import static de.speexx.csv.table.EntryDescriptor.Type.INTEGER;
import static de.speexx.csv.table.EntryDescriptor.Type.TIME;
import static de.speexx.csv.table.EntryDescriptor.Type.STRING;
import java.sql.Statement;
import java.util.Objects;
import java.sql.PreparedStatement;
import java.util.concurrent.atomic.AtomicInteger;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static de.speexx.csv.table.EntryDescriptorSupport.cloneEntryDescriptorList;
import de.speexx.csv.table.transformer.UnsupportedTransformationException;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static de.speexx.csv.table.util.UuidSupport.shortUuid;
import java.util.Optional;

final class DbTable implements Table {
     
    private static final Logger LOG = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

    private static final String FROM_CLAUSE = "from";
    private static final String REPLACABLE = "xXx";
    private static final String DERBY_JDBC_URL_TEMPLATE = "jdbc:derby:memory:" + REPLACABLE + ";create=true";
    private static final String DERBY_JDBC_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
    private static final int MAX_VARCHAR = 32672;
    private final String tableName;
    private Connection connection;
    private String internalTableName;
    private final OriginalReplacementMap replacementMap = new OriginalReplacementMap();
    private List<? extends EntryDescriptor> descriptors;
    private String rowNumberColumnName;
    
    public DbTable(final String name) {
        this.tableName = name;
    }

    @Override
    public String getName() {
        return this.tableName;
    }
    
    void init(final RowReader reader) {
        Objects.requireNonNull(reader, "reader is null");
        this.descriptors = cloneEntryDescriptorList(Objects.requireNonNull(reader.getEntryDescriptors(),
                                                    "entry descriptor of row is null"));

        fillReplacementMap(this.descriptors);
        
        getJdbcDriverClass();
        final String jdbcUrl = getJdbcUrlTemplate().replace(REPLACABLE, getName());
        LOG.trace("JDBC URL: {}", jdbcUrl);
        
        try {
            this.connection = DriverManager.getConnection(jdbcUrl);
            createDbTable(this.connection, this.descriptors);
            final String insertStatementTemplate = createInsertDbTablePreparedStatement(this.descriptors);            
            final AtomicInteger rowNumber = new AtomicInteger();
            reader.forEach(row -> fillInDbTable(this.connection, insertStatementTemplate, row, rowNumber));
            
        } catch (final SQLException ex) {
            throw new TableException(ex);
        }
    }
    
    void fillReplacementMap(final List<? extends EntryDescriptor> descs) {
        Objects.requireNonNull(descs, "entry descriptors is null");
        
        LOG.debug("Entry Descriptors: " + descs);
        
        if (!descs.isEmpty()) {
            descs.forEach(entry -> this.replacementMap.addOriginal(entry.getName()));
        }
    }
    
    void fillInDbTable(final Connection conn, final String insertStmtTemplate, final Row row, final AtomicInteger rowNumber) {
        assert nonNull(conn);
        assert nonNull(insertStmtTemplate);
        assert nonNull(rowNumber);

        Objects.requireNonNull(row, "row is null");
        try (final PreparedStatement stmt = conn.prepareStatement(insertStmtTemplate)) {

            final AtomicInteger stmtIdx = new AtomicInteger();
            stmt.setInt(stmtIdx.incrementAndGet(), rowNumber.getAndIncrement());

            row.forEach(entry -> {
                try {
                    stmt.setString(stmtIdx.incrementAndGet(), String.valueOf(entry.getValue()));
                } catch (final SQLException e) {
                    throw new TableException(e);
                }
            });

            stmt.execute();
            
        } catch (final SQLException e) {
            throw new TableException(e);
        }
        
        try {
            conn.commit();
        } catch (final SQLException e) {
            throw new TableException(e);
        }
    }
    
    @Override
    public RowReader executeSql(final String sql) {
        assert this.connection != null;
        
        final AtomicReference<String> useSql = new AtomicReference<>(sql);
        this.replacementMap.originals().forEachRemaining(original -> {
            final String replacement = 
                    this.replacementMap.replacementForOriginal(original)
                            .orElseThrow(() -> new TableException("Unable to get internal columnName for '" + original + "'"));
            // This is currently pragmatic but not very stable.
            // Maybe must change to use JSQLParser also to handle this.
            useSql.set(useSql.get().replace(original, replacement));
        });
        
        final String uSql = useSql.get();
        // From change
        final Optional<String> fromPart = extractFromPartFromSelectSql(uSql);
        final String toExecuteSql = uSql.replace(fromPart.orElseThrow(() -> new TableException("No from part in query: " + uSql)), this.internalTableName);
        
        try {
            assert !this.connection.isClosed();

            try (final Statement stmt = this.connection.createStatement();
                 final ResultSet result = stmt.executeQuery(toExecuteSql)) {
                return new ResultSetBackedRowReader(result, this.rowNumberColumnName, this.replacementMap);
            }
        } catch (final Exception e) {
            throw new TableException(e);
        }
    }
    
    Optional<String> extractFromPartFromSelectSql(final String sql) {
        final String[] parts = sql.split(" ");
        boolean fromWasLast = false;
        for (final String part : parts) {
            if (fromWasLast) {
                return Optional.of(part);
            }
            if (part.equalsIgnoreCase(FROM_CLAUSE)) {
                fromWasLast = true;
            }
        }
        return Optional.empty();
    }
    
    String createInsertDbTablePreparedStatement(final List<? extends EntryDescriptor> descs) {
        assert descs != null;
        
        final StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ");
        sb.append(getInternalTableName());
        sb.append(" (");
        sb.append(getRowNumberColumnName());
        sb.append(", ");
        sb.append(descs.stream()
                .map(desc -> this.replacementMap.replacementForOriginal(desc.getName()).get())
                .collect(joining(", ")));
        sb.append(") VALUES (?, ");
        
        sb.append(descs.stream()
                .map(desc -> "?")
                .collect(joining(", ")));
        sb.append(")");
        final String stmt = sb.toString();
        LOG.debug("INSERT STMT: {}", stmt);
        return stmt;
        
    }
    
    String createInsertNamePart(final EntryDescriptor desc) {
        assert nonNull(desc);
        final String originalName = desc.getName();
        final String replacementName = this.replacementMap.replacementForOriginal(originalName)
            .orElseThrow(() -> new TableException("Unable to get internal columnName for '" + originalName + "'"));
        return replacementName;
    }
    
    void createDbTable(final Connection conn, final List<? extends EntryDescriptor> descs) {
        assert nonNull(conn);
        assert nonNull(descs);
        
        if (descs.isEmpty()) {
            return;
        }
        
        final String statementStr = createTableCreateStatement(descs);
        try {
            final Statement stmnt = conn.createStatement();
            stmnt.execute(statementStr);
        } catch (final SQLException e) {
            throw new TableException("unable to execute table create statement: " + statementStr, e);
        }
    }

    String createTableCreateStatement(final List<? extends EntryDescriptor> descs) {
        final StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE ");
        sb.append(getInternalTableName());
        sb.append(" (");
        sb.append(createRowNumberColumnPartForTableCreateStatement());
        sb.append(descs.stream()
                .map(desc -> createNameTypePart(desc))
                .collect(joining(", ")));
        sb.append(")");
        final String statementStr = sb.toString();
        LOG.debug("CREATE STMT: {}", statementStr);
        return statementStr;
    }

    String getInternalTableName() {
        if (this.internalTableName == null) {
            this.internalTableName = "t" + shortUuid();
        }
        return this.internalTableName;
    }
    
    String createRowNumberColumnPartForTableCreateStatement() {
        return getRowNumberColumnName() + " INT NOT NULL PRIMARY KEY, ";
    }

    String getRowNumberColumnName() {
        if (this.rowNumberColumnName == null) {
            this.rowNumberColumnName = "rnc" + shortUuid();
        }
        return this.rowNumberColumnName;
    }
    
    String createNameTypePart(final EntryDescriptor desc) {
        assert nonNull(desc);
        final String name = desc.getName();
        final String replacement = this.replacementMap.replacementForOriginal(name)
                .orElseThrow(() -> new TableException("Unable to get internal columnName for '" + name + "'"));

        final StringBuilder sb = new StringBuilder();
        sb.append(replacement);
        sb.append(" ");
        switch (desc.getType()) {
            case STRING: sb.append(desc.getType().getSqlTypeName()).append("(").append(MAX_VARCHAR).append(")"); break;
            case INTEGER:
            case DECIMAL:
            case DATE:
            case TIME:
            case DATETIME: sb.append(desc.getType().getSqlTypeName()); break;
            default: throw new TableException("unsupported type: " + desc.getType());
        }

        return sb.toString();
    }
    
    Class<?> getJdbcDriverClass() {
        try {
            final String jdbcDriver = DERBY_JDBC_DRIVER;
            LOG.trace("JDBC DRIVER: {}", jdbcDriver);
            return Class.forName(jdbcDriver);
        } catch (final ClassNotFoundException e) {
            throw new TableException(e);
        }
    }
    
    String getJdbcUrlTemplate() {
        return DERBY_JDBC_URL_TEMPLATE;
    }

    @Override
    public List<EntryDescriptor> getEntryDescriptors() {
        return Collections.unmodifiableList(this.descriptors);
    }
    
    static final class OriginalReplacementMap {
        
        private static final String PREFIX = "c";
        
        private final Map<String, String> originalToReplacement = new HashMap<>();
        private final Map<String, String> replacementToOriginal = new HashMap<>();
        
        void addOriginalAndReplacement(final String original, final String replacement) {
            Objects.requireNonNull(original, "orginal is null");
            Objects.requireNonNull(replacement, "replacement is null");
            if (this.originalToReplacement.containsKey(adjustCase(original))) {
                throw new DoubleKeyException(original, "Key duplicate: " + original);
            }
            if (this.replacementToOriginal.containsKey(adjustCase(replacement))) {
                throw new DoubleKeyException(original, "Replacement duplicate: " + replacement);
            }

            this.originalToReplacement.put(adjustCase(original), adjustCase(replacement));
            this.replacementToOriginal.put(adjustCase(replacement), adjustCase(original));
        }
        
        String adjustCase(final String s) {
            assert Objects.nonNull(s) : "value to adjust is null";
            return s.toLowerCase(Locale.ENGLISH);
        }

        public String addOriginal(final String original) {
            final String replacement = PREFIX + shortUuid();
            addOriginalAndReplacement(original, replacement);
            return replacement;
        }
        
        public Optional<String> replacementForOriginal(final String original) {
            final String lower = adjustCase(original);
            if (this.originalToReplacement.containsKey(lower)) {
                return Optional.of(this.originalToReplacement.get(lower));
            }
            return Optional.empty();
        }
        
        public Optional<String> originalForReplacement(final String replacement) {
            final String lower = adjustCase(replacement);
            if (this.replacementToOriginal.containsKey(lower)) {
                return Optional.of(this.replacementToOriginal.get(lower));
            }
            return Optional.empty();
        }
        
        public Iterator<String> originals() {
            return this.originalToReplacement.keySet().iterator();
        }
        public Iterator<String> replacements() {
            return this.replacementToOriginal.keySet().iterator();
        }

        @Override
        public String toString() {
            return "OriginalReplacementMap{" + "originalToReplacement=" + originalToReplacement + ", replacementToOriginal=" + replacementToOriginal + '}';
        }
    }
    
    @Override
    public void changeColumnType(final String columnName, final EntryDescriptor.Type newType) {
        Objects.requireNonNull(columnName, "columnName is null");
        Objects.requireNonNull(newType, "newType is null");

        final TypeTransformer transformer =
                detectTypeTransformerForColmn(columnName, newType)
                        .orElseThrow(() -> new TableException("Transformation of column '" + columnName
                                                                + "' to type '" + newType + "' not possible."));
        
        LOG.trace("Start changeColumnToType...");
        
        final String realColumnName = this.replacementMap.replacementForOriginal(columnName)
                .orElseThrow(() -> new TableException("Unknown column with name: " + columnName));

        final String intermediateTableName = "i_" + shortUuid();
        final EntryDescriptorSupport.TypeChangeableEntryDescriptor descriptor = 
                findEntryDescriptorForName(this.descriptors, columnName);
        if (Objects.isNull(descriptor)) {
            throw new TableException("Unknown descriptor for name: " + columnName);
        }
        
        try (final Statement alterStmt = this.connection.createStatement();
             final Statement dropStmt = this.connection.createStatement();
             final Statement renameStmt = this.connection.createStatement()) {

            final String alterTableStmtString = "ALTER TABLE " + this.internalTableName + " ADD COLUMN " + intermediateTableName + " " + newType.getSqlTypeName() + (newType == EntryDescriptor.Type.STRING ? "(" + MAX_VARCHAR + ")" : "");
            LOG.trace("ALTER TABLE STMT: " + alterTableStmtString);
            final String dropColumnStmtString = "ALTER TABLE " + this.internalTableName + " DROP COLUMN " + realColumnName;
            LOG.trace("       DROP STMT: " + dropColumnStmtString);
            final String renameColumnStmtString = "RENAME COLUMN " + this.internalTableName + "." + intermediateTableName + " TO " + realColumnName;
            LOG.trace("     RENAME STMT: " + renameColumnStmtString);

            alterStmt.executeUpdate(alterTableStmtString);
            //transformAndCopy(realColumnName, descriptor.getType(), intermediateTableName, newType, transformer);
            transformAndCopy(realColumnName, descriptor.getType(), intermediateTableName, newType, transformer);
            dropStmt.executeUpdate(dropColumnStmtString);
            renameStmt.executeUpdate(renameColumnStmtString);
            
            this.connection.commit();

        } catch (final SQLException e) {
            throw new TableException(e);
        }
        
        descriptor.setType(newType);
    }
    
    void transformAndCopy(final String fromColumn,
                          final EntryDescriptor.Type fromType,
                          final String toColumn,
                          final EntryDescriptor.Type toType,
                          final TypeTransformer transformer) {

        assert nonNull(fromColumn) : "fromColumn name is null";
        assert nonNull(toColumn) : "toColumn name is null";
        assert nonNull(transformer) : "transformer name is null";
        assert nonNull(fromType) : "fromType is null";
        assert nonNull(toType) : "toType is null";
 
        final String selectStmtString = "SELECT " + this.rowNumberColumnName + ", " + fromColumn + " FROM " + this.internalTableName;
        final String updateStmtString = "UPDATE " + this.internalTableName + " SET " + toColumn + " = (?) WHERE " + this.rowNumberColumnName + " = ?";

        LOG.trace("SELECT STMT: " + selectStmtString);
        LOG.trace("UPDATE STMT: " + updateStmtString);
        
        try (final PreparedStatement selectStmt = this.connection.prepareStatement(selectStmtString);
             final ResultSet result = selectStmt.executeQuery()) {
            while (result.next()) {
                final int row = result.getInt(1);
                Object to;
                switch (fromType) {
                    case DATE: {
                        to = transformer.transform(result.getDate(2)).get();
                        break;
                    }
                    case DATETIME: {
                        to = transformer.transform(result.getTimestamp(2)).get();
                        break;
                    }
                    case TIME: {
                        to = transformer.transform(result.getTime(2)).get();
                        break;
                    }
                    case DECIMAL: {
                        to = transformer.transform(result.getDouble(2)).get();
                        break;
                    }
                    case INTEGER: {
                        to = transformer.transform(result.getLong(2)).get();
                        break;
                    }
                    case STRING: {
                        to = transformer.transform(result.getString(2)).get();
                        break;
                    }
                    default:
                        throw new TransformationException("unsupported type: " + fromType);
                }
                try (final PreparedStatement insertStmt = this.connection.prepareStatement(updateStmtString)) {
                    switch (toType) {
                        case DATE: {
                            insertStmt.setDate(1, (Date) to);
                            break;
                        }
                        case DATETIME: {
                            insertStmt.setTimestamp(1, (Timestamp) to);
                            break;
                        }
                        case TIME: {
                            insertStmt.setTime(1, (Time) to);
                            break;
                        }
                        case DECIMAL: {
                            insertStmt.setDouble(1, (Double) to);
                            break;
                        }
                        case INTEGER: {
                            insertStmt.setLong(1, (Long) to);
                            break;
                        }
                        case STRING: {
                            insertStmt.setString(1, (String) to);
                            break;
                        }
                        default:
                            throw new TransformationException("unsupported type: " + fromType);
                    }
                    insertStmt.setInt(2, row);
                    insertStmt.executeUpdate();
                }
            }
        } catch (final SQLException e) {
            throw new TransformationException(e);
        }
    }
    
    static final EntryDescriptorSupport.TypeChangeableEntryDescriptor findEntryDescriptorForName(final List<? extends EntryDescriptor> descriptors,
                                                                                                 final String descriptorName) {
        Objects.requireNonNull(descriptors, "descriptors is null");
        Objects.requireNonNull(descriptorName, "descriptorName is null");
        for (final EntryDescriptor descriptor : descriptors) {
            if (descriptorName.equals(descriptor.getName())) {
                return (EntryDescriptorSupport.TypeChangeableEntryDescriptor) descriptor;
            }
        }

        return null;
    }


    Optional<EntryDescriptor.Type> getTypeForColumnName(final String columnName) {
        if (Objects.isNull(columnName)) {
            return Optional.empty();
        }
        
        final Optional<EntryDescriptor> descriptor = getEntryDescriptorForColumnName(columnName);

        if (descriptor.isPresent()) {
            return Optional.of(descriptor.get().getType());
        }
        return Optional.empty();
    }
    
    Optional<EntryDescriptor> getEntryDescriptorForColumnName(final String columnName) {
        
        if (Objects.isNull(columnName)) {
            return Optional.empty();
        }
        
        final List<EntryDescriptor> descs = this.getEntryDescriptors();
        assert Objects.nonNull(descs);

        for (final EntryDescriptor descriptor : descs) {
            final String name = descriptor.getName();
            if (columnName.equals(name)) {
                return Optional.of(descriptor);
            }
        }
        
        return Optional.empty();
    }
    
    Optional<TypeTransformer> detectTypeTransformerForColmn(final String columnName, final EntryDescriptor.Type newType) {
        final Optional<EntryDescriptor.Type> fromType = this.getTypeForColumnName(columnName);
        if (fromType.isPresent()) {
            try {
                return Optional.of(TypeTransformer.of(fromType.get(), newType));
            } catch (final UnsupportedTransformationException ex) {
                LOG.error(ex.getMessage());
            }
        }
        
        return Optional.empty();
    }

    @Override
    public String toString() {
        return "DbTable{" + "tableName=" + tableName + ", internalTableName=" + internalTableName + ", replacementMap=" + replacementMap + ", descriptors=" + descriptors + ", rowNumberColumnName=" + rowNumberColumnName + '}';
    }
    
}