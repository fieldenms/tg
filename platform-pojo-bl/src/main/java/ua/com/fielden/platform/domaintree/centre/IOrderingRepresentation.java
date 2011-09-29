package ua.com.fielden.platform.domaintree.centre;

import java.util.List;

import ua.com.fielden.platform.domaintree.IDomainTreeRepresentation.ITickRepresentation;
import ua.com.fielden.platform.utils.Pair;

/**
 * This interface defines how domain tree "tick" can be represented for base "add to result" functionality. <br><br>
 *
 * The major aspects of tree tick representation are following: <br><br>
 *  1. property's ordering disablement (immutable);<br>
 *  2. property's ordering (by default);<br>
 *
 * @see ITickRepresentation
 * @author TG Team
 *
 */
public interface IOrderingRepresentation {
    /**
     * Represents an enumeration of the possible orderings ({@link #ASCENDING}, {@link #DESCENDING}).
     */
    public enum Ordering {
	/**
	 * Enumeration value indicating the items are sorted in increasing order. For example, the set <code>1, 4, 0</code> sorted in <code>ASCENDING</code> order is
	 * <code>0, 1, 4</code>.
	 */
	ASCENDING,

	/**
	 * Enumeration value indicating the items are sorted in decreasing order. For example, the set <code>1, 4, 0</code> sorted in <code>DESCENDING</code> order is
	 * <code>4, 1, 0</code>.
	 */
	DESCENDING
    }

    /**
     * Defines a contract which properties (columns) should be <b>immutably</b> disabled for <b>ordering</b> in domain tree representation. <br><br>
     *
     * This contract should not conflict with "excluded properties" contract. The conflict will produce an {@link IllegalArgumentException}.<br><br>
     *
     * The method should be mainly concentrated on the "classes" of properties that should be disabled for <b>ordering</b> (based on i.e. types, nature, parents, annotations assigned).
     * If you want to disable <b>ordering</b> for "concrete" property -- use {@link #disableOrderingImmutably(Class, String)} method. <br><br>
     *
     * <b>IMPORTANT</b> : the disabled <b>immutably</b> property's <b>ordering</b> could not be enabled anymore.
     *
     * @param root -- a root type that contains property.
     * @param property -- a dot-notation expression that defines a property (empty property defines an entity itself).
     *
     * @return
     */
    boolean isOrderingDisabledImmutably(final Class<?> root, final String property);

    /**
     * Marks a concrete property to be <b>immutably</b> disabled for <b>ordering</b> in domain tree representation. <br><br>
     *
     * This action should not conflict with "excluded properties" contract. The conflict will produce an {@link IllegalArgumentException}.<br><br>
     *
     * The method should be mainly concentrated on "concrete" properties that should be disabled.
     * If you want to define which "classes" of property's ticks should be disabled (based on i.e. types, nature, parents, annotations assigned) --
     * use {@link #isOrderingDisabledImmutably(Class, String)} method. <br><br>
     *
     * <b>IMPORTANT</b> : the disabled <b>immutably</b> property's <b>ordering</b> could not be enabled anymore.
     *
     * @param root -- a root type that contains property.
     * @param property -- a dot-notation expression that defines a property (empty property defines an entity itself).
     *
     */
    void disableOrderingImmutably(final Class<?> root, final String property);

    /**
     * Returns a list of <b>ordered</b> properties (columns) for concrete <code>root</code> type.
     *
     * This action should not conflict with "excluded properties" contract ([root + ""] should not be excluded). The conflict will produce an {@link IllegalArgumentException}.<br><br>
     *
     * @param root -- a root type that contains an <b>ordered</b> properties.
     * @return
     */
    List<Pair<String, Ordering>> orderedPropertiesByDefault(final Class<?> root);

    /**
     * Sets a list of <b>ordered</b> properties (columns) for concrete <code>root</code> type.
     *
     * This action should not conflict with "excluded properties" contract ([root + ""] should not be excluded). The conflict will produce an {@link IllegalArgumentException}.<br><br>
     *
     * @param root -- a root type that contains an <b>ordered</b> properties.
     * @return
     */
    void setOrderedPropertiesByDefault(final Class<?> root, final List<Pair<String, Ordering>> orderedPropertiesByDefault);
}
