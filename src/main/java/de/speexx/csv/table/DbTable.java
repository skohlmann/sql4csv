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
import static java.util.Objects.nonNull;
import java.util.ArrayList;
import java.util.Optional;
import static de.speexx.csv.table.EntryDescriptorBuilder.of;
import static java.util.stream.Collectors.joining;
import static de.speexx.csv.table.util.UuidSupport.shortUuid;

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
        LOG.debug("JDBC URL: {}", jdbcUrl);
        
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
    
    void checkDescriptors(final EntryDescriptor... descriptors) {
        Objects.requireNonNull(descriptors, "descriptors is null");
        assert Objects.nonNull(descriptors);

        for (final EntryDescriptor descriptor : descriptors) {
            Objects.nonNull(descriptor);
            Objects.nonNull(descriptor.getName());
            Objects.nonNull(descriptor.getType());
        }
    }
        
    @Override
    public void changeColumnTypes(final EntryDescriptor... descriptors) {
        if (descriptors == null || descriptors.length == 0) {
            return;
        }
        checkDescriptors(descriptors);
        final List<ChangeColumnTypeData> changeDatas = new ArrayList<>();

        for (final EntryDescriptor desc : descriptors) {
            final String columnName = desc.getName();
            final EntryDescriptor.Type newType = desc.getType();
            final TypeTransformer transformer =
                    detectTypeTransformerForColmn(columnName, newType)
                            .orElseThrow(() -> new TableException("Transformation of column '" + columnName
                                                                    + "' to type '" + newType + "' not possible."));

            LOG.debug("Start changeColumnToType...");

            final String realColumnName = this.replacementMap.replacementForOriginal(columnName)
                    .orElseThrow(() -> new TableException("Unknown column with name: " + columnName));

            final String intermediateColumnName = "i_" + shortUuid();
            final EntryDescriptorSupport.TypeChangeableEntryDescriptor descriptor = 
                    findEntryDescriptorForName(this.descriptors, columnName);
            if (Objects.isNull(descriptor)) {
                throw new TableException("Unknown descriptor for name: " + columnName);
            }

            final String alterTableStmtString = "ALTER TABLE " + this.internalTableName + " ADD COLUMN " + intermediateColumnName + " " + newType.getSqlTypeName() + (newType == EntryDescriptor.Type.STRING ? "(" + MAX_VARCHAR + ")" : "");
            LOG.debug("ALTER TABLE STMT: " + alterTableStmtString);
            final String dropColumnStmtString = "ALTER TABLE " + this.internalTableName + " DROP COLUMN " + realColumnName;
            LOG.debug("       DROP STMT: " + dropColumnStmtString);
            final String renameColumnStmtString = "RENAME COLUMN " + this.internalTableName + "." + intermediateColumnName + " TO " + realColumnName;
            LOG.debug("     RENAME STMT: " + renameColumnStmtString);

            final ChangeColumnTypeData changeData = new ChangeColumnTypeData();
            changeData.setDropColumnStatement(dropColumnStmtString);
            changeData.setNewColumnStatement(alterTableStmtString);
            changeData.setRenameColumnStatement(renameColumnStmtString);
            changeData.setToDescriptor(of().addName(intermediateColumnName).addType(newType).build());
            changeData.setFromDescriptor(of().addName(realColumnName).addType(descriptor.getType()).build());
            changeData.setSourceDescriptor(desc);
            changeData.setTransformer(transformer);
            changeDatas.add(changeData);

        }

        createNewColumns(this.connection, changeDatas);
        transformAndCopy(this.connection, changeDatas);
        dropOldColumnAndRenameIntermediateColumn(this.connection, changeDatas);
        updateDescriptors(changeDatas);

        try {
            this.connection.commit();
        } catch (final SQLException ex) {
            throw new TableException(ex);
        }

    }

    void updateDescriptors(final List<ChangeColumnTypeData> changeDatas) {
        assert Objects.nonNull(changeDatas);
    
        for (final ChangeColumnTypeData changeData : changeDatas) {
            final EntryDescriptor sourceDescriptor = changeData.getSourceDescriptor();
            final String sourceColumnName = sourceDescriptor.getName();
            final EntryDescriptorSupport.TypeChangeableEntryDescriptor newSourceDescriptor = findEntryDescriptorForName(this.descriptors, sourceColumnName);
            newSourceDescriptor.setType(changeData.getToDescriptor().getType());
        }
    }
    
    void transformAndCopy(final Connection con, final List<ChangeColumnTypeData> changeDatas) {
        assert Objects.nonNull(con);
        assert Objects.nonNull(changeDatas);

        final String selectStmtString = createSelectStatement(changeDatas);
        final String updateStmtString = createUpdateStatement(changeDatas);
        LOG.debug("SELECT Stmt: {}", selectStmtString);
        LOG.debug("UPDATE Stmt: {}", updateStmtString);
        try (final PreparedStatement selectStmt = this.connection.prepareStatement(selectStmtString);
             final ResultSet result = selectStmt.executeQuery()) {
            while (result.next()) {
                final int row = result.getInt(this.rowNumberColumnName);

                try (final PreparedStatement updateStmt = this.connection.prepareStatement(updateStmtString)) {
                    final AtomicInteger statementIndex = new AtomicInteger();
                    changeDatas.forEach(changeData -> {
                        final String fromColumn = changeData.getFromDescriptor().getName();
                        try {
                            final Object fromValue = result.getObject(fromColumn);
                            if (fromValue != null) {
                                final Optional<Object> toValueOpt = changeData.getTransformer().transform(fromValue);
                                if (toValueOpt.isPresent()) {
                                    final Object toValue = toValueOpt.get();
                                    if (!(toValue instanceof Double) || !(((Double) toValue).isInfinite() || ((Double) toValue).isNaN())) {
                                        updateStmt.setObject(statementIndex.incrementAndGet(), toValue, changeData.getToDescriptor().getType().getSqlType());
                                    } else {
                                        updateStmt.setNull(statementIndex.incrementAndGet(), changeData.getToDescriptor().getType().getSqlType());
                                    }
                                } else {
                                    updateStmt.setNull(statementIndex.incrementAndGet(), changeData.getToDescriptor().getType().getSqlType());
                                }
                            } else {
                                updateStmt.setNull(statementIndex.incrementAndGet(), changeData.getToDescriptor().getType().getSqlType());
                            }
                        } catch (final SQLException e) {
                            throw new TransformationException(e);
                        }
                    });
                    updateStmt.setInt(statementIndex.incrementAndGet(), row);
                    updateStmt.executeUpdate();
                } catch (final SQLException e) {
                    throw new TransformationException(e);
                }
           }
        } catch (final SQLException e) {
            throw new TransformationException(e);
        }
    }

    String createSelectStatement(final List<ChangeColumnTypeData> changeDatas) {
        assert Objects.nonNull(changeDatas);
//        final String selectStmtString = "SELECT " + this.rowNumberColumnName + ", " + fromColumn + " FROM " + this.internalTableName;

        final StringBuilder selectBuilder = new StringBuilder("SELECT ");
        selectBuilder.append(changeDatas.stream().map(data -> data.getFromDescriptor().getName()).collect(joining(", ")));
        selectBuilder.append(", ");
        selectBuilder.append(this.rowNumberColumnName);
        selectBuilder.append(" FROM ");
        selectBuilder.append(this.internalTableName);

        return selectBuilder.toString();
    }

    String createUpdateStatement(final List<ChangeColumnTypeData> changeDatas) {
        assert Objects.nonNull(changeDatas);
//        final String updateStmtString = "UPDATE " + this.internalTableName + " SET " + toColumn + " = (?) WHERE " + this.rowNumberColumnName + " = ?";

        final StringBuilder selectBuilder = new StringBuilder("UPDATE ");
        selectBuilder.append(this.internalTableName);
        selectBuilder.append(" SET ");
        selectBuilder.append(changeDatas.stream().map(data -> data.getToDescriptor().getName()).collect(joining(" = ?, ")));
        selectBuilder.append(" = ? WHERE ");
        selectBuilder.append(this.rowNumberColumnName);
        selectBuilder.append(" = ?");

        return selectBuilder.toString();
    }

    void createNewColumns(final Connection con, final List<ChangeColumnTypeData> changeDatas) {
        assert Objects.nonNull(con);
        assert Objects.nonNull(changeDatas);

        changeDatas.forEach(changeData -> {
            LOG.debug("Create new Column for {}", changeData);
            try (final Statement alterStmt = this.connection.createStatement()) {
                alterStmt.executeUpdate(changeData.getNewColumnStatement());
            } catch (final SQLException e) {
                throw new TableException(e);
            }
        });
        try {
            this.connection.commit();
        } catch (final SQLException e) {
            throw new TableException(e);
        }
    }
    
    void dropOldColumnAndRenameIntermediateColumn(final Connection con, final List<ChangeColumnTypeData> changeDatas) {
        assert Objects.nonNull(con);
        assert Objects.nonNull(changeDatas);

        changeDatas.forEach(changeData -> {
            try (final Statement dropStmt = this.connection.createStatement();
                 final Statement renameStmt = this.connection.createStatement()) {
                LOG.debug("Drop old Column for {}", changeData);
                dropStmt.executeUpdate(changeData.getDropColumnStatement());
                LOG.debug("Rename to new Column for {}", changeData);
                renameStmt.executeUpdate(changeData.getRenameColumnStatement());
            } catch (final SQLException e) {
                throw new TableException(e);
            }
        });
        try {
            this.connection.commit();
        } catch (final SQLException e) {
            throw new TableException(e);
        }
    }
    
    void doLogTransformationError(final Object... values) {
        assert values.length == 5;
        LOG.error("To value: {} for fromColumn {} (type: {}) to toColumn {} (type {})  - {}", values);
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
    
    static final class OriginalReplacementMap {
        
        private static final String PREFIX = "c";
        
        private final Map<String, String> adjustedOriginalToReplacement = new HashMap<>();
        private final Map<String, String> adjustedOriginalToOriginal = new HashMap<>();
        private final Map<String, String> replacementToAdjustedOriginal = new HashMap<>();
        
        void addOriginalAndReplacement(final String original, final String replacement) {
            Objects.requireNonNull(original, "orginal is null");
            Objects.requireNonNull(replacement, "replacement is null");
            if (this.adjustedOriginalToReplacement.containsKey(adjustCase(original))) {
                throw new DoubleKeyException(original, "Key duplicate: " + original);
            }
            if (this.replacementToAdjustedOriginal.containsKey(adjustCase(replacement))) {
                throw new DoubleKeyException(original, "Replacement duplicate: " + replacement);
            }

            this.adjustedOriginalToReplacement.put(adjustCase(original), adjustCase(replacement));
            this.replacementToAdjustedOriginal.put(adjustCase(replacement), adjustCase(original));
            this.adjustedOriginalToOriginal.put(adjustCase(original), original);
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
            final String adjusted = adjustCase(original);
            if (this.adjustedOriginalToReplacement.containsKey(adjusted)) {
                return Optional.of(this.adjustedOriginalToReplacement.get(adjusted));
            }
            return Optional.empty();
        }
        
        public Optional<String> originalForReplacement(final String replacement) {
            final String adjusted = adjustCase(replacement);
            if (this.replacementToAdjustedOriginal.containsKey(adjusted)) {
                final String adjustedOriginal = this.replacementToAdjustedOriginal.get(adjusted);
                return Optional.of(this.adjustedOriginalToOriginal.get(adjustedOriginal));
            }
            return Optional.empty();
        }
        
        public Iterator<String> originals() {
            final List<String> originals = new ArrayList<>(this.adjustedOriginalToOriginal.values());
            originals.sort((s1, s2) -> s2.length() - s1.length());
            return originals.iterator();
        }

        public Iterator<String> replacements() {
            return this.replacementToAdjustedOriginal.keySet().iterator();
        }

        @Override
        public String toString() {
            return "OriginalReplacementMap{" + "originalToReplacement=" + adjustedOriginalToReplacement + ", replacementToOriginal=" + replacementToAdjustedOriginal + '}';
        }
    }
        
    static final class ChangeColumnTypeData {

        private EntryDescriptor fromDescriptor;
        private EntryDescriptor toDescriptor;
        private EntryDescriptor sourceDescriptor;
        private TypeTransformer transformer;
        private String newColumnStatement;
        private String dropColumnStatement;
        private String renameColumnStatement;

        public EntryDescriptor getSourceDescriptor() {
            return this.sourceDescriptor;
        }

        public void setSourceDescriptor(EntryDescriptor sourceDescriptor) {
            this.sourceDescriptor = sourceDescriptor;
        }

        
        public String getNewColumnStatement() {
            return this.newColumnStatement;
        }

        public void setNewColumnStatement(String newColumnStatement) {
            this.newColumnStatement = newColumnStatement;
        }

        public EntryDescriptor getFromDescriptor() {
            return this.fromDescriptor;
        }

        public void setFromDescriptor(EntryDescriptor fromDescriptor) {
            this.fromDescriptor = fromDescriptor;
        }

        public EntryDescriptor getToDescriptor() {
            return this.toDescriptor;
        }

        public void setToDescriptor(EntryDescriptor toDescriptor) {
            this.toDescriptor = toDescriptor;
        }

        public TypeTransformer getTransformer() {
            return this.transformer;
        }

        public void setTransformer(TypeTransformer transformer) {
            this.transformer = transformer;
        }

        public String getDropColumnStatement() {
            return this.dropColumnStatement;
        }

        public void setDropColumnStatement(String dropColumnStatement) {
            this.dropColumnStatement = dropColumnStatement;
        }

        public String getRenameColumnStatement() {
            return this.renameColumnStatement;
        }

        public void setRenameColumnStatement(String renameColumnStatement) {
            this.renameColumnStatement = renameColumnStatement;
        }

        @Override
        public String toString() {
            return "ChangeColumnTypeData{" + "fromDescriptor=" + this.fromDescriptor + ", sourceDescriptor=" + this.sourceDescriptor + ", toDescriptor=" + this.toDescriptor + ", transformer=" + this.transformer + ", newColumnStatement=" + this.newColumnStatement + ", dropColumnStatement=" + this.dropColumnStatement + ", renameColumnStatement=" + this.renameColumnStatement + '}';
        }
    }
}
