package ua.com.fielden.platform.entity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that should be used for indication of fields that are properties and to indicate a kind of relation used by the property.
 * <p>
 * Currently following usage cases are supported: <br>
 * 1. <b>ordinary property</b> -- just use @IsProperty:
 *
 * <pre>
 * ...
 * &#64;IsProperty
 * private BigDecimal bigDecimalProp;
 * ...
 * </pre>
 *
 * 2. <b>property descriptor</b> -- use @IsProperty with class parameter: <br>
 *
 * <pre>
 * ...
 * &#64;IsProperty(Rotable.class)
 * private PropertyDescriptor&lt;Rotable&gt; rotablePropertyDescriptor;
 * ...
 * </pre>
 *
 * 3. <b>many-to-one</b> association -- just use @IsProperty:
 *
 * <pre>
 * ...
 * &#64;IsProperty
 * private Make make;
 * ...
 * </pre>
 *
 * 4. <b>one-to-many</b> association -- use @IsProperty with class parameter and linkProperty:
 *
 * <pre>
 * ...
 * &#64;IsProperty(value = FuelUsage.class, linkProperty = "vehicle")
 * private Set&lt;FuelUsage&gt; fuelUsages;
 * ...
 * </pre>
 *
 * In case of one-to-many association setting {@link #value()} should be used to specify type of collection elements (necessary). {@link #linkProperty()} is used to determine by
 * which property of element type the association occurs (it can be omitted only in case when FuelUsage has a composite key with the same type as parent).
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
    public static String stubForLinkProperty = "----dummy-property----";

    /**
     * This setting should be used to identify a type parameter of the property type. For example, it could indicate an element type for collectional properties.
     *
     * @return
     */
    Class<?> value() default Void.class;

    /**
     * This setting makes sense only in case of collection property, which elements are entities. It should be used (and not missed!) to specify a property by which this collection
     * is linked to some "parent" type. Only direct "parents" are allowed, see example below :
     *
     * <pre>
     * WorkOrder
     *   serviced : Vehicle
     *     <i><b>IsProperty(value = WorkOrder.class, linkProperty = "backedUp")</b></i>
     *     <i><b>backedUpWorkorders : WorkOrder []</b></i>
     *       serviced : Vehicle
     *       <i><b>backedUp : Vehicle</b> (the same as <b>WorkOrder->serviced</b> and will be deleted)</i>
     *       movedUp  : Vehicle
     *     <i><b>IsProperty(value = WorkOrder.class, linkProperty = "backedUp")</b></i>
     *     <i><b>lastBackedUpWorkorder : WorkOrder</b></i>
     *       serviced : Vehicle
     *       <i><b>backedUp : Vehicle</b> (the same as <b>WorkOrder->serviced</b> and will be deleted)</i>
     *       movedUp  : Vehicle
     *     orderDetails : VehicleOrderDetails
     *       key : Vehicle <i>(the same as <b>WorkOrder->serviced</b> and will be deleted)</i>
     *   backedUp : Vehicle
     *     ...
     *   serviced : Vehicle
     *     ...
     * </pre>
     *
     * @return
     */
    String linkProperty() default stubForLinkProperty;

    /**
     * Declares property as such that should be assigned automatically before entity is saved for the first time.
     *
     * @return
     */
    boolean assignBeforeSave() default false;
}
