package ua.com.fielden.platform.entity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ua.com.fielden.platform.treemodel.rules.ICalculatedProperty;
import ua.com.fielden.platform.treemodel.rules.ICalculatedProperty.CalculatedPropertyCategory;

/**
 * Should be used to indicate whether property is calculated and thus is not persisted.
 *
 * @author TG Team
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface Calculated {
    /**
     * Returns an expression string in <b>eQuery manner</b> that defines a calculated property. The expression should be fully defined in context of root type. <br><br>
     *
     * Concrete parts of expression (simple or other calculated properties) should be incorporated into this expression using dot-notation.
     */
    String expression() default "";

    /**
     * The name of property in root type, from which this calculated property has been originated.
     *
     * @return
     */
    String origination() default "";

    /**
     * The category of calculated property (see {@link ICalculatedProperty.CalculatedPropertyCategory} for more details).
     *
     * @return
     */
    CalculatedPropertyCategory category() default CalculatedPropertyCategory.EXPRESSION;
}
