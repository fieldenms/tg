package ua.com.fielden.platform.domaintree;

import java.util.List;

/**
 * This interface defines how domain tree can be managed for <b>analyses</b> specific (a piece of logic). <br>
 * <br>
 * 
 * <b>Important:</b> it is necessary to override {@link #equals(Object)} and {@link #hashCode()} methods in implementors to provide logical comparison of instances. <br>
 * <br>
 * 
 * The major aspects of tree management (context-specific) are following: <br>
 * 1. used properties (a subset in checked properties)<br>
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
     * This contract should not conflict with "checked properties" contract. The conflict will produce an {@link IllegalArgumentException}.<br>
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
     * Marks a concrete property's tick to be <b>mutably</b> checked (used) in domain tree manager. <br>
     * <br>
     * 
     * The action should not conflict with "checked properties" contract. The conflict will produce an {@link IllegalArgumentException}.
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