package ua.com.fielden.platform.domaintree;

import java.util.List;

/**
 * This interface defines how domain tree can be managed. <br><br>
 *
 * Domain tree consists of a tree of properties. <br><br>
 *
 * <b>Important:</b> it is necessary to override {@link #equals(Object)} and {@link #hashCode()} methods in implementors to provide logical comparison of instances. <br><br>
 *
 * 1. The domain can be enhanced by <i>calculated properties</i> using {@link IDomainTreeEnhancer} instance ({@link #getEnhancer()} method).<br>
 * 2. Each property has two "tick" managers (refer to {@link ITickManager}), which include tick checking logic etc.<br>
 * 3. The rules of tree representation (properties disablement, immutable checking etc.) can be changed by {@link IDomainTreeRepresentation} instance ({@link #getRepresentation()} method).<br>
 *
 * @author TG Team
 *
 */
public interface IDomainTreeManager {
    /**
     * Returns a domain representation that is able to change domain representation rules. See {@link IDomainTreeRepresentation} documentation for more details.
     *
     * @return
     */
    IDomainTreeRepresentation getRepresentation();

    /**
     * A <i>domain tree manager<i> with <i>enhancer</i> inside.
     *
     * @author TG Team
     *
     */
    public interface IDomainTreeManagerAndEnhancer extends IDomainTreeManager, IEnhancerRetriever {
    }

    /**
     * A retriever interface for <i>domain tree enhancer<i>.
     *
     * @author TG Team
     *
     */
    public interface IEnhancerRetriever {
	/**
	 * Returns a domain tree enhancer that is able to modify domain. See {@link IDomainTreeEnhancer} documentation for more details.
	 *
	 * @return
	 */
	IDomainTreeEnhancer getEnhancer();
    }

    /**
     * Returns a tree manager for a first tick. Includes tick mutable checking & functions logic.
     */
    ITickManager getFirstTick();

    /**
     * Returns a tree manager for a second tick. Includes tick mutable checking & functions logic.
     */
    ITickManager getSecondTick();

    /**
     * This interface defines how domain tree can be managed. <br><br>
     * Domain tree consists of a tree of properties.
     * Each property has two true-false modifiers.<br><br>
     *
     * <b>Important:</b> it is necessary to override {@link #equals(Object)} and {@link #hashCode()} methods in implementors to provide logical comparison of instances. <br><br>
     *
     * The major aspects of tree management (context-specific) are following: <br>
     *  1. property's tick checking;<br>
     *
     * @author TG Team
     *
     */
    public interface ITickManager {
        /**
         * Defines a contract which ticks for which properties should be <b>mutably</b> checked in domain tree representation. <br><br>
         *
         * This contract should not conflict with "excluded / disabled properties" contract. The conflict will produce an {@link IllegalArgumentException}.<br><br>
         *
         * The method should be mainly concentrated on the "classes" of property's ticks that should be checked (based on i.e. types, nature, parents, annotations assigned).
         * If you want to check "concrete" property's tick -- use {@link #check(Class, String)} method. <br><br>
         *
         * @param root -- a root type that contains property.
         * @param property -- a dot-notation expression that defines a property (empty property defines an entity itself).
         *
         * @return
         */
        boolean isChecked(final Class<?> root, final String property);

        /**
         * Marks a concrete property's tick to be <b>mutably</b> checked in domain tree representation. <br><br>
         *
         * The action should not conflict with a contract of disabled / checked property's ticks. The conflict will produce an {@link IllegalArgumentException}.
         *
         * @param root -- a root type that contains property.
         * @param property -- a dot-notation expression that defines a property.
         * @param check -- an action to perform (<code>true</code> to check, <code>false</code> to uncheck)
         *
         */
        ITickManager check(final Class<?> root, final String property, final boolean check);

        /**
         * Returns an <b>ordered</b> list of checked properties for concrete <code>root</code> type. <br><br>
         *
         * The order of the checked properties should be following (if it was not altered using {@link #swap(Class, String, String)}/{@link #move(Class, String, String)} methods):<br>
         * 1. all checked properties as defined by a) {@link #isChecked(Class, String)} contract b) {@link IDomainTreeRepresentation#includedProperties(Class)} order<br>
         * 2. all manually checked properties (in order that they were checked)
         *
         * @param root -- a root type that contains a checked properties.
         * @return
         */
        List<String> checkedProperties(final Class<?> root);

        /**
         * A post-successful listener for property checking.
         *
         * @author TG Team
         *
         */
        public interface IPropertyCheckingListener {
	    /**
	     * @param hasBeenChecked -- <code>true</code> to indicate that property was successfully checked, <code>false</code> to indicate that it was successfully unchecked.
	     * @param index -- the <code>index</code> of the property being checked / unchecked inside "checking properties" list.
	     */
	    void propertyStateChanged(final Class<?> root, final String property, final Boolean hasBeenChecked, final Boolean oldState, final int index);
        }

        /**
         * Adds a {@link IPropertyCheckingListener} listener.
         *
         * @param listener
         * @return
         */
        boolean addPropertyCheckingListener(final IPropertyCheckingListener listener);

        /**
         * Removes a {@link IPropertyCheckingListener} listener.
         *
         * @param listener
         * @return
         */
        boolean removePropertyCheckingListener(final IPropertyCheckingListener listener);

        /**
         * Swaps two properties (<code>property1</code> and <code>property2</code>) in an ordered list of checked properties for concrete <code>root</code> type.
         *
         * @param root -- a root type that contains a checked properties.
         * @param property1 -- a first property to swap
         * @param property2 -- a second property to swap
         */
        ITickManager swap(final Class<?> root, final String property1, final String property2);

        /**
         * Moves property <code>what</code> to the place <b>before</b> property <code>beforeWhat</code> in an ordered list of checked properties for concrete <code>root</code> type.
         *
         * @param root -- a root type that contains a checked properties.
         * @param what -- a property to move
         * @param beforeWhat -- a property before which property "what" will be inserted
         */
        ITickManager move(final Class<?> root, final String what, final String beforeWhat);

        /**
         * Moves property <code>what</code> to the place after all properties in an ordered list of checked properties for concrete <code>root</code> type.
         *
         * @param root -- a root type that contains a checked properties.
         * @param what -- a property to move
         */
        ITickManager moveToTheEnd(final Class<?> root, final String what);

        @Override
        public boolean equals(Object obj);

        @Override
        public int hashCode();
    }

    @Override
    public boolean equals(Object obj);

    @Override
    public int hashCode();
}