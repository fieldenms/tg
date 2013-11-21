package ua.com.fielden.platform.swing.review.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * The annotation for association batch action classes.
 *
 * @author TG Team
 *
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface AssociationAction {

    /**
     * Returns the first property in association.
     *
     * @return
     */
    String firstPropertyInAssociation();

    /**
     * Returns the second property in association.
     *
     * @return
     */
    String secondPropertyInAssociation();

    /**
     * Returns the property name that is in association between first and second association properties.
     *
     * @return
     */
    String propertyInAssociation() default "";
}
