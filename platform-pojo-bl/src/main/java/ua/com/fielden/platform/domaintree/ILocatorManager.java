package ua.com.fielden.platform.domaintree;

import java.util.List;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager.ILocatorDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.master.IMasterDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.Pair;

/**
 * {@link ILocatorManager} represents a manager for property related locators within some box-like entity
 * (for e.g. entity-centre {@link ICentreDomainTreeManagerAndEnhancer} or entity-master {@link IMasterDomainTreeManager}).
 * There can be one locator associated with some {@link AbstractEntity}-typed property.
 * <p>
 * There are three <b>phases</b> of locator lifecycle: <br>
 * 1. <b>Usage</b> phase -- at this phase locator can be used for reading (don't forget to <b>refresh</b> it every time before usage and then <b>discard</b>)<br>
 * 2. <b>Editing</b> phase -- at this phase locator can be edited until it will be <b>accepted / discarded</b><br>
 * 3. <b>Freezed Editing</b> phase -- at this phase locator has been <b>freezed</b> and can be edited until it will be <b>accepted / discarded</b><br>
 * <p>
 * Current locator can be accessed using method <b>get</b>. At the very beginning of locator history it will be <code>null</code>.
 * After first <b>refresh</b> locator will be initialised from Global Representation. <b>Refresh</b> method also will override everytime it
 * by fresh instance from Global Representation until it will become <i>LOCAL</i>.
 * <p>
 * Any action will throw {@link IllegalArgumentException} when the property is not of {@link AbstractEntity} type.
 *
 * @author TG Team
 *
 */
public interface ILocatorManager extends IRootTyped {
    /**
     * Refreshes a current version of locator manager and starts <b>Editing</b> phase.
     * <p>
     * In <b>Usage</b> phase it will override only <i>Global</i> locator by new one from Global Representation ("refresh Global" action).<br>
     * In <b>Editing</b> phase it is not applicable.<br>
     * In <b>Freezed Editing</b> phase it is not applicable.
     *
     * @param root -- a root type that contains property.
     * @param property -- a dot-notation expression that defines a property.
     * @return
     */
    void refreshLocatorManager(final Class<?> root, final String property);

    /**
     * Resets a current version of locator manager to default in <b>Usage</b> phase.
     * <p>
     * In <b>Usage</b> phase it will override <i>Local</i> locator to <code>null</code> (<i>Global</i> locator will stay <code>null</code>).<br>
     * In <b>Editing</b> phase it is not applicable.<br>
     * In <b>Freezed Editing</b> phase it is not applicable.
     *
     * @param root -- a root type that contains property.
     * @param property -- a dot-notation expression that defines a property.
     * @return
     */
    void resetLocatorManagerToDefault(final Class<?> root, final String property);

    /**
     * Accepts the changes of the current version of locator.
     * <p>
     * In <b>Usage</b> phase it is not applicable.<br>
     * In <b>Editing</b> phase it will accept <i>LOCALLY</i> locator changes after <b>Editing</b> phase has been started and move to <b>Usage</b> phase.<br>
     * In <b>Freezed Editing</b> phase it will accept locator changes after <b>Freezed Editing</b> phase has been started and move to <b>Editing</b> phase.
     *
     * @param root -- a root type that contains property.
     * @param property -- a dot-notation expression that defines a property.
     * @return
     */
    void acceptLocatorManager(final Class<?> root, final String property);

    /**
     * Discards the changes of the current version of locator.
     * <p>
     * In <b>Usage</b> phase it is not applicable.<br>
     * In <b>Editing</b> phase it will discard locator changes after <b>Editing</b> phase has been started and move to <b>Usage</b> phase.<br>
     * In <b>Freezed Editing</b> phase it will discard locator changes after <b>Freezed Editing</b> phase has been started and move to <b>Editing</b> phase.
     *
     * @param root -- a root type that contains property.
     * @param property -- a dot-notation expression that defines a property.
     * @return
     */
    void discardLocatorManager(final Class<?> root, final String property);

    /**
     * Promotes a current (perhaps not accepted) version of locator to global "type-related" configuration (without any additional actions like <b>accept / discard</b>).
     * <p>
     * In <b>Usage</b> phase it is not applicable.<br>
     * In <b>Editing</b> phase it will promote a current version of locator to global "type-related" configuration using {@link IGlobalDomainTreeRepresentation#setLocatorManagerByDefault(Class, ILocatorDomainTreeManager)} method.<br>
     * In <b>Freezed Editing</b> phase it is not applicable.
     *
     * @param root -- a root type that contains property.
     * @param property -- a dot-notation expression that defines a property.
     * @return
     */
    void saveLocatorManagerGlobally(final Class<?> root, final String property);

    /**
     * Freezes a current version of a locator.
     * <p>
     * In <b>Usage</b> phase it is not applicable.<br>
     * In <b>Editing</b> phase it will freeze a current version of locator and move it to <b>Freezed Editing</b> phase.<br>
     * In <b>Freezed Editing</b> phase it is not applicable.
     *
     * @param root -- a root type that contains property.
     * @param property -- a dot-notation expression that defines a property.
     * @return
     */
    void freezeLocatorManager(final Class<?> root, final String property);

    //////////////////////////////////////////////////////////////////////////////
    ////////////////////////////// STATE MANAGEMENT //////////////////////////////
    //////////////////////////////////////////////////////////////////////////////
    /**
     * Returns a current version of a locator manager.
     * <p>
     * In <b>Usage</b> phase please don't forget to <b>refresh</b> it every time before usage to get fresh instance and then <b>discard</b>.
     *
     * @param root -- a root type that contains property.
     * @param property -- a dot-notation expression that defines a property.
     * @return
     */
    ILocatorDomainTreeManagerAndEnhancer getLocatorManager(final Class<?> root, final String property);

    /**
     * A phases of locator lifecycle.
     *
     * @author TG Team
     *
     */
    public enum Phase {
	/**
	 * A phase in which locator can only be read and not changed (should be <b>refreshed</b> everytime before usage).
	 */
	USAGE_PHASE,
	/**
	 * A phase in which locator can be changed.
	 */
	EDITING_PHASE,
	/**
	 * A phase in which locator is "freezed" and can be changed.
	 */
	FREEZED_EDITING_PHASE
    }

    /**
     * Locator type by means of its "origination" (LOCAL or GLOBAL).
     *
     * @author TG Team
     *
     */
    public enum Type {
	/**
	 * A type of the current version of locator that has been accepted locally at least once.
	 */
	LOCAL,
	/**
	 * A type of the current version of locator that has never been accepted locally (has been originated from Global Representation).
	 */
	GLOBAL
    }

    /**
     * Returns {@link Phase} and {@link Type} of the current version of locator.
     *
     * @param root -- a root type that contains property.
     * @param property -- a dot-notation expression that defines a property.
     * @return
     */
    Pair<Phase, Type> phaseAndTypeOfLocatorManager(final Class<?> root, final String property);

    /**
     * Returns <code>true</code> if the current version of locator has been changed since last <b>accept / discard</b> has been performed (or since
     * the beginning of locator history).
     * <p>
     * In <b>Usage</b> phase it will return <code>false</code>.<br>
     * In <b>Editing</b> phase it will return <code>true</code> if locator has been changed since <b>Editing</b> phase has been started.<br>
     * In <b>Freezed Editing</b> phase it will return <code>true</code> if locator has been changed since <b>Freezed Editing</b> phase has been started.
     *
     * @param root -- a root type that contains property.
     * @param property -- a dot-notation expression that defines a property.
     * @return
     */
    boolean isChangedLocatorManager(final Class<?> root, final String property);

    /**
     * Returns a list of existent locator keys, that can be used in {@link #getLocatorManager(Class, String)} method.
     *
     * @return
     */
    List<Pair<Class<?>, String>> locatorKeys();
}
