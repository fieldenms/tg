package ua.com.fielden.platform.entity.annotation;

import ua.com.fielden.platform.entity.validation.MaxLengthValidator;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.types.RichText;

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
    String DEFAULT_LINK_PROPERTY = "----dummy-property----";
    int DEFAULT_LENGTH = 0;
    int MAX_LENGTH = Integer.MAX_VALUE;
    int DEFAULT_PRECISION = -1;
    int DEFAULT_SCALE = -1;
    boolean DEFAULT_TRAILING_ZEROS = true;
    String DEFAULT_DISPLAY_AS = "";


    /**
     * This setting should be used to identify a type parameter of the property type. For example, it could indicate an element type for collectional properties.
     */
    Class<?> value() default Void.class;
    // keep these in sync, as the field cannot be used in place of the class literal
    Class<?> DEFAULT_VALUE = Void.class;

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
     */
    String linkProperty() default DEFAULT_LINK_PROPERTY;

    /**
     * Declares property as such that should be assigned automatically before entity is saved for the first time.
     */
    boolean assignBeforeSave() default false;

    /**
     * Length indicates the maximum length of a value for a <code>String</code> property.
     * This value can be used for validation, but in and of itself, this value is only an indicator of a maximum length.
     * <p>
     * Value `0` indicates "undetermined" length, which may have context-dependent interpretation.
     * For example, a database schema generator may use its own default length in such cases.
     * <p>
     * If the length of value `Integer.MAX_VALUE` (also `MAX_LENGTH`) is specified for properties of type `String`, the DDL generation tool would pick the most appropriate data type to represent long text.
     * For example, for PostgreSQL type `text` and for SQL Server type `varchar(max)` would be used.
     * <p>
     * Also, the length value can affect pre-condition {@link MaxLengthValidator} to enforce the length integrity constraint on properties of types {@link String}, {@link Hyperlink}, and {@link RichText}.
     * Please refer to {@link MaxLengthValidator} for more details and supported property types.
     */
    int length() default DEFAULT_LENGTH;

    /**
     * Precision is the number of digits in a number. For example, the number 123.45 has a precision of 5.
     * <p>
     * This parameter is applicable only to properties of type <code>BigDecimal</code>.
     */
    int precision() default DEFAULT_PRECISION;


    /**
     * Scale is the number of digits to the right of the decimal point in a number. For example, the number 123.45 has a scale of 2.
     * <p>
     * This parameter is applicable only to properties of type <code>BigDecimal</code>.
     */
    int scale() default DEFAULT_SCALE;

    /**
     * This parameter should be used to indicate whether trailing zeros have any significance for decimal properties (including Money).
     * Such information can be used by, for example, UI logic for displaying or hind the trailing zeros.
     */
    boolean trailingZeros() default DEFAULT_TRAILING_ZEROS;

    /**
     * Defines a template for displaying a value of a composite entity-typed property.
     * Has no effect for other property types or for non-composite entities.
     * The value of this attribute can be empty, which means that the displayed value would use the default pattern (i.e., composite entity property will be displayed with the title-value pattern).
     * This attribute supports patterns like {@code #1tv#2tv}, {@code #1vs#2v}, {@code #1v}, {@code z}, {@code #1.2tv#2tv}, {@code #1.2.3v}, where:
     * <ol>
     *  <li>{@code #i} – the i-th key member (this must be the starting token in a template);
     *  <li>{@code i.j} – the j-th key member in the i-th key member (the i-th key member must be a composite entity).
     *      Arbitrary depth of key members is supported (e.g., {@code i.*.z}, where {@code *} may be replaced with any number of key members).
     *  <li>{@code t} – title of a preceding key member
     *      (e.g., {@code #1t} represents the title of the 1st key member, {@code #1.2t} - title of the 2nd key member in the 1st key member);
     *  <li>{@code v} – value of a preceding key member
     *      (e.g., {@code #1v} represents the value of the 1st key member, {@code #1.2v} - value of the 2nd key member in the 1st key member);
     *  <li>{@code s} – an optional key member separator (mutually exclusive with {@code t}).
     * </ol>
     * Alternatively, a template may consist of a single {@code z} character, serving as a {@code toString} representation of the displayed entity.
     * <p>
     * The default template is {@code ""}, which is equivalent to applying template {@code #itv} to every key member.
     */
    String displayAs() default DEFAULT_DISPLAY_AS;
}
