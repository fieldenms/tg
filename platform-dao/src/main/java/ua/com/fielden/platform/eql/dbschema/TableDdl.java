package ua.com.fielden.platform.eql.dbschema;

import com.google.common.collect.ImmutableMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.dialect.Dialect;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.persistence.HibernateHelpers;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;

import java.lang.reflect.Field;
import java.util.*;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static ua.com.fielden.platform.entity.AbstractEntity.*;
import static ua.com.fielden.platform.entity.exceptions.NoSuchPropertyException.noSuchPropertyException;
import static ua.com.fielden.platform.entity.query.DbVersion.MSSQL;
import static ua.com.fielden.platform.entity.query.DbVersion.POSTGRESQL;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getKeyType;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getPropertyAnnotation;
import static ua.com.fielden.platform.reflection.Finder.findRealProperties;
import static ua.com.fielden.platform.utils.EntityUtils.*;

/// Generates DDL to create a table, primary key, indices (including unique) and foreign key constraints for the specified persistent type.
///
/// There are four separate methods to generate different aspects of DDL:
///
/// - `createTableSchema` — generates `CREATE TABLE` statement.
/// - `createPkSchema` — generates DDL for creation of a primary key statement.
/// - `createIndicesSchema` — generates DDL for creation of all unique and non-unique indices.
/// - `createFkSchema` — generates DDL for creation of all foreign keys; it is expected that all referenced tables are present when this DDL is executed.
///
public class TableDdl {

    private static final Logger LOGGER = LogManager.getLogger();

    public final Class<? extends AbstractEntity<?>> entityType;
    /** Maps a property path to its column definition. */
    private final Map<String, ColumnDefinition> columns;
    private final String tableName;

    public TableDdl(final ColumnDefinitionExtractor columnDefinitionExtractor, final Class<? extends AbstractEntity<?>> entityType) {
        this.entityType = entityType;
        this.columns = populateColumns(columnDefinitionExtractor, entityType);
        this.tableName = tableName(entityType);
    }

    private static Map<String, ColumnDefinition> populateColumns(final ColumnDefinitionExtractor columnDefinitionExtractor, final Class<? extends AbstractEntity<?>> entityType) {
        final var columns = ImmutableMap.<String, ColumnDefinition> builder();

        columns.put(ID, columnDefinitionExtractor.extractIdProperty(entityType));

        columnDefinitionExtractor.extractSimpleKeyProperty(entityType).ifPresent(colDef -> columns.put(KEY, colDef));

        columns.put(VERSION, columnDefinitionExtractor.extractVersionProperty(entityType));

        for (final Field propField : findRealProperties(entityType, MapTo.class)) {
            if (!shouldIgnore(propField)) {
                final MapTo mapTo = getPropertyAnnotation(MapTo.class, entityType, propField.getName());
                final IsProperty isProperty = getPropertyAnnotation(IsProperty.class, entityType, propField.getName());
                final PersistentType persistedType = getPropertyAnnotation(PersistentType.class, entityType, propField.getName());
                final boolean required = PropertyTypeDeterminator.isRequiredByDefinition(propField, entityType);
                final boolean unique = propField.isAnnotationPresent(Unique.class);
                final Optional<Integer> compositeKeyMemberOrder = Optional.ofNullable(propField.getAnnotation(CompositeKeyMember.class)).map(ann -> ann.value());
                columns.putAll(columnDefinitionExtractor.extractFromProperty(entityType, propField.getName(), propField.getType(), isProperty, mapTo, persistedType, required, unique, compositeKeyMemberOrder));
            }
        }

        return columns.buildOrThrow();
    }

    private static boolean shouldIgnore(final Field propField) {
        return KEY.equals(propField.getName());
    }

    public String getTableName() {
        return this.tableName;
    }

    private Collection<ColumnDefinition> columnDefinitions() {
        return columns.values();
    }

    /// Generates a DDL statement for a table (without constraints or indices) based on provided RDBMS dialect.
    ///
    public String createTableSchema(final Dialect dialect) {
        final var sb = new StringBuilder();
        sb.append("CREATE TABLE %s ( ".formatted(this.tableName));
        sb.append(columnDefinitions().stream().map(col -> col.schemaString(dialect)).collect(Collectors.joining(", ")));
        sb.append(" );");
        return sb.toString();
    }

    /// Returns a column definition for the specified `property`.
    ///
    /// * It is considered an error if the `property` is not contained within this table.
    /// * If the `property` is **component-typed**, the path must be a full path to the component.
    ///   For example, `note.coreText` for the property `note: RichText`.
    /// * If the `property` is **union-typed**, the path must be a full path to a union member.
    ///   For example, `location.workshop` for the union-typed property `location: Location`, where union members are `workshop` and `station`.
    /// * Otherwise, the path must be a simple property name.
    ///
    /// @param property the property path
    ///
    public ColumnDefinition getColumnDefinition(final String property) {
        if (columns.containsKey(property)) {
            return columns.get(property);
        }
        else {
            throw noSuchPropertyException(entityType, property);
        }
    }

    /// An alternative to [#getColumnDefinition(String)] that returns an empty optional if the specified property is not contained in this table.
    ///
    public Optional<ColumnDefinition> getColumnDefinitionOpt(final String property) {
        return Optional.ofNullable(columns.get(property));
    }

    /// Generates DDL statements for all unique and non-unique indices, including those representing a business key.
    ///
    public List<String> createIndicesSchema(final Dialect dialect) {
        final Map<Boolean, List<ColumnDefinition>> uniqueAndNot = columnDefinitions().stream().collect(Collectors.partitioningBy(col -> col.unique));
        final List<String> result = new LinkedList<>();
        if (isCompositeEntity(entityType)) {
            result.add(createUniqueCompositeIndicesSchema(columnDefinitions().stream(), dialect));
        }
        result.addAll(createUniqueIndicesSchema(uniqueAndNot.get(true).stream(), dialect));
        result.addAll(createNonUniqueIndicesSchema(uniqueAndNot.get(false).stream(), dialect));
        return result;
    }

    private String createUniqueCompositeIndicesSchema(final Stream<ColumnDefinition> cols, final Dialect dialect) {
        final String tableName = tableName(entityType);
        final String keyMembersStr = cols
                .filter(col -> col.compositeKeyMemberOrder.isPresent())
                .sorted(Comparator.comparingInt(col -> col.compositeKeyMemberOrder.get()))
                .map(col -> col.name)
                .collect(Collectors.joining(", "));
        return "CREATE UNIQUE INDEX KUI_%1$s ON %1$s(%2$s);".formatted(this.tableName, keyMembersStr);
    }

    private List<String> createUniqueIndicesSchema(final Stream<ColumnDefinition> cols, final Dialect dialect) {
        final DbVersion dbVersion = HibernateHelpers.getDbVersion(dialect);
        return cols
                // We know how to create unique indexes for nullable columns in case of SQL Server and PostgreSQL.
                .filter(col -> col.nullable ? MSSQL == dbVersion || POSTGRESQL == dbVersion : true)
                .filter(col -> {
                    if (!col.indexApplicable) {
                        LOGGER.warn(() -> "Index for column type [%s] is not supported by [%s]. Skipping index creation for column [%s] in [%s]."
                                          .formatted(col.sqlTypeName, dbVersion, col.name, entityType.getSimpleName()));
                        return false;
                    } else {
                        return true;
                    }
                })
                .map(col -> {
                    // Otherwise, create a unique index.
                    final String indexName = "KEY_".equals(col.name) ? "KUI_%s".formatted(this.tableName) : "UI_%s_%s".formatted(this.tableName, col.name);
                    final StringBuilder sb = new StringBuilder();
                    sb.append("CREATE UNIQUE INDEX %s ON %s(%s)".formatted(indexName, this.tableName, col.name));
                    // Enforce uniqueness for NOT NULL values, permitting multiple records with NUll in the same column.
                    if (col.nullable) {
                        sb.append(" WHERE (%s IS NOT NULL)".formatted(col.name));
                    }
                    // Enforce uniqueness for value 'Y', permitting multiple records with 'N' in the same column.
                    else if (col.javaType == boolean.class) {
                        sb.append(" WHERE (%s = 'Y')".formatted(col.name));
                    }
                    sb.append(";");
                    return sb.toString();
                })
                .collect(toList());
    }

    public List<String> createNonUniqueIndicesSchema(final Stream<ColumnDefinition> cols, final Dialect dialect) {
        final DbVersion dbVersion = HibernateHelpers.getDbVersion(dialect);
        return cols
                .map(col -> col.maybeIndex.map(index -> {
                    if (!col.indexApplicable) {
                        LOGGER.warn(() -> "Index for column type [%s] is not supported by [%s]. Skipping index creation for column [%s] in [%s]."
                                          .formatted(col.sqlTypeName, dbVersion, col.name, entityType.getSimpleName()));
                        return "";
                    }
                    else {
                        return "CREATE INDEX %s ON %s(%s %s)".formatted(
                                indexName(this.tableName, col.name),
                                this.tableName,
                                col.name,
                                switch (index.order()) {
                                    case ASC -> "ASC";
                                    case DESC -> "DESC";
                                });
                    }
                }).orElse(""))
                .filter(s -> !s.isEmpty())
                .collect(toList());
    }

    /// Returns the name of an index for the specified column.
    ///
    /// It is **not** required for the specified column to be present in this table.
    ///
    public String getIndexName(final ColumnDefinition column) {
        return indexName(this.tableName, column.name);
    }

    /// Returns the name of an index for the specified `property`.
    ///
    /// * It is considered an error if the `property` is not contained within this table.
    /// * If the `property` is **component-typed**, the path must be a full path to the component.
    ///   For example, `note.coreText` for the property `note: RichText`.
    /// * If the `property` is **union-typed**, the path must be a full path to a union member.
    ///   For example, `location.workshop` for the union-typed property `location: Location`, where union members are `workshop` and `station`.
    /// * Otherwise, the path must be a simple property name.
    ///
    /// @param property  a property path.
    ///
    public String getIndexName(final CharSequence property) {
        return getIndexName(getColumnDefinition(property.toString()));
    }

    private static String indexName(final CharSequence tableName, final CharSequence columnName) {
        return "I_%s_%s".formatted(tableName, columnName);
    }

    /// Generates a DDL statement to add a primary key constraint on column `_ID`.
    ///
    public String createPkSchema(final Dialect dialect) {
        // This statement should be suitable for the majority of SQL dialects
        return "ALTER TABLE %1$s ADD CONSTRAINT PK_%1$s_ID PRIMARY KEY (_ID);".formatted(this.tableName);
    }

    /// Generates DDL statements to add all foreign key constraints.
    /// Execution of this statement should occur only after all tables schema have been executed.
    ///
    public List<String> createFkSchema(final Dialect dialect) {
        // This statement should be suitable for the majority of SQL dialects
        final List<String> ddl = columnDefinitions().stream()
                .filter(cd -> isPersistentEntityType(cd.javaType))
                .map(cd -> {
                    final String thatTableName = tableName((Class<? extends AbstractEntity<?>>) cd.javaType);
                    return fkConstraint(dialect, this.tableName, cd.name, thatTableName);
                }).collect(toList());

        // let's handle a situation where the entity type is a one-2-one entity.
        if (isOneToOne(entityType)) {
            final var thatTableName = tableName((Class<? extends AbstractEntity<?>>) getKeyType(entityType));
            final var fk = fkConstraint(dialect, this.tableName, "_ID", thatTableName);
            ddl.add(fk);
        }

        return ddl;
    }

    private static String fkConstraint(final Dialect dialect, final String thisTableName, final String colName, final String thatTableName) {
        return "ALTER TABLE %1$s ADD CONSTRAINT FK_%1$s_%2$s FOREIGN KEY (%2$s) REFERENCES %3$s (_ID);".formatted(thisTableName, colName, thatTableName);
    }

    /// Computes the table name for a given entity.
    ///
    public static String tableName(final Class<? extends AbstractEntity<?>> entityType) {
        final MapEntityTo mapEntityTo = entityType.getAnnotation(MapEntityTo.class);
        if (isEmpty(mapEntityTo.value())) {
            return entityType.getSimpleName().toUpperCase() + "_";
        } else {
            return mapEntityTo.value();
        }
    }

}
