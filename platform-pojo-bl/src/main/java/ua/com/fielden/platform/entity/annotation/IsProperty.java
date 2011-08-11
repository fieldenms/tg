package ua.com.fielden.platform.entity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Should be used for indication of fields that are properties.
 *
 * Setting {@link #value()} should be used to specify type of elements in case of annotating collectional property. This is required to cater for Java's approach not to store any
 * generic info for RTTI.
 * <p>
 * For example:
 *
 * <pre>
 * &#064;IsProperty(String.class)
 * private List&lt;String&gt; collectionalProperty;
 * </pre>
 * <p>
 * If there no specific type parameter or there is an unbound type parameter for collectional property then Object should be specified:
 *
 * <pre>
 * public class Unbound&lt;T&gt; {
 * 	...
 * 	&#64;IsProperty(Object.class)
 * 	private List&lt;T&gt; collectionalProperty;
 * 	...
 * }
 * </pre>
 *
 * If a bound type parameter for collectional property is specified then the boundary class should used:
 *
 * <pre>
 * public class Bound&lt;T extends Rotable&gt; {
 * 	...
 * 	&#64;IsProperty(Rotable.class)
 * 	private List&lt;T&gt; collectionalProperty;
 * 	...
 * }
 * </pre>
 *
 * @author TG Team
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface IsProperty {
    /**
     * This setting should be used to identify a type parameter of the property type. For example, it could indicate an element type for collectional properties.
     *
     * @return
     */
    Class<?> value() default Void.class;
}
