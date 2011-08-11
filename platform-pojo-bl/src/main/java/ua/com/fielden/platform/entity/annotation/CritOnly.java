package ua.com.fielden.platform.entity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that an entity property should only be used as a criterion for dynamic entity reviews (i.e. it cannot be added to the result set). {@link #value()} indicates range or
 * single selection to be used for property. See {@link Type} for more details.
 * 
 * @author TG Team
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface CritOnly {

    Type value() default Type.RANGE; // represents a choice by which boundary (left or right) the property should be selected.

    /**
     * Enumeration to choose whether range property should be selected by single or double criterion. Single property will not be affected.
     * 
     * @author jhou
     * 
     */
    public enum Type {
	/**
	 * Indicates that property should be selected by left and right boundary (if it is range property) and by single boundary (if it is single property).
	 */
	RANGE,
	/**
	 * Indicates that property should be selected only by single boundary (even if it is range property).
	 */
	SINGLE
    }
}
