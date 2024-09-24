package ua.com.fielden.platform.eql.dbschema;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.query.DbVersion.MSSQL;
import static ua.com.fielden.platform.entity.query.DbVersion.POSTGRESQL;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getKeyType;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getPropertyAnnotation;
import static ua.com.fielden.platform.reflection.Finder.findRealProperties;
import static ua.com.fielden.platform.utils.EntityUtils.isCompositeEntity;
import static ua.com.fielden.platform.utils.EntityUtils.isOneToOne;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;

import java.lang.reflect.Field;
import java.sql.Types;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.dialect.Dialect;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.PersistentType;
import ua.com.fielden.platform.entity.annotation.Unique;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.persistence.HibernateHelpers;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;

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
    public String createTableSchema(final Dialect dialect, final String colDelimeter) {
        final StringBuilder sb = new StringBuilder();
        sb.append(format("CREATE TABLE %s (", tableName(entityType)));
        sb.append("    " + colDelimeter);
        sb.append(columns.stream().map(col -> "    " + col.schemaString(dialect)).collect(Collectors.joining(", " + colDelimeter)));
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
        final Map<Boolean, List<ColumnDefinition>> uniqueAndNot = columns.stream().collect(Collectors.partitioningBy(ColumnDefinition::unique));
        final List<String> result = new LinkedList<>();
        if (isCompositeEntity(entityType)) {
            result.add(createUniqueCompositeIndicesSchema(columns.stream(), dialect));
        }
        result.addAll(createUniqueIndicesSchema(uniqueAndNot.get(true).stream(), dialect));
        result.addAll(createNonUniqueIndicesSchema(uniqueAndNot.get(false).stream(), dialect));
        return result;
    }

    private String createUniqueCompositeIndicesSchema(final Stream<ColumnDefinition> cols, final Dialect dialect) {
        final StringBuilder sb = new StringBuilder();

        final String tableName = tableName(entityType);
        final String keyMembersStr = cols
                .filter(col -> col.compositeKeyMemberOrder().isPresent())
                .sorted((col1, col2) -> Integer.compare(col1.compositeKeyMemberOrder().get(), col2.compositeKeyMemberOrder().get()))
                .map(ColumnDefinition::name)
                .collect(Collectors.joining(", "));
        sb.append(format("CREATE UNIQUE INDEX KUI_%s ON %s(%s);", tableName, tableName, keyMembersStr));
        return sb.toString();
    }

    private List<String> createUniqueIndicesSchema(final Stream<ColumnDefinition> cols, final Dialect dialect) {
        final DbVersion dbVersion = HibernateHelpers.getDbVersion(dialect);
        return cols
                // We know how to create unique indexes for nullable columns in case of SQL Server and PostgreSQL.
                .filter(col -> col.nullable() ? MSSQL == dbVersion || POSTGRESQL == dbVersion : true)
                .filter(col -> {
                    if (!isIndexApplicable(col, dbVersion)) {
                        LOGGER.warn(format("Index for column type [%s] is not supported by [%s]. Skipping index creation for column [%s] in [%s].",
                                           col.sqlType(), dbVersion, col.name(), entityType.getSimpleName()));
                        return false;
                    } else {
                        return true;
                    }
                })
                .map(col -> {
                    // otherwise, let's create unique index with the nullable clause if required
                    final String tableName = tableName(entityType);
                    final String indexName = "KEY_".equals(col.name()) ? format("KUI_%s", tableName) : format("UI_%s_%s", tableName, col.name());
                    final StringBuilder sb = new StringBuilder();
                    sb.append(format("CREATE UNIQUE INDEX %s ON %s(%s)", indexName, tableName, col.name()));
                    if (col.nullable()) {
                        sb.append(format(" WHERE (%s IS NOT NULL)", col.name()));
                    }
                    sb.append(";");
                    return sb.toString();
                })
                .collect(toList());
    }

    private List<String> createNonUniqueIndicesSchema(final Stream<ColumnDefinition> cols, final Dialect dialect) {
        final DbVersion dbVersion = HibernateHelpers.getDbVersion(dialect);
        return cols
                .filter(col -> col.requiresIndex() || isPersistedEntityType(col.javaType()))
                .filter(col -> {
                    if (!isIndexApplicable(col, dbVersion)) {
                        LOGGER.warn(format("Index for column type [%s] is not supported by [%s]. Skipping index creation for column [%s] in [%s].",
                                           col.sqlType(), dbVersion, col.name(), entityType.getSimpleName()));
                        return false;
                    } else {
                        return true;
                    }
                })
                .map(col -> {
                    final StringBuilder sb = new StringBuilder();
                    final String tableName = tableName(entityType);
                    sb.append(format("CREATE INDEX I_%s_%s ON %s(%s);", tableName, col.name(), tableName, col.name()));
                    return sb.toString();
                })
                .collect(toList());
    }

    private static boolean isIndexApplicable(final ColumnDefinition column, final DbVersion dbVersion) {
        return switch (dbVersion) {
            // https://learn.microsoft.com/en-us/sql/t-sql/statements/create-index-transact-sql
            case MSSQL -> switch (column.sqlType()) {
                case SqlType.Named $ -> true;
                case SqlType.TypeCode (var code) -> switch (code) {
                    case Types.VARCHAR, Types.VARBINARY, Types.NVARCHAR -> column.length() != Integer.MAX_VALUE;
                    default -> true;
                };
            };
            default -> true;
        };
    }

    /**
     * Generates a DDL statement to add a primary key constraint on column <code>_ID</code>.
     * 
     * @param dialect
     * @return
     */
    public String createPkSchema(final Dialect dialect) {
        // This statement should be suitable for majority of SQL dialogs
        final String tableName = tableName(entityType);
        final StringBuilder sb = new StringBuilder();
        sb.append(format("ALTER TABLE %s ", tableName));
        sb.append(" ");
        sb.append(format("ADD CONSTRAINT PK_%s_ID PRIMARY KEY (_ID);", tableName));
        return sb.toString();
    }

    /**
     * Generates DDL statements to add all foreign key constraints. Execution of this statement should occur only after all tables schema have been executed.
     * 
     * @param dialect
     * @return
     */
    public List<String> createFkSchema(final Dialect dialect) {
        // This statement should be suitable for majority of SQL dialogs
        final String thisTableName = tableName(entityType);
        final List<String> ddl = columns.stream()
                .filter(cd -> isPersistedEntityType(cd.javaType()))
                .map(cd -> {
                    final StringBuilder sb = new StringBuilder();
                    final String thatTableName = tableName((Class<? extends AbstractEntity<?>>) cd.javaType());
                    fkConstraint(dialect, thisTableName, cd.name(), sb, thatTableName);
                    return sb.toString();
        
                }).collect(toList());

        // let's handle a situation where entity type is one-2-one entity.
        if (isOneToOne(entityType)) {
            final StringBuilder sb = new StringBuilder();
            final String thatTableName = tableName((Class<? extends AbstractEntity<?>>) getKeyType(entityType));
            fkConstraint(dialect, thisTableName, "_ID", sb, thatTableName);
            ddl.add(sb.toString());
        }

        return ddl;
    }

    private void fkConstraint(final Dialect dialect, final String thisTableName, final String colName, final StringBuilder sb, final String thatTableName) {
        sb.append(format("ALTER TABLE %s ", thisTableName));
        sb.append(" ");
        sb.append(format("ADD CONSTRAINT FK_%s_%s FOREIGN KEY (%s) REFERENCES %s (_ID);", thisTableName, colName, colName, thatTableName));
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
