package ua.com.fielden.platform.domaintree.centre;

import java.util.List;

import ua.com.fielden.platform.domaintree.IDomainTreeRepresentation.IPropertyStateListener;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.utils.Pair;

/**
 * This interface defines how domain tree "tick" can be managed for base "add to result" functionality. <br>
 * <br>
 * 
 * The major aspects of tree management (context-specific) are following: <br>
 * 1. property's ordering;<br>
 * 
 * @author TG Team
 * 
 */
public interface IOrderingManager {
    /**
     * Returns a list of <b>ordered</b> properties (columns) for concrete <code>root</code> type.
     * 
     * @param root
     *            -- a root type that contains an <b>ordered</b> properties.
     * @return
     */
    List<Pair<String, Ordering>> orderedProperties(final Class<?> root);

    /**
     * Toggles an <i>ordering</i> of a result property by following convention: [... => ASC => DESC => unordered => ASC => ...]<br>
     * <br>
     * 
     * This action should not conflict with "checked properties" (or with "used properties" -- more accurately) contract. The conflict will produce an
     * {@link IllegalArgumentException}.<br>
     * <br>
     * 
     * @param root
     *            -- a root type that contains property.
     * @param property
     *            -- a dot-notation expression that defines a property.
     * @param ordering
     *            -- an ordering to set
     * @return -- a result tick representation
     */
    IOrderingManager toggleOrdering(final Class<?> root, final String property);

    /**
     * A post-successful listener for property ordering.
     * 
     * @author TG Team
     * 
     */
    public interface IPropertyOrderingListener extends IPropertyStateListener<List<Pair<String, Ordering>>> {
        /**
         * @param newOrderedProperties
         *            -- a full new list of ordered properties after the change of the ordering at property [root, property].
         */
        @Override
        void propertyStateChanged(final Class<?> root, final String property, final List<Pair<String, Ordering>> newOrderedProperties, final List<Pair<String, Ordering>> oldState);
    }

    /**
     * Adds a {@link IPropertyOrderingListener} listener.
     * 
     * @param listener
     * @return
     */
    void addPropertyOrderingListener(final IPropertyOrderingListener listener);

    /**
     * Adds a weak {@link IPropertyOrderingListener} listener.
     * 
     * @param listener
     */
    void addWeakPropertyOrderingListener(final IPropertyOrderingListener listener);

    /**
     * Removes a {@link IPropertyOrderingListener} listener.
     * 
     * @param listener
     * @return
     */
    void removePropertyOrderingListener(final IPropertyOrderingListener listener);
}
