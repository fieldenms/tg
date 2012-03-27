package ua.com.fielden.platform.domaintree;

import java.util.List;

import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager.ILocatorDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.Pair;

/**
 * The manager for locators. The locator key is [<i>rootType; property</i>].
 *
 * @author TG Team
 *
 */
public interface ILocatorManager extends IRootTyped {
    /**
     * Produces a brand new <b>locator manager</b> for <i>entity-typed</i> property. The initialisation uses "default" type-related configuration (if exists) or raw instance creation.<br><br>
     *
     * (implementation note) : there should be two sets of locators : persistentLocators and currentLocators. save = [persistentLocators <= currentLocators]. discard =
     * [persistentLocators => currentLocators]. <br><br>
     *
     * This current version of a locator manager can be altered by its methods, and then saved ({@link #acceptLocatorManager(Class, String, boolean)} method) or discarded (
     * {@link #discardLocatorManager(Class, String, boolean)} method). <br><br>
     *
     * TODO Throws {@link IllegalArgumentException} when the property is not checked (see {@link #isChecked(Class, String)} method).<br>
     * Throws {@link IllegalArgumentException} when the property is not of {@link AbstractEntity} type.
     *
     * @param root -- a root type that contains property.
     * @param property -- a dot-notation expression that defines a property.
     * @return
     */
    ILocatorDomainTreeManagerAndEnhancer produceLocatorManagerByDefault(final Class<?> root, final String property);

    /**
     * Resets a current version of locator manager to <code>null</code>. <br><br>
     *
     * (implementation note) : there should be two sets of locators : persistentLocators and currentLocators. save = [persistentLocators <= currentLocators]. discard =
     * [persistentLocators => currentLocators]. <br><br>
     *
     * This current version of a locator manager can be altered by its methods, and then saved ({@link #acceptLocatorManager(Class, String, boolean)} method) or discarded (
     * {@link #discardLocatorManager(Class, String, boolean)} method). <br><br>
     *
     * TODO Throws {@link IllegalArgumentException} when the property is not checked (see {@link #isChecked(Class, String)} method).<br>
     * Throws {@link IllegalArgumentException} when the property is not of {@link AbstractEntity} type.
     *
     * @param root -- a root type that contains property.
     * @param property -- a dot-notation expression that defines a property.
     * @return
     */
    void resetLocatorManager(final Class<?> root, final String property);

    /**
     * Initialises a brand new <b>locator manager</b> for <i>entity-typed</i> property. The initialisation uses "default" type-related configuration (if exists) or raw instance creation.
     * If the manager was initialised before, it completely overrides a manager with a brand new instance. <br><br>
     *
     * (implementation note) : there should be two sets of locators : persistentLocators and currentLocators. save = [persistentLocators <= currentLocators]. discard =
     * [persistentLocators => currentLocators]. <br><br>
     *
     * This current version of a locator manager can be altered by its methods, and then saved ({@link #acceptLocatorManager(Class, String, boolean)} method) or discarded (
     * {@link #discardLocatorManager(Class, String, boolean)} method). <br><br>
     *
     * TODO Throws {@link IllegalArgumentException} when the property is not checked (see {@link #isChecked(Class, String)} method).<br>
     * Throws {@link IllegalArgumentException} when the property is not of {@link AbstractEntity} type.
     *
     * @param root -- a root type that contains property.
     * @param property -- a dot-notation expression that defines a property.
     * @return
     */
    void initLocatorManagerByDefault(final Class<?> root, final String property);

    /**
     * Discards a current version of a locator manager for <i>entity-typed</i> property.
     * If a current version of <b>locator manager</b> was freezed then it just "discards" the changes made after freezing.
     * <br><br>
     *
     * (implementation note) : there should be two sets of locators : persistentLocators and currentLocators. save = [persistentLocators <= currentLocators]. discard =
     * [persistentLocators => currentLocators]. <br><br>
     *
     * This current version of a locator manager can be altered by its methods, and then saved ({@link #acceptLocatorManager(Class, String, boolean)} method) or discarded (
     * {@link #discardLocatorManager(Class, String, boolean)} method). <br><br>
     *
     * TODO Throws {@link IllegalArgumentException} when the property is not checked (see {@link #isChecked(Class, String)} method).<br>
     * Throws {@link IllegalArgumentException} when the property is not of {@link AbstractEntity} type.
     *
     * @param root -- a root type that contains property.
     * @param property -- a dot-notation expression that defines a property.
     * @return
     */
    void discardLocatorManager(final Class<?> root, final String property);

    /**
     * Accepts a current version of a locator manager for <i>entity-typed</i> property.
     * If a current version of <b>locator manager</b> was freezed then it just "accepts" the current version.
     * <br><br>
     *
     * (implementation note) : there should be two sets of locators : persistentLocators and currentLocators. save = [persistentLocators <= currentLocators]. discard =
     * [persistentLocators => currentLocators]. <br><br>
     *
     * This current version of a locator manager can be altered by its methods, and then saved ({@link #acceptLocatorManager(Class, String, boolean)} method) or discarded (
     * {@link #discardLocatorManager(Class, String, boolean)} method). <br><br>
     *
     * TODO Throws {@link IllegalArgumentException} when the property is not checked (see {@link #isChecked(Class, String)} method).<br>
     * Throws {@link IllegalArgumentException} when the property is not of {@link AbstractEntity} type. <br><br>
     *
     * TODO : Throws {@link IllegalArgumentException} when this action is invoked by non-base user. Non-base users can change, discard and initialise its own locator (full usage), but it
     * can not be accepted ({@link #acceptLocatorManager(Class, String, boolean)} method). <br><br>
     *
     * @param root -- a root type that contains property.
     * @param property -- a dot-notation expression that defines a property.
     * @return
     */
    void acceptLocatorManager(final Class<?> root, final String property);

    /**
     * Saves a current version of a locator to default global "type-related" configuration (see {@link IGlobalDomainTreeRepresentation#setLocatorManagerByDefault(Class, ILocatorDomainTreeManager)} method)
     * for <i>entity-typed</i> property. <br><br>
     *
     * (implementation note) : there should be two sets of locators : persistentLocators and currentLocators. save = [persistentLocators <= currentLocators]. discard =
     * [persistentLocators => currentLocators]. <br><br>
     *
     * This current version of a locator manager can be altered by its methods, and then saved ({@link #acceptLocatorManager(Class, String, boolean)} method) or discarded (
     * {@link #discardLocatorManager(Class, String, boolean)} method). <br><br>
     *
     * TODO Throws {@link IllegalArgumentException} when the property is not checked (see {@link #isChecked(Class, String)} method).<br>
     * Throws {@link IllegalArgumentException} when the property is not of {@link AbstractEntity} type. <br><br>
     *
     * Throws {@link IllegalArgumentException} when this action is invoked by non-base user. Non-base users can change, discard and initialise its own locator (full usage), but it
     * can not be accepted ({@link #acceptLocatorManager(Class, String, boolean)} method). <br><br>
     *
     * @param root -- a root type that contains property.
     * @param property -- a dot-notation expression that defines a property.
     * @return
     */
    void saveLocatorManagerGlobally(final Class<?> root, final String property);

    /**
     * Gets a current version of a locator manager for <i>entity-typed</i> property. <br><br>
     *
     * (implementation note) : there should be two sets of locators : persistentLocators and currentLocators. save = [persistentLocators <= currentLocators]. discard =
     * [persistentLocators => currentLocators]. <br><br>
     *
     * This current version of a locator manager can be altered by its methods, and then saved ({@link #acceptLocatorManager(Class, String, boolean)} method) or discarded (
     * {@link #discardLocatorManager(Class, String, boolean)} method). <br><br>
     *
     * TODO Throws {@link IllegalArgumentException} when the property is not checked (see {@link #isChecked(Class, String)} method).<br>
     * Throws {@link IllegalArgumentException} when the property is not of {@link AbstractEntity} type.
     *
     * @param root -- a root type that contains property.
     * @param property -- a dot-notation expression that defines a property.
     * @return
     */
    ILocatorDomainTreeManagerAndEnhancer getLocatorManager(final Class<?> root, final String property);

    /**
     * Freezes a current version of a locator manager for <i>entity-typed</i> property. <br><br>
     *
     * (implementation note) : there should be two sets of locators : persistentLocators and currentLocators. save = [persistentLocators <= currentLocators]. discard =
     * [persistentLocators => currentLocators]. <br><br>
     *
     * This current version of a locator manager can be altered by its methods, and then saved ({@link #acceptLocatorManager(Class, String, boolean)} method) or discarded (
     * {@link #discardLocatorManager(Class, String, boolean)} method). <br><br>
     *
     * TODO Throws {@link IllegalArgumentException} when the property is not checked (see {@link #isChecked(Class, String)} method).<br>
     * Throws {@link IllegalArgumentException} when the property is not of {@link AbstractEntity} type.
     *
     * @param root -- a root type that contains property.
     * @param property -- a dot-notation expression that defines a property.
     * @return
     */
    void freezeLocatorManager(final Class<?> root, final String property);

    /**
     * Returns <code>true</code> if the current version of locator manager instance for <i>entity-typed</i> property is in freezed state.
     * Use {@link #discardLocatorManager(Class, String)} or {@link #acceptLocatorManager(Class, String)} to discard / accept the changes that were made after
     * freezing (these actions trigger automatic unfreezing after that).
     * <br><br>
     *
     * (implementation note) : there should be two sets of locators : persistentLocators and currentLocators. save = [persistentLocators <= currentLocators]. discard =
     * [persistentLocators => currentLocators]. <br><br>
     *
     * This current version of a locator manager can be altered by its methods, and then saved ({@link #acceptLocatorManager(Class, String, boolean)} method) or discarded (
     * {@link #discardLocatorManager(Class, String, boolean)} method). <br><br>
     *
     * TODO Throws {@link IllegalArgumentException} when the property is not checked (see {@link #isChecked(Class, String)} method).<br>
     * Throws {@link IllegalArgumentException} when the property is not of {@link AbstractEntity} type.
     *
     * @param root -- a root type that contains property.
     * @param property -- a dot-notation expression that defines a property.
     * @return
     */
    boolean isFreezedLocatorManager(final Class<?> root, final String property);

    /**
     * Returns <code>true</code> if the current version of locator manager instance for <i>entity-typed</i> property has been changed since last saving/discard (or since
     * the beginning of locator history). <br><br>
     *
     * (implementation note) : there should be two sets of locators : persistentLocators and currentLocators. save = [persistentLocators <= currentLocators]. discard =
     * [persistentLocators => currentLocators]. <br><br>
     *
     * This current version of a locator manager can be altered by its methods, and then saved ({@link #acceptLocatorManager(Class, String, boolean)} method) or discarded (
     * {@link #discardLocatorManager(Class, String, boolean)} method). <br><br>
     *
     * TODO Throws {@link IllegalArgumentException} when the property is not checked (see {@link #isChecked(Class, String)} method).<br>
     * Throws {@link IllegalArgumentException} when the property is not of {@link AbstractEntity} type.
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
