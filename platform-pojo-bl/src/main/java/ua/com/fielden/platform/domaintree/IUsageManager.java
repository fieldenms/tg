package ua.com.fielden.platform.domaintree;

import java.util.List;

import ua.com.fielden.platform.domaintree.IDomainTreeManager.ITickManager;
import ua.com.fielden.platform.domaintree.exceptions.DomainTreeException;

/**
 * This interface defines how domain tree tick {@link ITickManager} can be managed in aspect of 'used properties'.
 * <p> 
 * <b>Important:</b> it is necessary to override {@link #equals(Object)} and {@link #hashCode()} methods in implementors to provide logical comparison of instances.
 * <p>
 * Used properties include all properties that were 'used' from already checked properties (subset in checked properties).
 * If the property becomes 'used' explicitly by {@link #use(Class, String, boolean)} method, it places into the end of {@link #usedProperties(Class)} list.
 * The order of {@link #usedProperties(Class)} is important; custom order could be achieved by {@link #use(Class, String, boolean)} method with custom order
 * of invocation.
 * 
 * @author TG Team
 * 
 */
public interface IUsageManager {
    /**
     * Defines a contract which ticks for which properties should be <b>mutably</b> checked (used) in domain tree manager. The property should be checked to be able to be
     * "used".<br>
     * <br>
     * 
     * This contract should not conflict with "checked properties" contract. The conflict will produce an {@link DomainTreeException}.<br>
     * <br>
     * 
     * The method should be mainly concentrated on the "classes" of property's ticks that should be used (based on i.e. types, nature, parents, annotations assigned). If you
     * want to use "concrete" property's tick -- use {@link #use(Class, String)} method. <br>
     * <br>
     * 
     * @param root
     *            -- a root type that contains property.
     * @param property
     *            -- a dot-notation expression that defines a property (empty property defines an entity itself).
     * 
     * @return
     */
    boolean isUsed(final Class<?> root, final String property);

    /**
     * Marks a concrete property's tick to be <b>mutably</b> checked (used) in domain tree manager.<br>
     * If the property becomes 'used', it places into the end of {@link #usedProperties(Class)} list.
     * <br>
     * 
     * The action should not conflict with "checked properties" contract. The conflict will produce an {@link DomainTreeException}.
     * 
     * @param root
     *            -- a root type that contains property.
     * @param property
     *            -- a dot-notation expression that defines a property.
     * @param check
     *            -- an action to perform (<code>true</code> to use, <code>false</code> to un-use)
     * 
     */
    IUsageManager use(final Class<?> root, final String property, final boolean check);

    /**
     * Returns an <b>ordered</b> list of used properties for concrete <code>root</code> type.
     * 
     * @param root
     *            -- a root type that contains an used properties.
     * @return
     */
    List<String> usedProperties(final Class<?> root);
}