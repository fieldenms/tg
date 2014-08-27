package ua.com.fielden.platform.dao;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.utils.EntityUtils.getEntityModelsOfQueryBasedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFromAlias;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.security.user.User;

public class BaseInfoForDomainMetadata {
    private final MapEntityTo userMapTo;
    private final Map<Class<? extends AbstractEntity<?>>, EntityTypeInfo> map = new HashMap<>();
    
    public BaseInfoForDomainMetadata(MapEntityTo userMapTo) {
        super();
        this.userMapTo = userMapTo;
    }
    
    public <ET extends AbstractEntity<?>> String getTableClause(final Class<ET> entityType) {
        EntityTypeInfo<ET> entityTypeInfo = getEntityTypeInfo(entityType);
        
        if (!entityTypeInfo.category.equals(EntityCategory.PERSISTED)) {
            return null;
        }

        final String providedTableName = entityTypeInfo.mapEntityToAnnotation.value();
        if (!StringUtils.isEmpty(providedTableName)) {
            return providedTableName;
        } else {
            return DynamicEntityClassLoader.getOriginalType(entityType).getSimpleName().toUpperCase() + "_";
        }
    }
    
    public <ET extends AbstractEntity<?>>  List<EntityResultQueryModel<ET>> getEntityModels(final Class<ET> entityType) {
        return getEntityTypeInfo(entityType).entityModels;
    }
    
    public <ET extends AbstractEntity<?>>  List<EntityResultQueryModel<ET>> getUnionEntityModels(final Class<ET> entityType) {
        return getEntityTypeInfo(entityType).unionEntityModels;
    }

    private <ET extends AbstractEntity<?>> EntityTypeInfo<ET> getEntityTypeInfo(Class<ET> entityType) {
        EntityTypeInfo<ET> existing = map.get(entityType);
        if (existing != null) {
            return existing;
        } else {
            EntityTypeInfo<ET> created = new EntityTypeInfo<ET>(entityType);
            map.put(entityType, created);
            return created;
        }
    }
    
    public EntityCategory getCategory(Class<? extends AbstractEntity<?>> entityType) {
        return getEntityTypeInfo(entityType).category;
    }

    private <ET extends AbstractEntity<?>> List<EntityResultQueryModel<ET>> produceUnionEntityModels(final Class<ET> entityType) {
        final List<EntityResultQueryModel<ET>> result = new ArrayList<EntityResultQueryModel<ET>>();
        if (!isUnionEntityType(entityType)) {
            return result;
        }
        
        final List<Field> unionProps = AbstractUnionEntity.unionProperties((Class<? extends AbstractUnionEntity>) entityType);
        for (final Field currProp : unionProps) {
            result.add(generateModelForUnionEntityProperty(unionProps, currProp).modelAsEntity(entityType));
        }
        return result;
    }

    private <PT extends AbstractEntity<?>> ISubsequentCompletedAndYielded<PT> generateModelForUnionEntityProperty(final List<Field> unionProps, final Field currProp) {
        final IFromAlias<PT> modelInProgress = select((Class<PT>) currProp.getType());
        //  final ISubsequentCompletedAndYielded<PT> modelInProgress = select((Class<PT>) currProp.getType()).yield().prop("key").as("key");
        ISubsequentCompletedAndYielded<PT> m = null;
        for (final Field field : unionProps) {
            if (m == null) {
                m = field.equals(currProp) ? modelInProgress.yield().prop(AbstractEntity.ID).as(field.getName()) : modelInProgress.yield().val(null).as(field.getName());
            } else {
                m = field.equals(currProp) ? m.yield().prop(AbstractEntity.ID).as(field.getName()) : m.yield().val(null).as(field.getName());
            }
        }

        return m;
    }
    
    class EntityTypeInfo <ET extends AbstractEntity<?>> {
        final Class<ET> entityType;
        final EntityCategory category;
        final MapEntityTo mapEntityToAnnotation;
        final List<EntityResultQueryModel<ET>> entityModels;
        final List<EntityResultQueryModel<ET>> unionEntityModels;
        
        public EntityTypeInfo(Class<ET> entityType) {
            this.entityType = entityType;
            mapEntityToAnnotation = User.class == entityType ? userMapTo : AnnotationReflector.getAnnotation(entityType, MapEntityTo.class);
            entityModels = getEntityModelsOfQueryBasedEntityType(entityType);
            unionEntityModels = produceUnionEntityModels(entityType);
            if (mapEntityToAnnotation != null && (entityModels.size() + unionEntityModels.size() == 0)) {
                category = EntityCategory.PERSISTED;
            } else if (mapEntityToAnnotation == null && entityModels.size() > 0 && unionEntityModels.size() == 0) {
                category = EntityCategory.QUERY_BASED;
            } else if (mapEntityToAnnotation == null && entityModels.size() == 0 && unionEntityModels.size() > 0) {
                category = EntityCategory.UNION;
            } else if (mapEntityToAnnotation == null && (entityModels.size() + unionEntityModels.size() == 0)) {
                category = EntityCategory.PURE;
            } else {
                throw new IllegalStateException("Unable to determine entity type category for type: " + entityType + "!\n MapEntityToAnnotation: " + 
            mapEntityToAnnotation + ";\n EntityModels.size: " + entityModels.size() + 
            ";\n UnionModels.size: " + unionEntityModels.size());
            }
        }
    }
}
