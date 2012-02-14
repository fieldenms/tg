package ua.com.fielden.platform.entity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory;

/**
 * An annotation to be used to indicate that property is calculated.
 *
 * @author TG Team
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface Calculated {
    /**
     * Returns an expression string that defines the calculated property. The expression should be fully defined in context of "context type".
     *
     * @return
     */
    String contextualExpression() default ""; // TODO this should be required

    /**
     * Returns a path that defines the context of the calculated property. The path should be fully defined in context of root type.
     *
     * @return
     */
    String contextPath() default ""; // TODO this should be required

    /**
     * The attribute of calculated property.
     *
     * @return
     */
    CalculatedPropertyAttribute attribute() default CalculatedPropertyAttribute.NO_ATTR; // TODO this should be required

    /**
     * The name of property in context type, from which this calculated property has been originated.
     *
     * @return
     */
    String origination() default ""; // TODO this should be required

    /**
     * The category of calculated property.
     *
     * @return
     */
    CalculatedPropertyCategory category() default CalculatedPropertyCategory.EXPRESSION; // TODO this should be inferred from "contextualExpression" and "contextPath"
}
