package ua.com.fielden.platform.entity.meta;

/**
 * An abstract implementation of {@link IAfterChangeEventHandler} that provides a helper method that looks for a property and returns its method.
 * 
 * @author 01es
 * 
 */
public abstract class AbstractMetaPropertyDefiner<T> implements IAfterChangeEventHandler<T> {

    protected <T> T findValue(final Object[] state, final String[] propertyNames, final String propertyName, final Class<T> type) {
        for (int index = 0; index < propertyNames.length; index++) {
            if (propertyName.equals(propertyNames[index])) {
                return type.cast(state[index]);
            }
        }
        return null;
    }
}
