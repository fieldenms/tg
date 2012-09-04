package ua.com.fielden.platform.entity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;

/**
 * An annotation to be used to indicate that property is calculated.
 * <p>
 * There are <i>contextual</i> and <i>non-contextual</i> calculated properties.
 * <p>
 * <i><b>Non-contextual</b></i> properties are <i>hard-coded and immutable</i>. It should be expressed in terms of direct parent type.
 * It should be defined with one of the following patterns:<br>
 * a) @Calculated -- calculated property with {@link ExpressionModel} defined (use "private static final ExpressionModel [name]_" to define the model).<br>
 * b) @Calculated("2 * integerProp") -- calculated property with "expression" defined.
 * <p>
 * <i><b>Contextual</b></i> properties are <i>auto-generated</i> properties from {@link IDomainTreeEnhancer} and should not be used in domain modelling.
 * So please avoid usage of any calculated property parameter except {@link #value()}.
 *
 * @author TG Team
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface Calculated {
    public static final String NOTHING = "-%-> NOTHING <-%-";
    public static String EMPTY = "";

    /**
     * Returns an expression string that defines the calculated property.
     *
     * @return
     */
    String value() default EMPTY;

    /**
     * Class indicating a root type name for calculated property.
     *
     * @return
     */
    String rootTypeName() default NOTHING;

    /**
     * Returns a path that defines the context of the calculated property. The path should be fully defined in context of root type.
     *
     * @return
     */
    String contextPath() default NOTHING;

    /**
     * The attribute of calculated property.
     *
     * @return
     */
    CalculatedPropertyAttribute attribute() default CalculatedPropertyAttribute.NO_ATTR;

    /**
     * The name of property in context type, from which this calculated property has been originated.
     *
     * @return
     */
    String origination() default NOTHING;

    /**
     * The category of calculated property.
     *
     * @return
     */
    CalculatedPropertyCategory category() default CalculatedPropertyCategory.EXPRESSION;
}
