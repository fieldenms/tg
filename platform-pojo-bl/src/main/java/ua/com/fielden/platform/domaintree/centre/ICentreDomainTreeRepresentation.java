package ua.com.fielden.platform.domaintree.centre;

import ua.com.fielden.platform.domaintree.IDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeManager.ITickRepresentationWithMutability;

/**
 * This interface defines how domain tree can be represented for <b>entity centres</b>. <br><br>
 *
 * Domain tree consists of a tree of properties. <br><br>
 *
 * <b>Important:</b> it is necessary to override {@link #equals(Object)} and {@link #hashCode()} methods in implementors to provide logical comparison of instances. <br><br>
 *
 * 1. Each property has two "tick" representations (refer to {@link IAddToCriteriaTickRepresentation} and {@link IAddToResultTickRepresentation}).<br>
 *
 * @author TG Team
 *
 */
public interface ICentreDomainTreeRepresentation extends IDomainTreeRepresentation {
    /**
     * Returns a tree representation for a <b>add to criteria</b> tick. See {@link IAddToCriteriaTickRepresentation} for more details.
     */
    IAddToCriteriaTickRepresentation getFirstTick();

    /**
     * Returns a tree representation for a <b>add to result-set</b> tick. See {@link IAddToResultTickRepresentation} for more details.
     */
    IAddToResultTickRepresentation getSecondTick();

    /**
     * This interface defines how <b>entity centre</b> domain tree can be represented for <b>criteria</b> (property represents as a criteria editor). <br><br>
     *
     * <b>Important:</b> it is necessary to override {@link #equals(Object)} and {@link #hashCode()} methods in implementors to provide logical comparison of instances. <br><br>
     *
     * The major aspects of tree management (context-specific) are following: <br>
     * 1. property's default values/values2 (adding to criteria);<br>
     *
     * @author TG Team
     *
     */
    public interface IAddToCriteriaTickRepresentation extends ITickRepresentationWithMutability {
	/**
	 * Gets a <b>default</b> <i>main</i> value of a criteria property. <br><br>
	 *
	 * This contract should not conflict with "excluded properties" contract. The conflict will produce an {@link IllegalArgumentException}.<br><br>
	 *
	 * @param root -- a root type that contains property.
	 * @param property -- a dot-notation expression that defines a property.
	 * @return
	 */
	Object getValueByDefault(final Class<?> root, final String property);

	/**
	 * Gets an <b>empty</b> <i>main</i> value of a criteria property. <br><br>
	 *
	 * This contract should not conflict with "excluded properties" contract. The conflict will produce an {@link IllegalArgumentException}.<br><br>
	 *
	 * @param root -- a root type that contains property.
	 * @param property -- a dot-notation expression that defines a property.
	 * @return
	 */
	Object getEmptyValueFor(final Class<?> root, final String property);

	/**
	 * Sets a <b>default</b> <i>main</i> value of a criteria property. <br><br>
	 *
	 * This action should not conflict with "excluded properties" contract. The conflict will produce an {@link IllegalArgumentException}.<br><br>
	 *
	 * @param root -- a root type that contains property.
	 * @param property -- a dot-notation expression that defines a property.
	 * @param value -- a value to set
	 * @return -- a criteria tick representation
	 */
	IAddToCriteriaTickRepresentation setValueByDefault(final Class<?> root, final String property, final Object value);

	/**
	 * Gets a <b>default</b> <i>secondary</i> value of a criteria property. <br><br>
	 *
	 * This contract should not conflict with "excluded properties" contract. The conflict will produce an {@link IllegalArgumentException}.<br>
	 * This contract is applicable only to <i>double editor (or boolean)</i> properties. These include date type, money type, number type etc. The conflict will produce an {@link IllegalArgumentException}.
	 *
	 * @param root -- a root type that contains property.
	 * @param property -- a dot-notation expression that defines a property.
	 * @return
	 */
	Object getValue2ByDefault(final Class<?> root, final String property);

	/**
	 * Gets an <b>empty</b> <i>secondary</i> value of a criteria property. <br><br>
	 *
	 * This contract should not conflict with "excluded properties" contract. The conflict will produce an {@link IllegalArgumentException}.<br>
	 * This contract is applicable only to <i>double editor (or boolean)</i> properties. These include date type, money type, number type etc. The conflict will produce an {@link IllegalArgumentException}.
	 *
	 * @param root -- a root type that contains property.
	 * @param property -- a dot-notation expression that defines a property.
	 * @return
	 */
	Object get2EmptyValueFor(final Class<?> root, final String property);

	/**
	 * Sets a <b>default</b> <i>secondary</i> value of a criteria property. <br><br>
	 *
	 * This action should not conflict with "excluded properties" contract. The conflict will produce an {@link IllegalArgumentException}.<br>
	 * This action is applicable only to <i>double editor (or boolean)</i> properties. These include date type, money type, number type etc. The conflict will produce an {@link IllegalArgumentException}.
	 *
	 * @param root -- a root type that contains property.
	 * @param property -- a dot-notation expression that defines a property.
	 * @param value2 -- a value to set
	 * @return -- a criteria tick representation
	 */
	IAddToCriteriaTickRepresentation setValue2ByDefault(final Class<?> root, final String property, final Object value2);
    }

    /**
     * This interface defines how <b>entity centre</b> domain tree can be represented for <b>result-set</b> (property represents as a result column). <br><br>
     *
     * <b>Important:</b> it is necessary to override {@link #equals(Object)} and {@link #hashCode()} methods in implementors to provide logical comparison of instances. <br><br>
     *
     * @see IOrderingRepresentation
     * @author TG Team
     *
     */
    public interface IAddToResultTickRepresentation extends IOrderingRepresentation, IWidthRepresentation, ITickRepresentationWithMutability {
    }
}
