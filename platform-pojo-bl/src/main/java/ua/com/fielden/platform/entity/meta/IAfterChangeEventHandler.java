package ua.com.fielden.platform.entity.meta;

import ua.com.fielden.platform.companion.IEntityReader;
import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * Provides a contract for handling After Change Events (ACE). It can be used for implementing a logic that assigns meta-property properties based on the value of the entity
 * property it is associated with. For example, {@link MetaProperty} has property <code>editable</code> the value for which should be determined based on some custom logic that
 * could use a value of the associated with this meta-property entity property.
 * <p>
 * <b>IMPORTANT:</b> <i>Implementations of this contract must be immutable due to caching of handler instances for reuse on multiple entities.</i>
 *
 * @author TG Team
 *
 */
public interface IAfterChangeEventHandler<T> {
    
    /**
     * Should implement ACE handling logic.
     *
     * @param property
     *            -- meta property associated with a property, which is the source of ACE
     * @param entityPropertyValue
     *            -- the current value of the property
     */
    void handle(final MetaProperty<T> property, final T entityPropertyValue);
    
    /**
     * Returns a narrowed down to the reader contract companion object for the given entity type.
     * By default this method throws {@link UnsupportedOperationException} exception. 
     * 
     * @param type
     * @return
     */
    default <R extends IEntityReader<E>, E extends AbstractEntity<?>> R co(final Class<E> type) {
        throw new UnsupportedOperationException("This method is not implemented by default. Use [AbstractAfterChangeEventHandler] as the base type to inherit the implementation.");
    }
}
