package ua.com.fielden.platform.eql.dbschema;

import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static ua.com.fielden.platform.entity.AbstractEntity.DESC;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getKeyType;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getPropertyAnnotation;
import static ua.com.fielden.platform.reflection.Finder.findRealProperties;
import static ua.com.fielden.platform.utils.EntityUtils.isOneToOne;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.SQLServerDialect;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.PersistentType;
import ua.com.fielden.platform.entity.annotation.Unique;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.utils.EntityUtils;

public class TableDefinition {
    public final Class<? extends AbstractEntity<?>> entityType;
    private final Set<ColumnDefinition> columns;

    public TableDefinition(final ColumnDefinitionExtractor columnDefinitionExtractor, final Class<? extends AbstractEntity<?>> entityType) {
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
                final PersistentType persistedType = getPropertyAnnotation(PersistentType.class, entityType, propField.getName());
                final boolean required = PropertyTypeDeterminator.isRequiredByDefinition(propField, entityType);
                final boolean unique = propField.isAnnotationPresent(Unique.class);
                columns.addAll(columnDefinitionExtractor.extractFromProperty(propField.getName(), propField.getType(), mapTo, persistedType, required, unique));
            }
        }

        return columns;
    }

    private static boolean shouldIgnore(final Field propField, final Class<? extends AbstractEntity<?>> entityType) {
        return KEY.equals(propField.getName()) || DESC.equals(propField.getName()) && EntityUtils.hasDescProperty(entityType);
    }

    private void addGoIfApplicable(final Dialect dialect, final StringBuilder sb) {
        if (dialect instanceof SQLServerDialect) {
            sb.append("\nGO\n");
        }
    }

    /**
     * Generates a DDL statement for a table (without constraints or indices) based on provided RDBMS dialect.
     * 
     * @param dialect
     * @return
     */
    public String createTableSchema(final Dialect dialect) {
        final StringBuilder sb = new StringBuilder();
        sb.append(format("CREATE TABLE %s (", tableName(entityType)));
        sb.append("\n");
        sb.append(columns.stream().map(col -> "    " + col.schemaString(dialect)).collect(Collectors.joining(",\n")));
        sb.append("\n);");
        addGoIfApplicable(dialect, sb);
        return sb.toString();
    }

    public String createIndicesSchema(final Dialect dialect) {
        final Map<Boolean, List<ColumnDefinition>> uniqueAndNot = columns.stream().collect(Collectors.partitioningBy(cd -> cd.unique));
        final StringBuilder sb = new StringBuilder();
        sb.append(createUniqueCompositeIndicesSchema(columns.stream(), dialect));
        sb.append(createUniqueIndicesSchema(uniqueAndNot.get(true).stream(), dialect));
        sb.append(createNonUniqueIndicesSchema(uniqueAndNot.get(false).stream(), dialect));
        return sb.toString();
    }
    
    private String createUniqueCompositeIndicesSchema(final Stream<ColumnDefinition> stream, final Dialect dialect) {
        // TODO Auto-generated method stub
        return "";
    }

    private String createUniqueIndicesSchema(final Stream<ColumnDefinition> cols, final Dialect dialect) {
        return cols.map(col -> {
            final StringBuilder sb = new StringBuilder();
            
            final String tableName = tableName(entityType);
            sb.append(format("CREATE UNIQUE INDEX UI_%s_%s ON %s(%s)", tableName, col.name, tableName, col.name));
            if (col.nullable) {
                sb.append(format(" WHERE (%s IS NOT NULL)", col.name));
            }
            sb.append(";");
            addGoIfApplicable(dialect, sb);
            return sb.toString();
        }).collect(Collectors.joining("\n"));
    }

    private String createNonUniqueIndicesSchema(final Stream<ColumnDefinition> cols, final Dialect dialect) {
        return cols.filter(col -> isPersistedEntityType(col.javaType)).map(col -> {
            final StringBuilder sb = new StringBuilder();
            
            final String tableName = tableName(entityType);
            sb.append(format("CREATE INDEX I_%s_%s ON %s(%s);", tableName, col.name, tableName, col.name));
            addGoIfApplicable(dialect, sb);
            return sb.toString();
        }).collect(Collectors.joining("\n"));
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
        sb.append("\n");
        sb.append(format("ADD CONSTRAINT PK_%s_ID PRIMARY KEY (_ID);", tableName));
        addGoIfApplicable(dialect, sb);
        return sb.toString();
    }

    /**
     * Generates a DDL statement to add all foreign key constraints. Execution of this statement should occur only after all tables schema have been executed.
     * 
     * @param dialect
     * @return
     */
    public String createFkSchema(final Dialect dialect) {
        // This statement should be suitable for majority of SQL dialogs
        final String thisTableName = tableName(entityType);
        final String ddl = columns.stream().filter(cd -> isPersistedEntityType(cd.javaType)).map(cd -> {
            final StringBuilder sb = new StringBuilder();
            final String thatTableName = tableName((Class<? extends AbstractEntity<?>>) cd.javaType);
            fkConstraint(dialect, thisTableName, cd.name, sb, thatTableName);
            return sb.toString();

        }).collect(Collectors.joining("\n"));

        // let's handle a situation where entity type is one-2-one entity.
        if (isOneToOne(entityType)) {
            final StringBuilder sb = new StringBuilder();
            final String thatTableName = tableName((Class<? extends AbstractEntity<?>>) getKeyType(entityType));
            fkConstraint(dialect, thisTableName, "_ID", sb, thatTableName);

            return ddl + sb;
        }

        return ddl;
    }
    
    private void fkConstraint(final Dialect dialect, final String thisTableName, final String colName, final StringBuilder sb, final String thatTableName) {
        sb.append(format("ALTER TABLE %s ", thisTableName));
        sb.append("\n");
        sb.append(format("ADD CONSTRAINT FK_%s_%s FOREIGN KEY (%s) REFERENCES %s (_ID);", thisTableName, colName, colName, thatTableName));
        addGoIfApplicable(dialect, sb);
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