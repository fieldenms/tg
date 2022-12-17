package ua.com.fielden.platform.entity.proxy;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static ua.com.fielden.platform.reflection.Finder.findFieldByName;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.isMockNotFoundType;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.exceptions.EntityException;
import ua.com.fielden.platform.entity.proxy.exceptions.MockException;

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
    public static final String COULD_NOT_SET_ERROR_MSG_ERR = "\"%s\" error message can not be set into [%s] field of [%s] mock entity.";
    public static final String COULD_NOT_ACCESS_ERROR_MSG_ERR = "Could not be access [%s] field of [%s] mock entity.";
    private static final String errorMsgPropName = "errorMessage_";

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
                return buddy.defineField(errorMsgPropName, String.class, Visibility.PRIVATE).name(entityType.getName() + MOCK_TYPE_ENDING).make()
                        .load(ClassLoader.getSystemClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                        .getLoaded();
            });
        } catch (final ExecutionException ex) {
            throw new EntityException(String.format("Could not create a mock-not-found type for entity [%s].", entityType.getName()), ex);
        }
    }

    /**
     * Set the specified errorMessage into errorMessage_ field of mock entity.
     *
     * @param <T>
     * @param mock
     * @param errMessage
     */
    public static <T extends AbstractEntity<?>> T setErrorMessage(final T mock, final String errMessage) {
        if (isMockNotFoundValue(mock)) {
            try {
                final Field msgField = findFieldByName(mock.getClass(), errorMsgPropName);
                final boolean isAccessible = msgField.canAccess(mock);
                msgField.setAccessible(true);
                msgField.set(mock, errMessage);
                msgField.setAccessible(isAccessible);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                throw new MockException(String.format(COULD_NOT_SET_ERROR_MSG_ERR, errMessage, errorMsgPropName, mock.getClass()));
            }
        }
        return mock;
    }

    /**
     * Retrieves error message from errorMessage_ field of mock entity.
     *
     * @param <T>
     * @param mock
     * @return
     */
    public static <T extends AbstractEntity<?>> Optional<String> getErrorMessage(final T mock) {
        if (isMockNotFoundValue(mock)) {
            try {
                final Field msgField = findFieldByName(mock.getClass(), errorMsgPropName);
                final boolean isAccessible = msgField.canAccess(mock);
                msgField.setAccessible(true);
                final Optional<?> msgVal = ofNullable(msgField.get(mock));
                msgField.setAccessible(isAccessible);
                return msgVal.map(Object::toString);
            } catch (final IllegalAccessException e) {
                throw new MockException(format(COULD_NOT_ACCESS_ERROR_MSG_ERR, errorMsgPropName, mock.getClass()));
            }
        }
        return empty();
    }
}
