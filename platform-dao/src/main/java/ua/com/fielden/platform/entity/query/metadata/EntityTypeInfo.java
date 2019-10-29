package ua.com.fielden.platform.entity.query.metadata;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static ua.com.fielden.platform.entity.query.metadata.DomainMetadataUtils.produceUnionEntityModels;
import static ua.com.fielden.platform.entity.query.metadata.EntityCategory.PERSISTED;
import static ua.com.fielden.platform.entity.query.metadata.EntityCategory.PURE;
import static ua.com.fielden.platform.entity.query.metadata.EntityCategory.QUERY_BASED;
import static ua.com.fielden.platform.entity.query.metadata.EntityCategory.UNION;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getAnnotation;
import static ua.com.fielden.platform.reflection.Finder.getKeyMembers;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader.isGenerated;
import static ua.com.fielden.platform.utils.EntityUtils.getEntityModelsOfQueryBasedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isCompositeEntity;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isSyntheticEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

import java.util.List;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.reflection.asm.impl.DynamicTypeNamingService;
import ua.com.fielden.platform.types.tuples.T2;

public class EntityTypeInfo <ET extends AbstractEntity<?>> {

    /** A cache of table names for entity class names, which includes both ordinary and generated entities. */
    private static final Cache<String, String> ENTITY_NAME_TO_TABLE_NAME = CacheBuilder.newBuilder().initialCapacity(500).maximumSize(1000).concurrencyLevel(50).build();
    
    public final Class<ET> entityType;
    public final EntityCategory category;
    public final String tableName;
    public final List<T2<String, Class<?>>> compositeKeyMembers;
    public final List<EntityResultQueryModel<ET>> entityModels;
    public final List<EntityResultQueryModel<ET>> unionEntityModels;

    public EntityTypeInfo(final Class<ET> entityType) {
        this.entityType = entityType;
        if (isPersistedEntityType(entityType)) {
            tableName = getTableClause(entityType);
            category = PERSISTED;
            entityModels = ImmutableList.of();
            unionEntityModels = ImmutableList.of();
        } else if (isSyntheticEntityType(entityType)) {
            tableName = null;
            category = QUERY_BASED;
            entityModels = ImmutableList.copyOf(getEntityModelsOfQueryBasedEntityType(entityType));
            unionEntityModels = ImmutableList.of();
        } else if (isUnionEntityType(entityType)) {
            tableName = null;
            category = UNION;
            entityModels = ImmutableList.of();
            unionEntityModels = ImmutableList.copyOf(produceUnionEntityModels(entityType));
        } else {
            tableName = null;
            category = PURE;
            entityModels = ImmutableList.of();
            unionEntityModels = ImmutableList.of();
        }
        
        compositeKeyMembers = isCompositeEntity(entityType) ? ImmutableList.copyOf(getCompositeKeyMembers(entityType)) : ImmutableList.of();
    }

    private String getTableClause(final Class<ET> entityType) {
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