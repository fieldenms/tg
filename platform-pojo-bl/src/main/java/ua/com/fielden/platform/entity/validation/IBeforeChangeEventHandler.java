package ua.com.fielden.platform.entity.validation;

import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.companion.IEntityReader;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.utils.IDates;

/**
 * Contract that should be implemented by any class to be used as an Entity property before change event handler.
 * <p>
 * <i><b>IMPORTANT:</b> Class implementing this interface must be thread-safe.</i><br/>
 * <i><b>IMPORTANT:</b> Class implementing this interface must not have any side-effects on the state of the entity being modified and property value being set.</i><br/>
 * <i><b>IMPORTANT:</b> If method {@link #handle(MetaProperty, Object, Set)} returns unsuccessful result then the new value is not set.</i>
 *
 * @author TG Team
 *
 */
public interface IBeforeChangeEventHandler<T> {

    /**
     * Processed Before Change Event (BCE) for a <code>property</code>.
     * <p>
     * Returns an instance of {@link Result}, which should indicate success or failure of the event handling, and may contain other context dependent information (e.g. exception
     * that might have occurred). Method {@link Result#isSuccessful()} should be used for quick evaluation of the handler result. If result is unsuccessful then the
     * <code>newValue</code> is not set.
     * <p>
     * This approach nicely fits into the concept of property value validation, where unsuccessful result is returned indicating the cause of the <code>newValue</code> not being
     * set.
     *
     * @param property
     *            -- meta-property for the entity property being set.
     * @param newValue
     *            -- a new value, which is a BCE handling subject; it gets assigned to an entity property if its validation succeeds.
     * @param mutatorAnnotations
     *            -- a set of annotations defined for a method representing a mutator changing property's value
     *
     * @return
     */
    Result handle(final MetaProperty<T> property, final T newValue, final Set<Annotation> mutatorAnnotations);

    /**
     * Returns a narrowed down to the reader contract companion object for the given entity type.
     * By default this method throws {@link UnsupportedOperationException} exception. 
     * 
     * @param type
     * @return
     */
    default <R extends IEntityReader<E>, E extends AbstractEntity<?>> R co(final Class<E> type) {
        throw new UnsupportedOperationException("This method is not implemented by default. Use [AbstractBeforeChangeEventHandler] as the base type to inherit the implementation.");
    }

    /**
     * Returns {@link ua.com.fielden.platform.utils.IDates} instance for dates API easy access.
     * By default this method throws {@link UnsupportedOperationException} exception.
     *
     * @return
     */
    default IDates dates() {
        throw new UnsupportedOperationException("This method is not implemented by default. Use [AbstractBeforeChangeEventHandler] as the base type to inherit the implementation.");
    }

}