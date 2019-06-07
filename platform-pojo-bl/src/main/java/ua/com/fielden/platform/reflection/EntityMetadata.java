package ua.com.fielden.platform.reflection;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getAnnotation;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.stripIfNeeded;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isPropertyDescriptor;

import java.lang.reflect.Field;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.SkipEntityExistsValidation;
import ua.com.fielden.platform.entity.annotation.factory.EntityExistsAnnotation;
import ua.com.fielden.platform.entity.exceptions.EntityDefinitionException;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;
import ua.com.fielden.platform.reflection.exceptions.ReflectionException;

/**
 * A set of utility functions to obtain metadata that is required (mainly) to instantiating instrumented entities.
 * The main goal of these functions in comparison the approach that existed before, is the memoization of entity metadata for efficient reuse.
 *
 * @author TG Team
 *
 */
public class EntityMetadata {
    private static final Cache<Class<? extends AbstractEntity<?>>, Cache<String, Boolean>> CACHE_IS_ENTITY_EXISTS_APPLICABLE = CacheBuilder.newBuilder().initialCapacity(1000).concurrencyLevel(50).build();
    private static final Cache<Class<? extends AbstractEntity<?>>, Class<? extends Comparable>> CACHE_KEY_TYPE = CacheBuilder.newBuilder().initialCapacity(1000).concurrencyLevel(50).build();
    private static final Cache<Class<? extends AbstractEntity<?>>, Cache<String, Class<?>>> CACHE_PROP_TYPE = CacheBuilder.newBuilder().initialCapacity(1000).concurrencyLevel(50).build();
    private static final Cache<Class<? extends AbstractEntity<?>>, Cache<String, EntityExists>> CACHE_ENTITY_EXISTS_ANNOTATION = CacheBuilder.newBuilder().initialCapacity(1000).concurrencyLevel(50).build();
    private EntityMetadata() {}
    
//    public static void build(final IApplicationDomainProvider domainProvider) {
//        for (final Class<? extends AbstractEntity<?>> entityType : domainProvider.entityTypes()) {
//            keyTypeInfo(entityType);
//            final List<Field> fields = Finder.findRealProperties(entityType);
//            for (final Field field : fields) { // for each property field
//                determinePropType(entityType, field);
//                isEntityExistsValidationApplicable(entityType, field);
//            }
//        }
//    }

    /**
     * Determines the type of property KEY.
     *
     * @param entityType
     * @return
     */
    public static Class<? extends Comparable> keyTypeInfo(final Class<? extends AbstractEntity<?>> entityType) {
        try {
            return CACHE_KEY_TYPE.get(entityType, () -> {
                final Class<? extends Comparable> keyType = (Class<? extends Comparable>) AnnotationReflector.getKeyType(entityType);
                if (keyType == null) {
                    throw new EntityDefinitionException(format("Entity [%s] is not fully defined -- key type is missing.", entityType.getName()));
                }
                return keyType;
            });
        } catch (final ExecutionException ex) {
            throw new EntityDefinitionException(format("Exception while trying determine key type for entity [%s].", entityType.getName()), ex);
        }
    }

    /**
     * Determines property type.
     *
     * @param field
     * @return
     */
    public static Class<?> determinePropType(final Class<? extends AbstractEntity<?>> entityType, final Field field) {
       try {
           return CACHE_PROP_TYPE
                   .get(entityType, () -> CacheBuilder.newBuilder().initialCapacity(10).concurrencyLevel(50).build())
                   .get(field.getName(), () -> KEY.equals(field.getName()) ? keyTypeInfo(entityType) : stripIfNeeded(field.getType()));
        } catch (final ExecutionException ex) {
            throw new ReflectionException(format("Could not determine type for property [%s] of entity [%s].", field.getName(), entityType.getName()), ex);
        } 
    }

    /**
     * Determines whether entity exists validation is applicable for the provided type and property.
     * 
     * @param entityType
     * @param field -- represents property
     * @return
     */
    public static boolean isEntityExistsValidationApplicable(final Class<? extends AbstractEntity<?>> entityType, final Field field) {
        try {
            return CACHE_IS_ENTITY_EXISTS_APPLICABLE
                    .get(entityType, () -> CacheBuilder.newBuilder().initialCapacity(10).concurrencyLevel(50).build())
                    .get(field.getName(), () -> {
                        final Class<?> propType = determinePropType(entityType, field);
                        final SkipEntityExistsValidation seevAnnotation = getAnnotation(field, SkipEntityExistsValidation.class);
                        final boolean doNotSkipEntityExistsValidation = seevAnnotation == null || seevAnnotation.skipActiveOnly() || seevAnnotation.skipNew();
                        return doNotSkipEntityExistsValidation && (isPersistedEntityType(propType) || isPropertyDescriptor(propType) /*|| isEntityExistsForced(entityType, field)*/);                        
                    });
         } catch (final ExecutionException ex) {
             throw new ReflectionException(format("Could not determine applicability of EntityExists validation for property [%s] of entity [%s].", field.getName(), entityType.getName()), ex);
         } 
    }

    /**
     * Creates annotation instance of type {@link EntityExists}.
     * 
     * @param entityType
     * @param propName
     * @param propType
     * @return
     */
    public static EntityExists entityExistsAnnotation(final Class<? extends AbstractEntity<?>> entityType, final String propName, final Class<? extends AbstractEntity<?>> propType) {
        try {
            return CACHE_ENTITY_EXISTS_ANNOTATION
                    .get(entityType, () -> CacheBuilder.newBuilder().initialCapacity(10).concurrencyLevel(50).build())
                    .get(propName, () -> EntityExistsAnnotation.newInstance((Class<? extends AbstractEntity<?>>) propType));
         } catch (final ExecutionException ex) {
             throw new ReflectionException(format("Could not create EntityExists annotation for property [%s] of entity [%s].", propName, entityType.getName()), ex);
         } 
     }
}
