package ua.com.fielden.platform.entity.query.metadata;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static ua.com.fielden.platform.entity.query.metadata.DomainMetadataUtils.produceUnionEntityModels;
import static ua.com.fielden.platform.entity.query.metadata.EntityCategory.PERSISTED;
import static ua.com.fielden.platform.entity.query.metadata.EntityCategory.PURE;
import static ua.com.fielden.platform.entity.query.metadata.EntityCategory.QUERY_BASED;
import static ua.com.fielden.platform.entity.query.metadata.EntityCategory.UNION;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getAnnotation;
import static ua.com.fielden.platform.reflection.Finder.getKeyMembers;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader.getOriginalType;
import static ua.com.fielden.platform.utils.EntityUtils.getEntityModelsOfQueryBasedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isCompositeEntity;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isSyntheticEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

import java.util.List;

import com.google.common.collect.ImmutableList;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.types.tuples.T2;

public class EntityTypeInfo <ET extends AbstractEntity<?>> {
    public final Class<ET> entityType;
    public final EntityCategory category;
    public final String tableName;
    public final List<T2<String, Class<?>>> compositeKeyMembers;
    public final List<EntityResultQueryModel<ET>> entityModels;
    public final List<EntityResultQueryModel<ET>> unionEntityModels;

    public EntityTypeInfo(final Class<ET> entityType) {
        this.entityType = entityType;
        tableName = getTableClause(entityType);
        if (isPersistedEntityType(entityType)) {
            category = PERSISTED;
            entityModels = ImmutableList.of();
            unionEntityModels = ImmutableList.of();
        } else if (isSyntheticEntityType(entityType)) {
            category = QUERY_BASED;
            entityModels = ImmutableList.copyOf(getEntityModelsOfQueryBasedEntityType(entityType));
            unionEntityModels = ImmutableList.of();
        } else if (isUnionEntityType(entityType)) {
            category = UNION;
            entityModels = ImmutableList.of();
            unionEntityModels = ImmutableList.copyOf(produceUnionEntityModels(entityType));
        } else {
            category = PURE;
            entityModels = ImmutableList.of();
            unionEntityModels = ImmutableList.of();
        }
        
        compositeKeyMembers = isCompositeEntity(entityType) ? ImmutableList.copyOf(getCompositeKeyMembers(entityType)) : ImmutableList.of();
    }

    private String getTableClause(final Class<ET> entityType) {
        final MapEntityTo mapEntityToAnnotation = getAnnotation(entityType, MapEntityTo.class);
        if (mapEntityToAnnotation == null) {
            return null;
        }

        final String providedTableName = mapEntityToAnnotation.value();
        return !isEmpty(providedTableName) ? providedTableName : getOriginalType(entityType).getSimpleName().toUpperCase() + "_";
    }
    
    private List<T2<String, Class<?>>> getCompositeKeyMembers(final Class<ET> entityType) {
        return getKeyMembers(entityType).stream().map(f -> T2.<String, Class<?>>t2(f.getName(), f.getType())).collect(toList());
    }
}