package ua.com.fielden.platform.entity;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jakarta.inject.Singleton;
import ua.com.fielden.platform.entity.exceptions.EntityDefinitionException;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;

@Singleton
public class CachingEntityTypeVerifier extends EntityTypeVerifier {

    private final Cache<Class<?>, Object> verifiedTypesCache = CacheBuilder.newBuilder()
            .maximumSize(4096)
            .initialCapacity(512)
            .weakKeys()
            .build();

    @Override
    public void verify(final Class<? extends AbstractEntity<?>> entityType)
            throws EntityDefinitionException
    {
        // For enhanced types that do not contain structural enhancements, "strip" them by using the original type.
        // This should not be done for structurally enhanced types, which may be enhanced with additional properties to be verified.
        final var origEntityType = (Class<? extends AbstractEntity<?>>) PropertyTypeDeterminator.stripIfNeeded(entityType);

        if (verifiedTypesCache.getIfPresent(origEntityType) != null) {
            return;
        }

        super.verify(origEntityType);

        verifiedTypesCache.put(origEntityType, true);
    }

}
