package ua.com.fielden.platform.domaintree.centre;

import ua.com.fielden.platform.domaintree.IDomainTreeRepresentation.ITickRepresentation;

/**
 * This interface defines how domain tree "tick" can be represented for base "add to result" functionality. <br><br>
 *
 * The major aspects of tree tick representation are following: <br><br>
 *  3. property's width (by default);<br>
 *
 * @see ITickRepresentation
 * @author TG Team
 *
 */
public interface IWidthRepresentation {
    /**
     * Defines a contract which properties (columns) have what <b>default</b> <i>widths</i>. <br><br>
     *
     * This contract should not conflict with "excluded properties" contract. The conflict will produce an {@link IllegalArgumentException}.<br><br>
     *
     * @param root -- a root type that contains property.
     * @param property -- a dot-notation expression that defines a property.
     * @return
     */
    int getWidthByDefault(final Class<?> root, final String property);

    /**
     * Marks a concrete property (column) to have specified <b>default</b> <i>width</i>. <br><br>
     *
     * This action should not conflict with "excluded properties" contract. The conflict will produce an {@link IllegalArgumentException}.<br><br>
     *
     * @param root -- a root type that contains property.
     * @param property -- a dot-notation expression that defines a property.
     * @param width -- a width to set
     */
    IWidthRepresentation setWidthByDefault(final Class<?> root, final String property, final int width);
}
