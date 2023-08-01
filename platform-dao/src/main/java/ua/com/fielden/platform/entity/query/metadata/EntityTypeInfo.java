package ua.com.fielden.platform.entity.query.metadata;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static ua.com.fielden.platform.entity.query.metadata.DomainMetadataUtils.produceUnionEntityModels;
import static ua.com.fielden.platform.entity.query.metadata.EntityCategory.PERSISTENT;
import static ua.com.fielden.platform.entity.query.metadata.EntityCategory.PURE;
import static ua.com.fielden.platform.entity.query.metadata.EntityCategory.QUERY_BASED;
import static ua.com.fielden.platform.entity.query.metadata.EntityCategory.UNION;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getAnnotation;
import static ua.com.fielden.platform.reflection.Finder.getKeyMembers;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader.isGenerated;
import static ua.com.fielden.platform.utils.EntityUtils.isCompositeEntity;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.reflection.asm.impl.DynamicTypeNamingService;
import ua.com.fielden.platform.reflection.exceptions.ReflectionException;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.EntityUtils;

public class EntityTypeInfo <ET extends AbstractEntity<?>> {

    /** A cache of table names for entity class names, which includes both ordinary and generated entities. */
    private static final Cache<String, String> ENTITY_NAME_TO_TABLE_NAME = CacheBuilder.newBuilder().initialCapacity(500).maximumSize(1000).concurrencyLevel(50).build();
    private static final ConcurrentMap<String, EntityTypeInfo<?>> entityTypesInfos = new ConcurrentHashMap<>();
    public final Class<ET> entityType;
    public final EntityCategory category;
    public final String tableName;
    public final List<T2<String, Class<?>>> compositeKeyMembers;
    public final List<EntityResultQueryModel<ET>> entityModels;

    public EntityTypeInfo(final Class<ET> entityType) {
        this.entityType = entityType;
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
        final EntityTypeInfo<? super T> existing = (EntityTypeInfo<? super T>) entityTypesInfos.get(DynamicEntityClassLoader.getOriginalType(type).getName());
        if (existing != null) {
            return existing;
        } else {
            final EntityTypeInfo<T> parentInfo = new EntityTypeInfo<>(type);
//            if (parentInfo.category != PURE) {
                entityTypesInfos.put(type.getName(), parentInfo);
                return parentInfo;
//            }
        }
//        return null;
    }
    
    private static String getTableClause(final Class<? extends AbstractEntity<?>> entityType) {
        try {
            return ENTITY_NAME_TO_TABLE_NAME.get(entityType.getSimpleName(), () -> {
                    final MapEntityTo mapEntityToAnnotation = getAnnotation(entityType, MapEntityTo.class);
                    if (mapEntityToAnnotation == null || isEmpty(mapEntityToAnnotation.value())) {
                        return deriveTableName(entityType);
                    } else {
                        return mapEntityToAnnotation.value();
                    }
                }
            );
        } catch (final ExecutionException ex) {
            throw new EqlException(format("Could not determine table name for entity [%s].", entityType.getSimpleName()), ex);
        }
    }

    /**
     * Derives a table name for a entity by its type.
     * Uses a class name instead of a class loader to derive the table name.
     *  
     * @param type
     * @return
     */
    private static String deriveTableName(final Class<? extends AbstractEntity<?>> type) {
        final String template = "%s_";
        final String typeName = type.getSimpleName();
        if (isGenerated(type)) {
            final String originalTypeName = typeName.substring(0, typeName.indexOf(DynamicTypeNamingService.APPENDIX));
            return format(template, originalTypeName.toUpperCase());
        } else {
            return format(template, typeName.toUpperCase());
        }
    }
    
    private List<T2<String, Class<?>>> getCompositeKeyMembers(final Class<ET> entityType) {
        return getKeyMembers(entityType).stream().map(f -> T2.<String, Class<?>>t2(f.getName(), f.getType())).collect(toList());
    }
}