package ua.com.fielden.platform.eql.dbschema;

import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static ua.com.fielden.platform.entity.AbstractEntity.DESC;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getPropertyAnnotation;
import static ua.com.fielden.platform.reflection.Finder.findRealProperties;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.dialect.Dialect;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.PersistentType;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.utils.EntityUtils;

public class TableDefinition {
    public final Class<? extends AbstractEntity<?>> entityType;
    private final Set<ColumnDefinition> columns;

    public TableDefinition(final ColumnDefinitionExtractor columnDefinitionExtractor, final Class<? extends AbstractEntity<?>> entityType) {
        this.entityType = entityType;
        this.columns = populateColumns(this, columnDefinitionExtractor, entityType);
    }

    private static Set<ColumnDefinition> populateColumns(final TableDefinition tableDefinition, final ColumnDefinitionExtractor columnDefinitionExtractor, final Class<? extends AbstractEntity<?>> entityType) {
        final Set<ColumnDefinition> columns = new LinkedHashSet<>();
        
        columns.add(columnDefinitionExtractor.extractIdProperty(tableDefinition));

        columnDefinitionExtractor.extractSimpleKeyProperty(tableDefinition)
        .map(key -> columns.add(key));
        
        columns.add(columnDefinitionExtractor.extractVersionProperty(tableDefinition));
        
        for (final Field propField : findRealProperties(entityType, MapTo.class)) {
            if (!shouldIgnore(propField, entityType)) {
                final MapTo mapTo = getPropertyAnnotation(MapTo.class, entityType, propField.getName());
                final PersistentType persistedType = getPropertyAnnotation(PersistentType.class, entityType, propField.getName());
                final boolean required = PropertyTypeDeterminator.isRequiredByDefinition(propField, entityType);
                columns.addAll(columnDefinitionExtractor.extractFromProperty(tableDefinition, propField.getName(), propField.getType(), mapTo, persistedType, required));
            }
        }

        return columns;
    }

    private static boolean shouldIgnore(final Field propField, final Class<? extends AbstractEntity<?>> entityType) {
        return KEY.equals(propField.getName()) || DESC.equals(propField.getName()) && EntityUtils.hasDescProperty(entityType);
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
        return sb.toString();
    }

    /**
     * Generates a DDL statement to add a primary key constraint on column <code>_ID</code>.
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
        return sb.toString();
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