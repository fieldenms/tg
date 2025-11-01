package ua.com.fielden.platform.reflection;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.SkipEntityExistsValidation;
import ua.com.fielden.platform.entity.annotation.factory.EntityExistsAnnotation;
import ua.com.fielden.platform.entity.exceptions.EntityDefinitionException;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;
import ua.com.fielden.platform.entity.validation.annotation.SupportsEntityExistsValidation;
import ua.com.fielden.platform.reflection.exceptions.ReflectionException;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getAnnotation;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.stripIfNeeded;
import static ua.com.fielden.platform.utils.EntityUtils.*;

/// A set of utility functions to obtain metadata that is required (mainly) to instantiating instrumented entities.
/// The main goal of these functions in comparison the approach that existed before, is the memoization of entity metadata for efficient reuse.
///
public class EntityMetadata {
    private static final Logger LOGGER = getLogger(EntityMetadata.class);

    private static final Cache<Class<? extends AbstractEntity<?>>, Cache<String, Boolean>> CACHE_IS_ENTITY_EXISTS_APPLICABLE = CacheBuilder.newBuilder().initialCapacity(1000).concurrencyLevel(50).build();
    private static final Cache<Class<? extends AbstractEntity<?>>, Class<? extends Comparable<?>>> CACHE_KEY_TYPE = CacheBuilder.newBuilder().initialCapacity(1000).concurrencyLevel(50).build();
    private static final Cache<Class<? extends AbstractEntity<?>>, Cache<String, Class<?>>> CACHE_PROP_TYPE = CacheBuilder.newBuilder().initialCapacity(1000).concurrencyLevel(50).build();
    private static final Cache<Class<? extends AbstractEntity<?>>, EntityExists> CACHE_ENTITY_EXISTS_ANNOTATION = CacheBuilder.newBuilder().initialCapacity(500).concurrencyLevel(50).build();

    public static final String MSG_ENTITY_METADATA_BUILT = "Entity metadata built: entities [%s], properties [%s], entity exists annotations [%s].";
    public static final String ERR_KEY_TYPE_IS_MISSING = "Entity [%s] is not fully defined -- key type is missing.";
    public static final String ERR_TRYING_DETERMINE_KEY_TYPE = "Exception while trying determine key type for entity [%s].";
    public static final String ERR_DETERMINING_PROPERTY_TYPE = "Could not determine type for property [%s] of entity [%s].";
    public static final String ERR_ENTITY_EXISTS_VALIDATION_APPLICABILITY = "Could not determine applicability of EntityExists validation for property [%s] of entity [%s].";
    public static final String ERR_INSTANTIATING_ENTITY_EXISTS_ANNOTATION = "Could not create EntityExists annotation for [%s.%s].";

    private EntityMetadata() {}

    /// Builds metadata for domain entities.
    /// Doing this at the application startup time should result in improved performance related to accessing entity metadata.
    ///
    public static void build(final IApplicationDomainProvider domainProvider) {
        long typeCount = 0;
        long propCount = 0;
        long eevCount = 0;
        for (final Class<? extends AbstractEntity<?>> entityType : domainProvider.entityTypes()) {
            keyTypeInfo(entityType);
            final List<Field> fields = Finder.findRealProperties(entityType);
            for (final Field field : fields) { // for each property field
                propCount++;
                final Class<?> propType = determinePropType(entityType, field);
                if (isEntityExistsValidationApplicable(entityType, field)) {
                    entityExistsAnnotation(entityType, field.getName(), (Class<? extends AbstractEntity<?>>) propType);
                    eevCount++;
                }
            }
            typeCount++;
        }
        LOGGER.info(MSG_ENTITY_METADATA_BUILT.formatted(typeCount, propCount, eevCount));
    }

    /// Determines the type of property KEY.
    ///
    public static Class<? extends Comparable<?>> keyTypeInfo(final Class<? extends AbstractEntity<?>> entityType) {
        try {
            return CACHE_KEY_TYPE.get(entityType, () -> {
                final Class<? extends Comparable<?>> keyType = AnnotationReflector.getKeyType(entityType);
                if (keyType == null) {
                    throw new EntityDefinitionException(ERR_KEY_TYPE_IS_MISSING.formatted(entityType.getName()));
                }
                return keyType;
            });
        } catch (final ExecutionException ex) {
            throw new EntityDefinitionException(ERR_TRYING_DETERMINE_KEY_TYPE.formatted(entityType.getName()), ex.getCause());
        }
    }

    /// Determines property type.
    ///
    public static Class<?> determinePropType(final Class<? extends AbstractEntity<?>> entityType, final Field field) {
       try {
           return CACHE_PROP_TYPE
                   .get(entityType, () -> CacheBuilder.newBuilder().initialCapacity(10).concurrencyLevel(50).build())
                   .get(field.getName(), () -> KEY.equals(field.getName()) ? keyTypeInfo(entityType) : stripIfNeeded(field.getType()));
        } catch (final ExecutionException ex) {
            throw new ReflectionException(ERR_DETERMINING_PROPERTY_TYPE.formatted(field.getName(), entityType.getName()), ex.getCause());
        }
    }

    /// Determines whether entity exists validation is applicable for the provided type and property.
    ///
    public static boolean isEntityExistsValidationApplicable(final Class<? extends AbstractEntity<?>> entityType, final Field prop) {
        try {
            return CACHE_IS_ENTITY_EXISTS_APPLICABLE
                    .get(entityType, () -> CacheBuilder.newBuilder().initialCapacity(10).concurrencyLevel(50).build())
                    .get(prop.getName(), () -> {
                        final Class<?> propType = determinePropType(entityType, prop);
                        final SkipEntityExistsValidation seevAnnotation = getAnnotation(prop, SkipEntityExistsValidation.class);
                        final boolean doNotSkipEntityExistsValidation = seevAnnotation == null || seevAnnotation.skipActiveOnly() || seevAnnotation.skipNew();
                        return doNotSkipEntityExistsValidation && (isPersistentEntityType(propType) || isPropertyDescriptor(propType) || isSupportsEntityExistsValidation(propType) || isUnionEntityType(propType));
                    });
         } catch (final ExecutionException ex) {
             throw new ReflectionException(ERR_ENTITY_EXISTS_VALIDATION_APPLICABILITY.formatted(prop.getName(), entityType.getName()), ex.getCause());
         }
    }

    private static boolean isSupportsEntityExistsValidation(final Class<?> propType) {
        return getAnnotation(propType, SupportsEntityExistsValidation.class) != null;
    }

    public static EntityExists entityExistsAnnotation(final Class<? extends AbstractEntity<?>> entityType, final String propName, final Class<? extends AbstractEntity<?>> propType) {
        try {
            return CACHE_ENTITY_EXISTS_ANNOTATION.get(propType, () -> EntityExistsAnnotation.newInstance(propType));
         } catch (final ExecutionException ex) {
             throw new ReflectionException(ERR_INSTANTIATING_ENTITY_EXISTS_ANNOTATION.formatted(entityType.getSimpleName(), propName), ex.getCause());
         }
     }

}
