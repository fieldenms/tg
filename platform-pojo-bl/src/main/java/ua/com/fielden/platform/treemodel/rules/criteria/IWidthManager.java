package ua.com.fielden.platform.treemodel.rules.criteria;


/**
 * This interface defines how domain tree "tick" can be managed for base "add to result" functionality. <br><br>
 *
 * The major aspects of tree management (context-specific) are following: <br>
 * 1. property's width;<br>
 *
 * @author TG Team
 *
 */
public interface IWidthManager {
    /**
     * Gets a <i>width</i> for property. <br><br>
     *
     * This contract should not conflict with "checked properties" (or with "used properties" -- more accurately) contract. The conflict will produce an {@link IllegalArgumentException}.<br><br>
     *
     * @param root -- a root type that contains property.
     * @param property -- a dot-notation expression that defines a property.
     * @return
     */
    int getWidth(final Class<?> root, final String property);

    /**
     * Sets a <i>width</i> for property. <br><br>
     *
     * This action should not conflict with "checked properties" (or with "used properties" -- more accurately) contract. The conflict will produce an {@link IllegalArgumentException}.<br><br>
     *
     * @param root -- a root type that contains property.
     * @param property -- a dot-notation expression that defines a property.
     * @param width -- a width to set
     * @return -- a result tick representation
     */
    void setWidth(final Class<?> root, final String property, final int width);
}
