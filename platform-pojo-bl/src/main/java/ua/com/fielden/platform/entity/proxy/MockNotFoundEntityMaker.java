package ua.com.fielden.platform.entity.proxy;

import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.isMockNotFoundType;

import java.util.concurrent.ExecutionException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.exceptions.EntityException;

/**
 * 
 * A class that represents runtime type information for entity types that represent mock-not-found values. 
 * Factory method {@link #mock(Class)} should be used to create mock-not-found entity types.
 * Method {@link #isMockNotFoundValue(AbstractEntity)} should be used to identify whether an entity instance is of a mock-not-found type.
 *
 * @author TG Team
 */
public class MockNotFoundEntityMaker {

    private static final Cache<String, Class<? extends AbstractEntity<?>>> TYPES = CacheBuilder.newBuilder().weakKeys().initialCapacity(10).maximumSize(100).concurrencyLevel(50).build();
    public static final String MOCK_TYPE_ENDING = "_MOCK_VALUE_NOT_FOUND";

    private MockNotFoundEntityMaker() { }

    public static long cleanUp() {
        TYPES.cleanUp();
        return TYPES.size();
    }

    /**
     * A predicate to identify whether {@code entity} is a mock-not-found instance. 
     *
     * @param entity
     * @return
     */
    public static <T extends AbstractEntity<?>> boolean isMockNotFoundValue(final T entity) {
        return isMockNotFoundType(entity.getClass());
    }

    /**
     * Returns either cached or a newly created mock-not-found type for the specified {@code entityType}.
     * This method ensures that if mock-not-found is passed then the same value is returned -- no mocks on mocks.
     *
     * @param entityType
     * @return
     */
    public static <T extends AbstractEntity<?>> Class<? extends T> mock(final Class<T> entityType) {
        // already a mock?
        if (isMockNotFoundType(entityType)) {
            return entityType;
        }
        // other wise get a mock...
        try {
            return (Class<? extends T>) TYPES.get(entityType.getName(), () -> {
                final Builder<T> buddy = new ByteBuddy().subclass(entityType);
                return buddy.name(entityType.getName() + MOCK_TYPE_ENDING).make()
                        .load(ClassLoader.getSystemClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                        .getLoaded();
            });
        } catch (final ExecutionException ex) {
            throw new EntityException(String.format("Could not create a mock-not-found type for entity [%s].", entityType.getName()), ex);
        }
    }
}
