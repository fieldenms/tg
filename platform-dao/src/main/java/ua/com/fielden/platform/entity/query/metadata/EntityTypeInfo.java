package ua.com.fielden.platform.entity.query.metadata;

import static java.util.Collections.unmodifiableList;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static ua.com.fielden.platform.entity.query.metadata.DomainMetadataUtils.produceUnionEntityModels;
import static ua.com.fielden.platform.entity.query.metadata.EntityCategory.PERSISTED;
import static ua.com.fielden.platform.entity.query.metadata.EntityCategory.PURE;
import static ua.com.fielden.platform.entity.query.metadata.EntityCategory.QUERY_BASED;
import static ua.com.fielden.platform.entity.query.metadata.EntityCategory.UNION;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getAnnotation;
import static ua.com.fielden.platform.reflection.Finder.getKeyMembers;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader.getOriginalType;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.EntityUtils.getEntityModelsOfQueryBasedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isCompositeEntity;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isSyntheticEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.types.tuples.T2;

public class EntityTypeInfo <ET extends AbstractEntity<?>> {
    public final Class<ET> entityType;
    public final EntityCategory category;
    public final String tableName;
    public final List<T2<String, Class<?>>> compositeKeyMembers = new ArrayList<>();
    private final List<EntityResultQueryModel<ET>> entityModels = new ArrayList<>();
    private final List<EntityResultQueryModel<ET>> unionEntityModels = new ArrayList<>();
    

    public EntityTypeInfo(final Class<ET> entityType) {
        this.entityType = entityType;
        tableName = getTableClause(entityType);
        if (isPersistedEntityType(entityType)) {
            category = PERSISTED;
        } else if (isSyntheticEntityType(entityType)) {
            category = QUERY_BASED;
            entityModels.addAll(getEntityModelsOfQueryBasedEntityType(entityType));
        } else if (isUnionEntityType(entityType)) {
            category = UNION;
            unionEntityModels.addAll(produceUnionEntityModels(entityType));
        } else {
            category = PURE;
        }
        
        if (isCompositeEntity(entityType)) {
            compositeKeyMembers.addAll(getCompositeKeyMembers(entityType));
        }
    }
    
    public List<EntityResultQueryModel<ET>> getEntityModels() {
        return unmodifiableList(entityModels);
    }

    public List<EntityResultQueryModel<ET>> getUnionEntityModels() {
        return unmodifiableList(unionEntityModels);
    }

    private String getTableClause(final Class<ET> entityType) {
        final MapEntityTo mapEntityToAnnotation = getAnnotation(entityType, MapEntityTo.class);
        if (mapEntityToAnnotation == null) {
            return null;
        }

        final String providedTableName = mapEntityToAnnotation.value();
        return !isEmpty(providedTableName) ? providedTableName : getOriginalType(entityType).getSimpleName().toUpperCase() + "_";
    }
    
    private List<T2<String, Class<?>>> getCompositeKeyMembers (final Class<ET> entityType) {
        final List<T2<String, Class<?>>> result = new ArrayList<>();
        for (Field field : getKeyMembers(entityType)) {
            result.add(t2(field.getName(), field.getType()));
        }
        return result;
    }
}