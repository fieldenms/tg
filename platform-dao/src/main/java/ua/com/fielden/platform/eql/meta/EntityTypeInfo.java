package ua.com.fielden.platform.eql.meta;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static ua.com.fielden.platform.entity.query.metadata.DomainMetadataUtils.produceUnionEntityModels;
import static ua.com.fielden.platform.eql.meta.EntityCategory.PERSISTENT;
import static ua.com.fielden.platform.eql.meta.EntityCategory.PURE;
import static ua.com.fielden.platform.eql.meta.EntityCategory.QUERY_BASED;
import static ua.com.fielden.platform.eql.meta.EntityCategory.UNION;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getAnnotation;
import static ua.com.fielden.platform.reflection.Finder.getKeyMembers;
import static ua.com.fielden.platform.utils.EntityUtils.isCompositeEntity;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.ImmutableList;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.reflection.exceptions.ReflectionException;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.EntityUtils;

/**
 * Contains core entity type level metadata.
 * 
 * @param <ET>
 * 
 * @author TG Team
 */
public class EntityTypeInfo <ET extends AbstractEntity<?>> {

    /** A map of {@code EntityTypeInfo} for entity types. Only plain (not generated) entity types are included. Generated entity types have the same {@code EntityTypeInfo} as their originals. */
    private static final ConcurrentMap<Class<? extends AbstractEntity<?>>, EntityTypeInfo<? extends AbstractEntity<?>>> ENTITY_TYPE_TO_ENTITY_TYPE_INFO = new ConcurrentHashMap<>();
    
    public final EntityCategory category;
    public final String tableName;
    public final List<T2<String, Class<?>>> compositeKeyMembers;
    public final List<EntityResultQueryModel<ET>> entityModels;

    private EntityTypeInfo(final Class<ET> entityType) {
        if (isPersistedEntityType(entityType)) {
            tableName = getTableClause(entityType);
            category = PERSISTENT;
            entityModels = ImmutableList.of();
        } else {
            final var synModelField = EntityUtils.findSyntheticModelFieldFor(entityType);
            if (synModelField != null) {
                tableName = null;
                category = QUERY_BASED;
                entityModels = getEntityModelsOfQueryBasedEntityType(entityType, synModelField);
            } else if (isUnionEntityType(entityType)) {
                tableName = null;
                category = UNION;
                entityModels = ImmutableList.copyOf(produceUnionEntityModels(entityType));
            } else {
                tableName = null;
                category = PURE;
                entityModels = ImmutableList.of();
            }
        }
 
        compositeKeyMembers = isCompositeEntity(entityType) ? ImmutableList.copyOf(getCompositeKeyMembers(entityType)) : ImmutableList.of();
    }

    /**
     * Returns a list of query models, defined for {@code entityType} as static field {@code modelField}.
     *
     * @param entityType
     * @return
     */
    private static <T extends AbstractEntity<?>> List<EntityResultQueryModel<T>> getEntityModelsOfQueryBasedEntityType(final Class<T> entityType, final Field modelField) {
        final List<EntityResultQueryModel<T>> result = new ArrayList<>();
        try {
            final var name = modelField.getName();
            modelField.setAccessible(true);
            final Object value = modelField.get(null);
            if ("model_".equals(name)) {
                result.add((EntityResultQueryModel<T>) value);
            } else {
                result.addAll((List<EntityResultQueryModel<T>>) modelField.get(null));
            }
            return unmodifiableList(result);
        } catch (final Exception ex) {
            if (ex instanceof ReflectionException) {
                throw (ReflectionException) ex;
            } else {
                throw new ReflectionException("Could not obtain the model for synthetic entity [%s].".formatted(entityType.getSimpleName()), ex);
            }
        }
    }

    public static <T extends AbstractEntity<?>> EntityTypeInfo<? super T> getEntityTypeInfo(final Class<T> type) {
        final var originalType = DynamicEntityClassLoader.getOriginalType(type);
        return (EntityTypeInfo<? super T>) ENTITY_TYPE_TO_ENTITY_TYPE_INFO.computeIfAbsent(originalType, EntityTypeInfo::new);
    }

    public static <T extends AbstractEntity<?>> EntityTypeInfoPair<T> getEntityTypeInfoPair(final Class<T> type) {
        return new EntityTypeInfoPair<T>(type, getEntityTypeInfo(type));
    }

    /**
     * Derives a table name for the entity type.
     *  
     * @param type
     * @return
     */
    private static String getTableClause(final Class<? extends AbstractEntity<?>> entityType) {
        try {
            final MapEntityTo mapEntityToAnnotation = getAnnotation(entityType, MapEntityTo.class);
            if (mapEntityToAnnotation == null || isEmpty(mapEntityToAnnotation.value())) {
                return entityType.getSimpleName().toUpperCase() + "_";
            } else {
                return mapEntityToAnnotation.value();
            }
        } catch (final Exception ex) {
            throw new EqlException(format("Could not determine table name for entity [%s].", entityType.getSimpleName()), ex);
        }
    }

    private List<T2<String, Class<?>>> getCompositeKeyMembers(final Class<ET> entityType) {
        return getKeyMembers(entityType).stream().map(f -> T2.<String, Class<?>>t2(f.getName(), f.getType())).collect(toList());
    }
    
    public static record EntityTypeInfoPair<ET extends AbstractEntity<?>> (Class<ET> entityType, EntityTypeInfo<? super ET> entityTypeInfo) {}
}