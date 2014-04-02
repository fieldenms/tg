package ua.com.fielden.platform.domaintree;

import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager.ILocatorDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;

/**
 * This interface defines how domain tree can be represented in the global client application scope. At this stage only the <i>default locator managers</i> can represented in
 * global client application scope. <br>
 * <br>
 * 
 * @author TG Team
 * 
 */
public interface IGlobalDomainTreeRepresentation {
    /**
     * Defines a contract which <i>entity-typed</i> properties have what locator manager by <b>default</b>. <br>
     * <br>
     * 
     * The method is concentrated on the "types" of properties that should be have locator managers assigned. If you want to change locator manager for "concrete" property type --
     * use {@link #excludeImmutably(Class, String)} method. <br>
     * <br>
     * 
     * Throws {@link IllegalArgumentException} when the property is not of {@link AbstractEntity} type.
     * 
     * @param propertyType
     *            -- a property type for which a default locator manager instance is asked.
     * @return
     */
    ILocatorDomainTreeManagerAndEnhancer getLocatorManagerByDefault(final Class<?> propertyType);

    /**
     * Sets a new locator manager by <b>default</b> for the specified property type. <br>
     * <br>
     * 
     * The method is concentrated on the "type" of property that should have locator managers assigned. If you want to get locator manager for "concrete" property type -- use
     * {@link #getLocatorManagerByDefault(Class)} method. <br>
     * <br>
     * 
     * Throws {@link IllegalArgumentException} when the property is not of {@link AbstractEntity} type. Throws {@link Result} when non-base user is trying to set default locator
     * (see {@link IGlobalDomainTreeManager#getUserProvider()}).
     * 
     * @param propertyType
     *            -- a property type for which a default locator manager instance is asked.
     * @param locatorManager
     *            -- a new locator manager to set
     * @return
     */
    IGlobalDomainTreeRepresentation setLocatorManagerByDefault(final Class<?> propertyType, final ILocatorDomainTreeManagerAndEnhancer locatorManager);
}
