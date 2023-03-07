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
 * <p>
 * FIXME The current implementation relies on the fact that {@link AbstractEntity} has property {@code desc} to capture string values, entered by users.
         However, property {@code desc} will be removed from {@link AbstractEntity} at some stage in the future.
         A separate, specifically generated property to hold values typed by users should be introduced instead (https://github.com/fieldenms/tg/issues/1933).

 * @author TG Team
 */
public class MockNotFoundEntityMaker {

    private static final Cache<String, Class<? extends AbstractEntity<?>>> TYPES = CacheBuilder.newBuilder().weakKeys().initialCapacity(10).maximumSize(100).concurrencyLevel(50).build();
    public static final String MOCK_TYPE_ENDING = "_MOCK_VALUE_NOT_FOUND";
    private static final String ERR_COULD_NOT_SET = "\"%s\" error message can not be set into [%s] field of [%s] mock entity.";
    private static final String ERR_COULD_NOT_ACCESS = "Could not access [%s] field of [%s] mock entity.";
    private static final String PROP_NAME_HOLDING_ERROR_MSG = "errorMessage_";

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
                return buddy
                        .name(entityType.getName() + MOCK_TYPE_ENDING)
                        .defineField(PROP_NAME_HOLDING_ERROR_MSG, String.class, Visibility.PRIVATE)
                        .make()
                        .load(ClassLoader.getSystemClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                        .getLoaded();
            });
        } catch (final ExecutionException ex) {
            throw new EntityException(format("Could not create a mock-not-found type for entity [%s].", entityType.getName()), ex);
        }
    }

    /**
     * Assigns {@code errorMsg} to the {@code PROP_NAME_HOLDING_ERROR_MSG} field of entity {@code mock}.
     *
     * @param <T>
     * @param mock
     * @param errorMsg
     */
    public static <T extends AbstractEntity<?>> T setErrorMessage(final T mock, final String errorMsg) {
        if (isMockNotFoundValue(mock)) {
            try {
                final Field msgField = findFieldByName(mock.getClass(), PROP_NAME_HOLDING_ERROR_MSG);
                final boolean isAccessible = msgField.canAccess(mock);
                msgField.setAccessible(true);
                msgField.set(mock, errorMsg);
                msgField.setAccessible(isAccessible);
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                throw new MockException(format(ERR_COULD_NOT_SET, errorMsg, PROP_NAME_HOLDING_ERROR_MSG, mock.getClass()), ex);
            }
        }
        return mock;
    }

    /**
     * Retrieves error message from field {@code PROP_NAME_HOLDING_ERROR_MSG} of {@code mock}.
     *
     * @param <T>
     * @param mock
     * @return
     */
    public static <T extends AbstractEntity<?>> Optional<String> getErrorMessage(final T mock) {
        if (isMockNotFoundValue(mock)) {
            try {
                final Field msgField = findFieldByName(mock.getClass(), PROP_NAME_HOLDING_ERROR_MSG);
                final boolean isAccessible = msgField.canAccess(mock);
                msgField.setAccessible(true);
                final Optional<String> msgVal = ofNullable((String)msgField.get(mock));
                msgField.setAccessible(isAccessible);
                return msgVal;
            } catch (final IllegalAccessException ex) {
                throw new MockException(format(ERR_COULD_NOT_ACCESS, PROP_NAME_HOLDING_ERROR_MSG, mock.getClass()), ex);
            }
        }
        return empty();
    }
}
