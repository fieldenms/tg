package ua.com.fielden.platform.eql.dbschema;

import com.google.common.collect.ImmutableMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.dialect.Dialect;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.dbschema.exceptions.DbSchemaException;
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
/// ## Default `id` column takes precedence over an override
///
/// The `_ID` column is always generated from `AbstractEntity.id` via [ColumnDefinitionExtractor#extractIdProperty].
/// A subclass MAY redeclare `id` with `@IsProperty` (to expose it on a master/centre, use it as a `@CompositeKeyMember`, etc.).
/// When such a redeclaration is present, an accompanying `@MapTo` is REQUIRED to carry exactly the value `"_ID"`;
/// any other value — or the empty default `""`, which would map to `"ID_"` — is rejected with a [DbSchemaException].
/// The override's `@MapTo("_ID")` is treated as a no-op confirming the default column name; the override does NOT
/// contribute to the column definition emitted by this class.
///
/// As a consequence, the following annotations on an overridden `id` are **dismissed from DDL generation**:
///
/// - `@CompositeKeyMember(N)` — does not contribute to the unique composite index. The PK on `_ID` already enforces
/// uniqueness, so the index would be redundant. When `id` is the only composite key member, the composite index is
/// suppressed entirely (see [#createIndicesSchema]) to avoid an empty `CREATE UNIQUE INDEX … ON T()`.
/// - `@Optional` — does not relax the column's `NOT NULL` constraint; `_ID` is always `NOT NULL` because it is the PK.
/// - `@Required` — already the default for `_ID`; redundant.
/// - `@Unique` — does not produce a separate unique index. The PK already enforces uniqueness.
/// - `@PersistentType` — does not change the column's SQL type; `_ID` is always emitted with the default `Long`-to-SQL mapping.
/// - `@IsProperty(length, scale, precision)` — not applied. These attributes are not meaningful for the `Long`-typed `id`.
/// - `@MapTo(defaultValue = ...)` — not applied. A `DEFAULT` clause on a PK column is not generated.
///
/// In short, the override is a model-level declaration only (visible to TG reflection, fetch providers, metamodels,
/// validators, etc.); the DDL is governed exclusively by `AbstractEntity.id`.
///
public class TableDdl {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String ERR_ID_OVERRIDE_INCORRECT_MAP_TO =
        "Property [id] in entity type [%%s] is annotated with @MapTo%s. When [id] overrides, only @MapTo(\"_ID\") is permitted (matching the default ID column).";
    private static final String ERR_ID_OVERRIDE_NON_DEFAULT_MAP_TO_COLUMN_NAME =
        ERR_ID_OVERRIDE_INCORRECT_MAP_TO.formatted("(\"%s\")");
    private static final String ERR_ID_OVERRIDE_MAP_TO_EMPTY_COLUMN_NAME =
        ERR_ID_OVERRIDE_INCORRECT_MAP_TO.formatted(" without a value (would default to \"ID_\")");

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

        // The `id`, `key` and `version` columns are always sourced from the AbstractEntity-level declarations.
        // For `id` this is intentional even when a subclass redeclares it — see the class javadoc on
        // "Default `id` column takes precedence over an override".
        columns.put(ID, columnDefinitionExtractor.extractIdProperty(entityType));

        columnDefinitionExtractor.extractSimpleKeyProperty(entityType).ifPresent(colDef -> columns.put(KEY, colDef));

        columns.put(VERSION, columnDefinitionExtractor.extractVersionProperty(entityType));

        for (final Field propField : findRealProperties(entityType, MapTo.class)) {
            if (!shouldIgnore(propField, entityType)) {
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

    private static boolean shouldIgnore(final Field propField, final Class<? extends AbstractEntity<?>> entityType) {
        // Ignore `key` and `id` `@IsProperty` fields as they are defined specially.
        return KEY.equals(propField.getName()) || isIdProperty(propField, entityType);
    }

    /// Determines whether a [MapTo] + [IsProperty]-annotated `propField` represents a subclass redeclaration of `id`,
    /// and validates the `@MapTo` value on that field.
    ///
    /// The default `id` column wins (see the class javadoc): the override may exist for model-level reasons
    /// (e.g. exposing `id` on a centre/master or marking it as `@CompositeKeyMember`), but it must agree with the
    /// default column name `"_ID"`. Any other `@MapTo` value — including the empty default which would map to
    /// `"ID_"` — is rejected with a [DbSchemaException]. When the override is valid, this method returns `true`
    /// so the surrounding iteration skips re-emitting the column.
    ///
    /// Annotations on the override other than `@MapTo("_ID")` itself are silently dismissed for DDL purposes;
    /// the class javadoc lists which ones and explains why each dismissal is safe.
    ///
    private static boolean isIdProperty(final Field propField, final Class<? extends AbstractEntity<?>> entityType) {
        final var isIdProperty = ID.equals(propField.getName());
        if (isIdProperty) {
            // `findRealProperties(MapTo.class)` filters fields that have both `@IsProperty` and `@MapTo`.
            // So `@MapTo` is guaranteed to be present here.
            // The id column is always generated as `_ID` by `extractIdProperty`.
            // So an override is only acceptable when it matches that column name exactly.
            final MapTo mapTo = getPropertyAnnotation(MapTo.class, entityType, ID);
            final String value = mapTo.value();
            if (value.isEmpty()) {
                throw new DbSchemaException(ERR_ID_OVERRIDE_MAP_TO_EMPTY_COLUMN_NAME.formatted(entityType.getSimpleName()));
            }
            if (!"_ID".equals(value)) {
                throw new DbSchemaException(ERR_ID_OVERRIDE_NON_DEFAULT_MAP_TO_COLUMN_NAME.formatted(entityType.getSimpleName(), value));
            }
        }
        return isIdProperty;
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
    /// For composite-key entities a unique index over all `@CompositeKeyMember` columns is emitted, but only when
    /// at least one such column is present in this table. This guard matters when the only declared composite key
    /// member is the overridden `id` property: its `@CompositeKeyMember` is dismissed (see the class javadoc),
    /// so emitting the index would produce an empty column list — invalid SQL. The PK on `_ID` already enforces
    /// uniqueness in that scenario, so skipping the redundant composite index is safe.
    ///
    public List<String> createIndicesSchema(final Dialect dialect) {
        final Map<Boolean, List<ColumnDefinition>> uniqueAndNot = columnDefinitions().stream().collect(Collectors.partitioningBy(col -> col.unique));
        final List<String> result = new LinkedList<>();
        if (isCompositeEntity(entityType) && hasCompositeKeyMembers()) {
            result.add(createUniqueCompositeIndicesSchema(columnDefinitions().stream(), dialect));
        }
        result.addAll(createUniqueIndicesSchema(uniqueAndNot.get(true).stream(), dialect));
        result.addAll(createNonUniqueIndicesSchema(uniqueAndNot.get(false).stream(), dialect));
        return result;
    }

    /// Returns `true` if any column in this table carries a `@CompositeKeyMember` ordering — that is,
    /// if there is at least one column to include in the unique composite index.
    ///
    private boolean hasCompositeKeyMembers() {
        return columnDefinitions().stream().anyMatch(col -> col.compositeKeyMemberOrder.isPresent());
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
