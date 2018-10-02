package ua.com.fielden.platform.dao;

import static org.apache.commons.lang.StringUtils.isEmpty;
import static ua.com.fielden.platform.dao.EntityCategory.PERSISTED;
import static ua.com.fielden.platform.dao.EntityCategory.PURE;
import static ua.com.fielden.platform.dao.EntityCategory.QUERY_BASED;
import static ua.com.fielden.platform.dao.EntityCategory.UNION;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractUnionEntity.unionProperties;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getAnnotation;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader.getOriginalType;
import static ua.com.fielden.platform.utils.EntityUtils.getEntityModelsOfQueryBasedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isSyntheticEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFromAlias;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

public class BaseInfoForDomainMetadata {
    private final ConcurrentMap<Class<? extends AbstractEntity<?>>, EntityTypeInfo<?>> map = new ConcurrentHashMap<>();

    public <ET extends AbstractEntity<?>> String getTableClause(final Class<ET> entityType) {
        final EntityTypeInfo<ET> entityTypeInfo = getEntityTypeInfo(entityType);

        if (entityTypeInfo.category != PERSISTED) {
            return null;
        }

        final String providedTableName = entityTypeInfo.mapEntityToAnnotation.value();
        return !isEmpty(providedTableName) ? providedTableName : getOriginalType(entityType).getSimpleName().toUpperCase() + "_";
    }

    public <ET extends AbstractEntity<?>>  List<EntityResultQueryModel<ET>> getEntityModels(final Class<ET> entityType) {
        return getEntityTypeInfo(entityType).entityModels;
    }

    public <ET extends AbstractEntity<?>>  List<EntityResultQueryModel<ET>> getUnionEntityModels(final Class<ET> entityType) {
        return getEntityTypeInfo(entityType).unionEntityModels;
    }

    public EntityCategory getCategory(final Class<? extends AbstractEntity<?>> entityType) {
        return getEntityTypeInfo(entityType).category;
    }

    private <ET extends AbstractEntity<?>> EntityTypeInfo<ET> getEntityTypeInfo(final Class<ET> entityType) {
        final EntityTypeInfo<ET> existing = (EntityTypeInfo<ET>) map.get(entityType);
        if (existing != null) {
            return existing;
        } else {
            final EntityTypeInfo<ET> created = new EntityTypeInfo<>(entityType);
            map.put(entityType, created);
            return created;
        }
    }

    private static class EntityTypeInfo <ET extends AbstractEntity<?>> {
        final Class<ET> entityType;
        final EntityCategory category;
        final MapEntityTo mapEntityToAnnotation;
        final List<EntityResultQueryModel<ET>> entityModels;
        final List<EntityResultQueryModel<ET>> unionEntityModels;

        public EntityTypeInfo(final Class<ET> entityType) {
            this.entityType = entityType;
            mapEntityToAnnotation = getAnnotation(entityType, MapEntityTo.class);
            entityModels = getEntityModelsOfQueryBasedEntityType(entityType);
            unionEntityModels = produceUnionEntityModels(entityType);
            if (isPersistedEntityType(entityType)) {
                category = PERSISTED;
            } else if (isSyntheticEntityType(entityType)) {
                category = QUERY_BASED;
            } else if (isUnionEntityType(entityType)) {
                category = UNION;
            } else if (mapEntityToAnnotation == null && (entityModels.size() + unionEntityModels.size() == 0)) {
                category = PURE;
            } else {
                throw new IllegalStateException("Unable to determine entity type category for type: " + entityType + "!\n MapEntityToAnnotation: " +
            mapEntityToAnnotation + ";\n EntityModels.size: " + entityModels.size() +
            ";\n UnionModels.size: " + unionEntityModels.size());
            }
        }
        
        private <ET extends AbstractEntity<?>> List<EntityResultQueryModel<ET>> produceUnionEntityModels(final Class<ET> entityType) {
            final List<EntityResultQueryModel<ET>> result = new ArrayList<>();
            if (!isUnionEntityType(entityType)) {
                return result;
            }

            final List<Field> unionProps = unionProperties((Class<? extends AbstractUnionEntity>) entityType);
            for (final Field currProp : unionProps) {
                result.add(generateModelForUnionEntityProperty(unionProps, currProp).modelAsEntity(entityType));
            }
            return result;
        }
        
        private <PT extends AbstractEntity<?>> ISubsequentCompletedAndYielded<PT> generateModelForUnionEntityProperty(final List<Field> unionProps, final Field currProp) {
            final IFromAlias<PT> modelStart = select((Class<PT>) currProp.getType());
            ISubsequentCompletedAndYielded<PT> modelInProgress = null;
            for (final Field field : unionProps) {
                if (modelInProgress == null) {
                    modelInProgress = field.equals(currProp) ? modelStart.yield().prop(ID).as(field.getName()) : modelStart.yield().val(null).as(field.getName());
                } else {
                    modelInProgress = field.equals(currProp) ? modelInProgress.yield().prop(ID).as(field.getName()) : modelInProgress.yield().val(null).as(field.getName());
                }
            }

            return modelInProgress;
        }

    }
}
