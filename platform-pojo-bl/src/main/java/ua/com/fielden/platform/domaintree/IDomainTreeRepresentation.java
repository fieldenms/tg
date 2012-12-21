package ua.com.fielden.platform.domaintree;

import java.util.List;
import java.util.Set;

/**
 * This interface defines how domain tree can be represented. <br><br>
 *
 * Domain tree consists of a tree of properties.
 * Each property has two "tick" representations (refer to {@link ITickRepresentation}), which include tick disablement, checking and so on.<br><br>
 *
 * <b>Important:</b> it is necessary to override {@link #equals(Object)} and {@link #hashCode()} methods in implementors to provide logical comparison of instances. <br><br>
 *
 * The major aspects of tree representation are following: <br>
 *  1. property immutable excluding;<br>
 *  2. property available functions;<br>
 *  3. tick-specific rules (refer to {@link ITickRepresentation} for more details)
 *
 * @author TG Team
 *
 */
public interface IDomainTreeRepresentation extends IRootTyped {
    /**
     * Returns a tree representation for a first tick. Includes tick disablement, checking logic and so on.
     */
    ITickRepresentation getFirstTick();

    /**
     * Returns a tree representation for a second tick. Includes tick disablement, checking logic and so on.
     */
    ITickRepresentation getSecondTick();

    /**
     * Defines a contract which properties should be <b>immutably</b> excluded from domain tree representation. <br><br>
     *
     * The method should be mainly concentrated on the "classes" of properties that should be excluded (based on i.e. types, nature, parents, annotations assigned).
     * If you want to exclude "concrete" property -- use {@link #excludeImmutably(Class, String)} method. <br><br>
     *
     * <b>IMPORTANT</b> : the excluded <b>immutably</b> properties could not be included anymore.
     *
     * @param root -- a root type that contains property.
     * @param property -- a dot-notation expression that defines a property (empty property defines an entity itself).
     *
     * @return
     */
    boolean isExcludedImmutably(final Class<?> root, final String property);

    /**
     * Marks a concrete property to be <b>immutably</b> excluded from domain tree representation. <br><br>
     *
     * The method should be mainly concentrated on "concrete" properties that should be excluded.
     * If you want to define which "classes" of properties should be excluded (based on i.e. types, nature, parents, annotations assigned) --
     * use {@link #isExcludedImmutably(Class, String)} method. <br><br>
     *
     * <b>IMPORTANT</b> : the excluded <b>immutably</b> properties could not be included anymore.
     *
     * @param root -- a root type that contains property.
     * @param property -- a dot-notation expression that defines a property (empty property defines an entity itself).
     *
     */
    IDomainTreeRepresentation excludeImmutably(final Class<?> root, final String property);

    /**
     * Returns an immutable <b>ordered</b> list of included properties for concrete <code>root</code> type. <br><br>
     *
     * The list fully reflects {@link #isExcludedImmutably(Class, String)} contract (+manually excluded properties) and defines an order
     * of properties -- currently the order is ["key" or key members => "desc" (if exists) => other properties in order as declared in domain]. <br><br>
     *
     * <b>Important:</b> the list also contains two types of "dummy" properties, to be closer to Entity Tree structure: <br>
     * 1. [prop1.prop2...propN.dummy-property] -- a marker for not loaded children of circular-reference property, <b>will be deleted after children will be loaded</b>.<br>
     * 2. [prop1.prop2...propN.common-properties.propN+1.propN+2...propM] -- a property under union-entity's common property, <b>should be treated as [prop1.prop2...propN.propN+1.propN+2...propM] according to union-entities naming contract</b><br>
     *
     * @param root -- a root type that contains included properties.
     * @return
     */
    List<String> includedProperties(final Class<?> root);

    /**
     * A post-successful listener for property addition / removal.
     *
     * @author TG Team
     *
     */
    public interface IPropertyListener extends IPropertyStateListener<Boolean> {
	/**
	 * @param wasAddedOrRemoved -- <code>true</code> to indicate that property was successfully added, <code>false</code> to indicate that it was successfully removed.
	 */
	@Override
	void propertyStateChanged(final Class<?> root, final String property, final Boolean wasAddedOrRemoved, final Boolean oldState);

	boolean isInternal();
    }

    /**
     * Adds a {@link IPropertyListener} listener.
     *
     * @param listener
     * @return
     */
    boolean addPropertyListener(final IPropertyListener listener);

    /**
     * Removes a {@link IPropertyListener} listener.
     *
     * @param listener
     * @return
     */
    boolean removePropertyListener(final IPropertyListener listener);

    /**
     * The structure of properties in case of circular references can be "not loaded" to some level of properties.
     * The method tries to load missing tree branch of "included properties" ({@link #includedProperties(Class)} contract).
     *
     * Example : "Vehicle.replacing.dummy-property" has been loaded, but "Vehicle.replacing.replacedBy.status" should be selected.
     * This method should be used to load missing properties.
     *
     * <b>Important:</b> the method should work with properties with "dummy" naming contract (like {@link #includedProperties(Class)} method), that includes: <br>
     * 1. [prop1.prop2...propN.dummy-property] -- a marker for not loaded children of circular-reference property, <b>will be deleted after children will be loaded</b>.<br>
     * 2. [prop1.prop2...propN.common-properties.propN+1.propN+2...propM] -- a property under union-entity's common property, <b>should be treated as [prop1.prop2...propN.propN+1.propN+2...propM] according to union-entities naming contract</b><br>
     *
     * @param root -- a root type that contains property.
     * @param property -- a dot-notation expression that defines a property to be "warmed up"
     *
     */
    IDomainTreeRepresentation warmUp(final Class<?> root, final String property);

    /**
     * Defines a contract for what properties have which functions available. If no functions are available -- the calculated properties could not be created.<br><br>
     *
     * Throws {@link IllegalArgumentException} if this contract conflicts with excluded properties contract.
     *
     * @param root -- a root type that contains property.
     * @param property -- a dot-notation expression that defines a property (empty property defines an entity itself).
     *
     * @return
     */
    Set<Function> availableFunctions(final Class<?> root, final String property);

    /**
     * This interface defines how domain tree "tick" can be represented. <br><br>
     *
     * Domain tree consists of a tree of properties.
     * Each property has several "tick" representations.<br><br>
     *
     * <b>Important:</b> it is necessary to override {@link #equals(Object)} and {@link #hashCode()} methods in implementors to provide logical comparison of instances. <br><br>
     *
     * The major aspects of tree tick representation are following: <br><br>
     *  1. property's tick <b>immutable</b> disablement;<br>
     *  2. property's tick <b>immutable</b> checking;<br>
     *
     * @author TG Team
     *
     */
    public interface ITickRepresentation {
        /**
         * Defines a contract which ticks for which properties should be <b>immutably</b> disabled in domain tree representation. <br><br>
         *
         * This contract should not conflict with "excluded properties" contract. The conflict will produce an {@link IllegalArgumentException}.<br><br>
         *
         * The method should be mainly concentrated on the "classes" of property's ticks that should be disabled (based on i.e. types, nature, parents, annotations assigned).
         * If you want to disable "concrete" property's tick -- use {@link #disableImmutably(Class, String)} method. <br><br>
         *
         * <b>IMPORTANT</b> : the disabled <b>immutably</b> property's ticks could not be enabled anymore.
         *
         * @param root -- a root type that contains property.
         * @param property -- a dot-notation expression that defines a property (empty property defines an entity itself).
         *
         * @return
         */
        boolean isDisabledImmutably(final Class<?> root, final String property);

        /**
         * Marks a concrete property's tick to be <b>immutably</b> disabled in domain tree representation. <br><br>
         *
         * This action should not conflict with "excluded properties" contract. The conflict will produce an {@link IllegalArgumentException}.<br><br>
         *
         * The method should be mainly concentrated on "concrete" property's ticks that should be disabled.
         * If you want to define which "classes" of property's ticks should be disabled (based on i.e. types, nature, parents, annotations assigned) --
         * use {@link #isDisabledImmutably(Class, String)} method. <br><br>
         *
         * <b>IMPORTANT</b> : the disabled <b>immutably</b> property's ticks could not be enabled anymore.
         *
         * @param root -- a root type that contains property.
         * @param property -- a dot-notation expression that defines a property (empty property defines an entity itself).
         *
         */
        ITickRepresentation disableImmutably(final Class<?> root, final String property);

        /**
         * A post-successful listener for property disablement.
         *
         * @author TG Team
         *
         */
        public interface IPropertyDisablementListener extends IPropertyStateListener<Boolean> {
	    /**
	     * @param hasBeenDisabled -- <code>true</code> to indicate that property was successfully disabled, <code>false</code> to indicate that it was successfully enabled.
	     */
	    @Override
	    void propertyStateChanged(final Class<?> root, final String property, final Boolean hasBeenDisabled, final Boolean oldState);
        }

        /**
         * Adds a {@link IPropertyDisablementListener} listener.
         *
         * @param listener
         * @return
         */
        boolean addPropertyDisablementListener(final IPropertyDisablementListener listener);

        /**
         * Removes a {@link IPropertyDisablementListener} listener.
         *
         * @param listener
         * @return
         */
        boolean removePropertyDisablementListener(final IPropertyDisablementListener listener);

        /**
         * Defines a contract which ticks for which properties should be <b>immutably</b> checked (and automatically disabled!) in domain tree representation. <br><br>
         *
         * This contract should not conflict with "excluded properties" contract. The conflict will produce an {@link IllegalArgumentException}.<br><br>
         *
         * The method should be mainly concentrated on the "classes" of property's ticks that should be checked (based on i.e. types, nature, parents, annotations assigned).
         * If you want to check "concrete" property's tick -- use {@link #checkImmutably(Class, String)} method. <br><br>
         *
         * <b>IMPORTANT</b> : the checked (and disabled) <b>immutably</b> property's ticks could not be unchecked (and enabled) anymore.
         *
         * @param root -- a root type that contains property.
         * @param property -- a dot-notation expression that defines a property (empty property defines an entity itself).
         *
         * @return
         */
        boolean isCheckedImmutably(final Class<?> root, final String property);

        @Override
        public boolean equals(Object obj);

        @Override
        public int hashCode();
    }

    @Override
    public boolean equals(Object obj);

    @Override
    public int hashCode();

    /**
     * A base listener interface for state "changes" of the property (e.g. property added / removed / checked / disabled / etc.).
     *
     * @author TG Team
     *
     * @param <T>
     */
    public interface IPropertyStateListener<T> {
	/**
	 * Invokes after successful "change" of property state (e.g. added / removed / checked / disabled / etc.).
	 *
	 * @param root
	 * @param property
	 * @param newState
	 * @param oldState
	 */
	void propertyStateChanged(final Class<?> root, final String property, final T newState, final T oldState);
    }
}
