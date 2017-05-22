package ua.com.fielden.platform.eql.dbschema;

import static ua.com.fielden.platform.entity.AbstractEntity.DESC;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getPropertyAnnotation;
import static ua.com.fielden.platform.reflection.Finder.findRealProperties;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.Set;

import org.hibernate.dialect.Dialect;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.PersistentType;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.utils.EntityUtils;

public class TableDefinition {
    private final Class<? extends AbstractEntity<?>> entityType;
    private final Set<ColumnDefinition> columns;

    public TableDefinition(final ColumnDefinitionExtractor columnDefinitionExtractor, final Class<? extends AbstractEntity<?>> entityType) {
        this.entityType = entityType;
        this.columns = populateColumns(columnDefinitionExtractor, entityType);
    }

    private Set<ColumnDefinition> populateColumns(final ColumnDefinitionExtractor columnDefinitionExtractor, final Class<? extends AbstractEntity<?>> entityType) {
        //                PersistedEntity entity = new PersistedEntity(entityType.getName(), getAnnotation(entityType, MapEntityTo.class).value());
        //                result.add(entity);
        //
        //                entity.getFinalProps().add(new FinalProperty(false, ID, "_ID", Long.class, resolveToSqlType(Long.class), 0, 0, 0, null));
        //                entity.getFinalProps().add(new FinalProperty(false, VERSION, "_VERSION", Long.class, resolveToSqlType(Long.class), 0, 0, 0, null));
        //                // TODO need to determine DESC property from annotations
        //                Class<? extends Comparable> keyType = getKeyType(entityType);
        //                if (!DynamicEntityKey.class.equals(keyType)) {
        //                    // TODO need to take into account the case of key column override
        //                    MapTo mt = columnFromProperty(entityType, KEY);
        //                    entity.getFinalProps().add(new FinalProperty(false, KEY, "KEY_", keyType, resolveToSqlType(keyType), mt.length(), mt.scale(), mt.precision(), mt.defaultValue()));
        //                }
        final Set<ColumnDefinition> columns = new LinkedHashSet<>();
 
        
        columnDefinitionExtractor.extractIdProperty(entityType)
        .map(id -> columns.add(id));
        columns.add(columnDefinitionExtractor.extractVersionProperty());
        
        for (final Field propField : findRealProperties(entityType, MapTo.class)) {
            if (!shouldIgnore(propField)) {
                final MapTo mapTo = getPropertyAnnotation(MapTo.class, entityType, propField.getName());
                final PersistentType persistedType = getPropertyAnnotation(PersistentType.class, entityType, propField.getName());
                final boolean required = PropertyTypeDeterminator.isRequiredByDefinition(propField, entityType);
                columns.addAll(columnDefinitionExtractor.extractFromProperty(propField.getName(), propField.getType(), mapTo, persistedType, required));
            }
        }

        return columns;
    }

    private boolean shouldIgnore(final Field propField) {
        return KEY.equals(propField.getName()) || DESC.equals(propField.getName()) && EntityUtils.hasDescProperty(entityType);
    }

    /**
     * Generates DDL statement for a table (without constraints or indices) based on provided RDBMS dialect.
     * 
     * @param dialect
     * @return
     */
    public String schemaString(final Dialect dialect) {
        final StringBuilder sb = new StringBuilder();
        for (final ColumnDefinition columnProperty : columns) {
            sb.append(columnProperty.schemaString(dialect) + "\n");
        }

        return sb.toString();
    }
}