package ua.com.fielden.platform.domaintree;



/**
 * The <b>calculated property</b> represents an abstraction for an expression which could be used in queries
 * and their results exactly as simple property. <br><br>
 *
 * <b>Important:</b> it is necessary to override {@link #equals(Object)} and {@link #hashCode()} methods in implementors to provide logical comparison of instances. <br><br>
 *
 * For example (root entity is Vehicle.class):<br>
 * 1. <i>Vehicle.doubleTanksQty := "2 * SUM([Vehicle.fuelUsages.oilQuantity]) / [Vehicle.techDetails.tankCapasity]"</i> (collectional aggregation, could contain an <i>aggregated collectional</i> atoms or simple atoms -- adds as domain tree extension, could be queried/resulted)<br>
 * 2. <i>Vehicle.readingWarrantyBalance := "3 * [Vehicle.lastReading] - [Vehicle.techDetails.warrantyKms]"</i>  (a couple of properties inside expression -- adds as domain tree extension, could be queried/resulted)<br>
 * 2a.<i>Vehicle.yearsFrom1970OfInitDate := "YEAR([Vehicle.initDate]) - 1970"</i>  (date or another property inside expression -- adds as domain tree extension, could be queried/resulted)<br>
 * 3. <i>Vehicle.averageReading := "AVG([Vehicle.readingWarrantyBalance - 100000])"</i>  (totals / analysisAggregation expression -- calculated property strictly <b>assigned</b> to a property from which it is originated, appears in "origination" property context only)<br>
 *
 * @author TG Team
 *
 */
public interface ICalculatedProperty /* extends Serializable */ {
    /**
     * Represents a different categories (types) of <b>calculated properties</b>.<br><br>
     *
     * For e.g. {@link CalculatedPropertyCategory#COLLECTIONAL_EXPRESSION}, {@link CalculatedPropertyCategory#EXPRESSION},
     * {@link CalculatedPropertyCategory#AGGREGATED_COLLECTIONAL_EXPRESSION}, {@link CalculatedPropertyCategory#ATTRIBUTED_COLLECTIONAL_EXPRESSION},
     * {@link CalculatedPropertyCategory#AGGREGATED_EXPRESSION}.
     *
     *
     * @author TG Team
     *
     */
    public enum CalculatedPropertyCategory /* implements Serializable */ {
        /**
         * The category of <b>calculated properties</b> only with simple hierarchy members -- no members of collectional hierarchy.
         * There are no restrictions for hierarchy levels to be used in this expression (higher or lower in the actual calculated property place).<br><br>
         *
         * Example: <br>
         * Place : Vehicle=><i>[status]</i>; <br>
         * Property = <i>[status.operational] and ([lastReading] > 1000 or [replacing.techDetails.amount] < 100)</i>
         *
         */
        EXPRESSION,
        /**
         * The category of <b>calculated properties</b> only with aggregated {@link #EXPRESSION}s.
         * Neither simple nor collectional members are permitted outside of aggregated sub-{@link #EXPRESSION}s in this expression.<br><br>
         *
         * Example: <br>
         * Place : Vehicle=><i>[]</i>; <br>
         * Property = <i>2 * <b>SUM(</b>2 * [replacing.techDetails.amount] - [lastReading]<b>)</b> + 3 * <b>AVG(</b>[replacing.lastReading] * 7<b>)</b></i>
         *
         */
        AGGREGATED_EXPRESSION,
        /**
         * The category of <b>calculated properties</b> with simple members and at least one member of collectional hierarchy
         * (the only one collectional hierarchy is permitted, and that hierarchy will be the place of the calculated property).<br><br>
         *
         * Example: <br>
         * Place : Vehicle=><i>[fuelUsages]</i>; <br>
         * Property = <i>2 * <b>[fuelUsages.oilQty]</b> - 4 * <b>[fuelUsages.details.oilPrice]</b> - [lastReading]</i>
         *
         */
        COLLECTIONAL_EXPRESSION,
        /**
         * The category of <b>calculated properties</b> with simple hierarchy members and with at least one aggregated {@link #COLLECTIONAL_EXPRESSION}.<br><br>
         *
         * Example: <br>
         * Place : Vehicle=><i>[]</i>; <br>
         * Property = <i><b>SUM(</b>2 * [fuelUsages.oilQty] - [lastReading]<b>)</b> + [replacing.lastReading] * 7</i>
         *
         */
        AGGREGATED_COLLECTIONAL_EXPRESSION,
        /**
         * The category of <b>calculated properties</b> with one and only one attributed {@link #COLLECTIONAL_EXPRESSION} (with ALL / ANY attributes).
         * No other expressions are permitted outside of ALL / ANY attribute.<br><br>
         *
         * Example: <br>
         * Place : Vehicle=><i>[]</i>; <br>
         * Property = <i><b>ALL(</b>2 * [fuelUsages.oilQty] - [lastReading]<b>)</b></i>
         *
         */
        ATTRIBUTED_COLLECTIONAL_EXPRESSION
    }

    /**
     * Returns a category (type, class) of calculated property.
     *
     * @return
     */
    CalculatedPropertyCategory getCategory();

    /**
     * The calculated property path and name. The calculated property exists in strict place with path {@link #getPath()} in root type {@link #getRootType()}. <br><br>
     *
     * For e.g. :<br>
     * root = WorkOrder.class, // the concrete place of calculated property (root)<br>
     * path = vehicle.status, // the concrete place of calculated property (path)<br>
     * name = operationalAndActive, // name of calc property<br>
     * parentType = VehicleStatus.class, //<br>
     * expr = [operational] and [active], // should be expressed in context of parentType<br>
     *
     * @return
     */
    String getPathAndName();

    /**
     * The calculated property path. The calculated property exists in strict place with path {@link #getPath()} in root type {@link #getRootType()}. <br><br>
     *
     * For e.g. :<br>
     * root = WorkOrder.class, // the concrete place of calculated property (root)<br>
     * path = vehicle.status, // the concrete place of calculated property (path)<br>
     * name = operationalAndActive, // name of calc property<br>
     * parentType = VehicleStatus.class, //<br>
     * expr = [operational] and [active], // should be expressed in context of parentType<br>
     *
     * @return
     */
    String getPath();

    /**
     * The root type of calculated property. The calculated property exists in strict place with path {@link #getPath()} in root type {@link #getRootType()}. <br><br>
     *
     * For e.g. :<br>
     * root = WorkOrder.class, // the concrete place of calculated property (root)<br>
     * path = vehicle.status, // the concrete place of calculated property (path)<br>
     * name = operationalAndActive, // name of calc property<br>
     * parentType = VehicleStatus.class, //<br>
     * expr = [operational] and [active], // should be expressed in context of parentType<br>
     *
     * @return
     */
    Class<?> getRootType();

    /**
     * The name of calculated property in context of parent type ({@link #getParentType()}). <br><br>
     *
     * For e.g. :<br>
     * root = WorkOrder.class, // the concrete place of calculated property (root)<br>
     * path = vehicle.status, // the concrete place of calculated property (path)<br>
     * name = operationalAndActive, // name of calc property<br>
     * parentType = VehicleStatus.class, //<br>
     * expr = [operational] and [active], // should be expressed in context of parentType<br>
     *
     * @return
     */
    String getName();

    /**
     * The parent type of calculated property. The expression ({@link #getExpression()}) should be fully defined in context of this type. <br><br>
     *
     * For e.g. :<br>
     * root = WorkOrder.class, // the concrete place of calculated property (root)<br>
     * path = vehicle.status, // the concrete place of calculated property (path)<br>
     * name = operationalAndActive, // name of calc property<br>
     * parentType = VehicleStatus.class, //<br>
     * expr = [operational] and [active], // should be expressed in context of parentType<br>
     *
     * @return
     */
    Class<?> getParentType();

    /**
     * The name of property in parent type ({@link #getParentType()}), from which this calculated property has been originated.
     *
     * @return
     */
    String getOriginationPropertyName();

    /**
     * Returns an expression string in <b>eQuery manner</b> that defines a calculated property. The expression should be fully defined in context of root type ({@link #getRootType()}). <br><br>
     *
     * Concrete parts of expression (simple or other calculated properties) should be incorporated into this expression using dot-notation.
     *
     * @return
     */
    String getExpression();

    /**
     * Sets an expression string in <b>eQuery manner</b> that defines a calculated property. The expression should be fully defined in context of parent type ({@link #getParentType()}). <br><br>
     *
     * Concrete parts of expression (simple or other calculated properties) should be incorporated into this expression using dot-notation.
     *
     * @param expression -- an expression string in <b>eQuery manner</b> that defines a calculated property.
     */
    ICalculatedProperty setExpression(final String expression);

    /**
     * Returns a result type of calculated property.
     */
    Class<?> getResultType();

    /**
     * Sets a result type of calculated property.
     */
    ICalculatedProperty setResultType(final Class<?> resultType);

    /**
     * Returns the title of calculated property.
     */
    String getTitle();

    /**
     * Sets the title of calculated property.
     */
    ICalculatedProperty setTitle(final String title);

    /**
     * Returns the description of calculated property.
     */
    String getDesc();

    /**
     * Sets the description of calculated property.
     */
    ICalculatedProperty setDesc(final String desc);

    @Override
    public boolean equals(Object obj);

    @Override
    public int hashCode();
}
