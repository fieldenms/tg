package ua.com.fielden.platform.eql.dbschema;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.dialect.Dialect;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.persistence.HibernateHelpers;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.query.DbVersion.MSSQL;
import static ua.com.fielden.platform.entity.query.DbVersion.POSTGRESQL;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getKeyType;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getPropertyAnnotation;
import static ua.com.fielden.platform.reflection.Finder.findRealProperties;
import static ua.com.fielden.platform.utils.EntityUtils.*;

/**
 * Generates DDL to create a table, primary key, indices (including unique) and foreign keys for the specified persistent type.
 * <p>
 * There are four separate methods to generate different aspects of DDL:
 * <ul>
 * <li><code>createTableSchema</code> -- generates <code>CREATE TABLE</code> statement.
 * <li><code>createPkSchema</code> -- generates DDL for creation of a primary key statement.
 * <li><code>createIndicesSchema</code> -- generates DDL for creation of all unique and non-unique indices.
 * <li><code>createFkSchema</code> -- generates DDL for creation of all foreign keys; it is expected that all referenced tables are present when this DDL is executed.
 * </ul>
 * 
 * @author TG Team
 *
 */
public class TableDdl {

    private static final Logger LOGGER = LogManager.getLogger();

    public final Class<? extends AbstractEntity<?>> entityType;
    private final Set<ColumnDefinition> columns;

    public TableDdl(final ColumnDefinitionExtractor columnDefinitionExtractor, final Class<? extends AbstractEntity<?>> entityType) {
        this.entityType = entityType;
        this.columns = populateColumns(columnDefinitionExtractor, entityType);
    }

    private static Set<ColumnDefinition> populateColumns(final ColumnDefinitionExtractor columnDefinitionExtractor, final Class<? extends AbstractEntity<?>> entityType) {
        final Set<ColumnDefinition> columns = new LinkedHashSet<>();

        columns.add(columnDefinitionExtractor.extractIdProperty());

        columnDefinitionExtractor.extractSimpleKeyProperty(entityType).map(key -> columns.add(key));

        columns.add(columnDefinitionExtractor.extractVersionProperty());

        for (final Field propField : findRealProperties(entityType, MapTo.class)) {
            if (!shouldIgnore(propField, entityType)) {
                final MapTo mapTo = getPropertyAnnotation(MapTo.class, entityType, propField.getName());
                final IsProperty isProperty = getPropertyAnnotation(IsProperty.class, entityType, propField.getName());
                final PersistentType persistedType = getPropertyAnnotation(PersistentType.class, entityType, propField.getName());
                final boolean required = PropertyTypeDeterminator.isRequiredByDefinition(propField, entityType);
                final boolean unique = propField.isAnnotationPresent(Unique.class);
                final Optional<Integer> compositeKeyMemberOrder = Optional.ofNullable(propField.getAnnotation(CompositeKeyMember.class)).map(ann -> ann.value());
                columns.addAll(columnDefinitionExtractor.extractFromProperty(propField.getName(), propField.getType(), isProperty, mapTo, persistedType, required, unique, compositeKeyMemberOrder));
            }
        }

        return columns;
    }

    private static boolean shouldIgnore(final Field propField, final Class<? extends AbstractEntity<?>> entityType) {
        return KEY.equals(propField.getName());
    }

    /**
     * Generates a DDL statement for a table (without constraints or indices) based on provided RDBMS dialect.
     * 
     * @param dialect
     * @return
     */
    public String createTableSchema(final Dialect dialect) {
        final StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE %s ( ".formatted(tableName(entityType)));
        sb.append(columns.stream().map(col -> col.schemaString(dialect)).collect(Collectors.joining(", ")));
        sb.append(" );");
        return sb.toString();
    }

    /**
     * Generates DDL statements for all unique and non-unique indices, including those representing a business key.
     * 
     * @param dialect
     * @return
     */
    public List<String> createIndicesSchema(final Dialect dialect) {
        final Map<Boolean, List<ColumnDefinition>> uniqueAndNot = columns.stream().collect(Collectors.partitioningBy(col -> col.unique));
        final List<String> result = new LinkedList<>();
        if (isCompositeEntity(entityType)) {
            result.add(createUniqueCompositeIndicesSchema(columns.stream(), dialect));
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
        return "CREATE UNIQUE INDEX KUI_%1$s ON %1$s(%2$s);".formatted(tableName, keyMembersStr);
    }

    private List<String> createUniqueIndicesSchema(final Stream<ColumnDefinition> cols, final Dialect dialect) {
        final DbVersion dbVersion = HibernateHelpers.getDbVersion(dialect);
        return cols
                // We know how to create unique indexes for nullable columns in case of SQL Server and PostgreSQL.
                .filter(col -> col.nullable ? MSSQL == dbVersion || POSTGRESQL == dbVersion : true)
                .filter(col -> {
                    if (!col.indexApplicable) {
                        LOGGER.warn("Index for column type [%s] is not supported by [%s]. Skipping index creation for column [%s] in [%s]."
                                    .formatted(col.sqlTypeName, dbVersion, col.name, entityType.getSimpleName()));
                        return false;
                    } else {
                        return true;
                    }
                })
                .map(col -> {
                    // otherwise, let's create unique index with the nullable clause if required
                    final String tableName = tableName(entityType);
                    final String indexName = "KEY_".equals(col.name) ? "KUI_%s".formatted(tableName) : "UI_%s_%s".formatted(tableName, col.name);
                    final StringBuilder sb = new StringBuilder();
                    sb.append("CREATE UNIQUE INDEX %s ON %s(%s)".formatted(indexName, tableName, col.name));
                    if (col.nullable) {
                        sb.append(" WHERE (%s IS NOT NULL)".formatted(col.name));
                    }
                    sb.append(";");
                    return sb.toString();
                })
                .collect(toList());
    }

    private List<String> createNonUniqueIndicesSchema(final Stream<ColumnDefinition> cols, final Dialect dialect) {
        final DbVersion dbVersion = HibernateHelpers.getDbVersion(dialect);
        return cols
                .filter(col -> col.requiresIndex || isPersistentEntityType(col.javaType))
                .filter(col -> {
                    if (!col.indexApplicable) {
                        LOGGER.warn("Index for column type [%s] is not supported by [%s]. Skipping index creation for column [%s] in [%s]."
                                    .formatted(col.sqlTypeName, dbVersion, col.name, entityType.getSimpleName()));
                        return false;
                    } else {
                        return true;
                    }
                })
                .map(col -> {
                    final String tableName = tableName(entityType);
                    return "CREATE INDEX I_%1$s_%2$s ON %1$s(%2$s);".formatted(tableName, col.name);
                })
                .collect(toList());
    }

    /**
     * Generates a DDL statement to add a primary key constraint on column <code>_ID</code>.
     * 
     * @param dialect
     * @return
     */
    public String createPkSchema(final Dialect dialect) {
        // This statement should be suitable for the majority of SQL dialects
        final String tableName = tableName(entityType);
        return "ALTER TABLE %1$s ADD CONSTRAINT PK_%1$s_ID PRIMARY KEY (_ID);".formatted(tableName);
    }

    /**
     * Generates DDL statements to add all foreign key constraints. Execution of this statement should occur only after all tables schema have been executed.
     * 
     * @param dialect
     * @return
     */
    public List<String> createFkSchema(final Dialect dialect) {
        // This statement should be suitable for the majority of SQL dialects
        final String thisTableName = tableName(entityType);
        final List<String> ddl = columns.stream()
                .filter(cd -> isPersistentEntityType(cd.javaType))
                .map(cd -> {
                    final String thatTableName = tableName((Class<? extends AbstractEntity<?>>) cd.javaType);
                    return fkConstraint(dialect, thisTableName, cd.name, thatTableName);
                }).collect(toList());

        // let's handle a situation where the entity type is a one-2-one entity.
        if (isOneToOne(entityType)) {
            final var thatTableName = tableName((Class<? extends AbstractEntity<?>>) getKeyType(entityType));
            final var fk = fkConstraint(dialect, thisTableName, "_ID", thatTableName);
            ddl.add(fk);
        }

        return ddl;
    }

    private static String fkConstraint(final Dialect dialect, final String thisTableName, final String colName, final String thatTableName) {
        return "ALTER TABLE %1$s ADD CONSTRAINT FK_%1$s_%2$s FOREIGN KEY (%2$s) REFERENCES %3$s (_ID);".formatted(thisTableName, colName, thatTableName);
    }

    /**
     * Computes the table name for a given entity.
     *
     * @param entityType
     * @return
     */
    public static String tableName(final Class<? extends AbstractEntity<?>> entityType) {
        final MapEntityTo mapEntityTo = entityType.getAnnotation(MapEntityTo.class);
        if (isEmpty(mapEntityTo.value())) {
            return entityType.getSimpleName().toUpperCase() + "_";
        } else {
            return mapEntityTo.value();
        }
    }
}
