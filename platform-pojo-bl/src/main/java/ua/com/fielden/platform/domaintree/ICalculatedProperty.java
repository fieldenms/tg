package ua.com.fielden.platform.domaintree;

import ua.com.fielden.platform.basic.IPropertyEnum;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;

/**
 * The <b>calculated property</b> represents an abstraction for an expression which could be used in queries and their results exactly as simple property. <br>
 * <br>
 * 
 * <b>Important:</b> it is necessary to override {@link #equals(Object)} and {@link #hashCode()} methods in implementors to provide logical comparison of instances. <br>
 * <br>
 * 
 * For example (root entity is Vehicle.class):<br>
 * 1. <i>Vehicle.doubleTanksQty := "2 * SUM([Vehicle.fuelUsages.oilQuantity]) / [Vehicle.techDetails.tankCapasity]"</i> (collectional aggregation, could contain an <i>aggregated
 * collectional</i> atoms or simple atoms -- adds as domain tree extension, could be queried/resulted)<br>
 * 2. <i>Vehicle.readingWarrantyBalance := "3 * [Vehicle.lastReading] - [Vehicle.techDetails.warrantyKms]"</i> (a couple of properties inside expression -- adds as domain tree
 * extension, could be queried/resulted)<br>
 * 2a.<i>Vehicle.yearsFrom1970OfInitDate := "YEAR([Vehicle.initDate]) - 1970"</i> (date or another property inside expression -- adds as domain tree extension, could be
 * queried/resulted)<br>
 * 3. <i>Vehicle.averageReading := "AVG([Vehicle.readingWarrantyBalance - 100000])"</i> (totals / analysisAggregation expression -- calculated property strictly <b>assigned</b> to
 * a property from which it is originated, appears in "origination" property context only)<br>
 * 
 * @author TG Team
 * 
 */
public interface ICalculatedProperty {
    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////// Required and immutable stuff ////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * A root type of the calculated property. <br>
     * <br>
     * 
     * For e.g. :<br>
     * {@link #getRoot()} = WorkOrder.class, // a root of a calculated property<br>
     * {@link #getContextPath()} = vehicle.replacing.fuelUsages, // a context path of the calculated property<br>
     * {@link #getContextualExpression()} = AVG(purchasePrice), // should be expressed in context of contextPath type<br>
     * {@link #getTitle()} = Average of purchase price, // a title that form a unique name<br>
     * {@link #getDesc()} = The average of purchase price, // description<br>
     * {@link #getAttribute()} = CalculatedPropertyAttribute.NO_ATTR, // can be ALL or ANY in case of COLLECTIONAL_EXPRESSION category, otherwise should be NO_ATTR <br>
     * {@link #getOriginationProperty()} = purchasePrice, // the property (in context of contextPath type), on which calc property is based. This is required for
     * AGGREGATED_EXPRESSION (aka Totals) <br>
     * {@link #category()} = CalculatedPropertyCategory.AGGREGATED_COLLECTIONAL_EXPRESSION, // a category for calculated property (inferred) <br>
     * {@link #name()} = averageOfPurchasePrice, // name of calc property (inferred) <br>
     * {@link #path()} = vehicle.replacing, // path of calc property (inferred) <br>
     * {@link #pathAndName()} = vehicle.replacing.averageOfPurchasePrice, // path and name combined together (inferred) <br>
     * {@link #contextType()} = FuelUsage.class, // a context type of a calculated property (inferred) <br>
     * {@link #parentType()} = Vehicle.class, // a type of a calculated property parent (inferred) <br>
     * {@link #resultType()} = BigDecimal.class, // a resultant type of a calculated property expression (inferred) <br>
     * 
     * @return
     */
    Class<?> getRoot();

    /**
     * The calculated property path. The calculated property exists in strict place with path {@link #getPath()} in root type {@link #getRoot()}. <br>
     * <br>
     * 
     * For e.g. :<br>
     * {@link #getRoot()} = WorkOrder.class, // a root of a calculated property<br>
     * {@link #getContextPath()} = vehicle.replacing.fuelUsages, // a context path of the calculated property<br>
     * {@link #getContextualExpression()} = AVG(purchasePrice), // should be expressed in context of contextPath type<br>
     * {@link #getTitle()} = Average of purchase price, // a title that form a unique name<br>
     * {@link #getDesc()} = The average of purchase price, // description<br>
     * {@link #getAttribute()} = CalculatedPropertyAttribute.NO_ATTR, // can be ALL or ANY in case of COLLECTIONAL_EXPRESSION category, otherwise should be NO_ATTR <br>
     * {@link #getOriginationProperty()} = purchasePrice, // the property (in context of contextPath type), on which calc property is based. This is required for
     * AGGREGATED_EXPRESSION (aka Totals) <br>
     * {@link #category()} = CalculatedPropertyCategory.AGGREGATED_COLLECTIONAL_EXPRESSION, // a category for calculated property (inferred) <br>
     * {@link #name()} = averageOfPurchasePrice, // name of calc property (inferred) <br>
     * {@link #path()} = vehicle.replacing, // path of calc property (inferred) <br>
     * {@link #pathAndName()} = vehicle.replacing.averageOfPurchasePrice, // path and name combined together (inferred) <br>
     * {@link #contextType()} = FuelUsage.class, // a context type of a calculated property (inferred) <br>
     * {@link #parentType()} = Vehicle.class, // a type of a calculated property parent (inferred) <br>
     * {@link #resultType()} = BigDecimal.class, // a resultant type of a calculated property expression (inferred) <br>
     * 
     * @return
     */
    String getContextPath();

    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////// Required and mutable stuff //////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns an expression string in <b>eQuery manner</b> that defines a calculated property. The expression should be fully defined in context of {@link #contextType()}.
     * Concrete parts of expression (simple or other calculated properties) should be incorporated into this expression using dot-notation. <br>
     * <br>
     * 
     * For e.g. :<br>
     * {@link #getRoot()} = WorkOrder.class, // a root of a calculated property<br>
     * {@link #getContextPath()} = vehicle.replacing.fuelUsages, // a context path of the calculated property<br>
     * {@link #getContextualExpression()} = AVG(purchasePrice), // should be expressed in context of contextPath type<br>
     * {@link #getTitle()} = Average of purchase price, // a title that form a unique name<br>
     * {@link #getDesc()} = The average of purchase price, // description<br>
     * {@link #getAttribute()} = CalculatedPropertyAttribute.NO_ATTR, // can be ALL or ANY in case of COLLECTIONAL_EXPRESSION category, otherwise should be NO_ATTR <br>
     * {@link #getOriginationProperty()} = purchasePrice, // the property (in context of contextPath type), on which calc property is based. This is required for
     * AGGREGATED_EXPRESSION (aka Totals) <br>
     * {@link #category()} = CalculatedPropertyCategory.AGGREGATED_COLLECTIONAL_EXPRESSION, // a category for calculated property (inferred) <br>
     * {@link #name()} = averageOfPurchasePrice, // name of calc property (inferred) <br>
     * {@link #path()} = vehicle.replacing, // path of calc property (inferred) <br>
     * {@link #pathAndName()} = vehicle.replacing.averageOfPurchasePrice, // path and name combined together (inferred) <br>
     * {@link #contextType()} = FuelUsage.class, // a context type of a calculated property (inferred) <br>
     * {@link #parentType()} = Vehicle.class, // a type of a calculated property parent (inferred) <br>
     * {@link #resultType()} = BigDecimal.class, // a resultant type of a calculated property expression (inferred) <br>
     * 
     * @return
     */
    String getContextualExpression();

    /**
     * Sets an expression string in <b>eQuery manner</b> that defines a calculated property. The expression should be fully defined in context of {@link #contextType()}. Concrete
     * parts of expression (simple or other calculated properties) should be incorporated into this expression using dot-notation. <br>
     * <br>
     * 
     * For e.g. :<br>
     * {@link #getRoot()} = WorkOrder.class, // a root of a calculated property<br>
     * {@link #getContextPath()} = vehicle.replacing.fuelUsages, // a context path of the calculated property<br>
     * {@link #getContextualExpression()} = AVG(purchasePrice), // should be expressed in context of contextPath type<br>
     * {@link #getTitle()} = Average of purchase price, // a title that form a unique name<br>
     * {@link #getDesc()} = The average of purchase price, // description<br>
     * {@link #getAttribute()} = CalculatedPropertyAttribute.NO_ATTR, // can be ALL or ANY in case of COLLECTIONAL_EXPRESSION category, otherwise should be NO_ATTR <br>
     * {@link #getOriginationProperty()} = purchasePrice, // the property (in context of contextPath type), on which calc property is based. This is required for
     * AGGREGATED_EXPRESSION (aka Totals) <br>
     * {@link #category()} = CalculatedPropertyCategory.AGGREGATED_COLLECTIONAL_EXPRESSION, // a category for calculated property (inferred) <br>
     * {@link #name()} = averageOfPurchasePrice, // name of calc property (inferred) <br>
     * {@link #path()} = vehicle.replacing, // path of calc property (inferred) <br>
     * {@link #pathAndName()} = vehicle.replacing.averageOfPurchasePrice, // path and name combined together (inferred) <br>
     * {@link #contextType()} = FuelUsage.class, // a context type of a calculated property (inferred) <br>
     * {@link #parentType()} = Vehicle.class, // a type of a calculated property parent (inferred) <br>
     * {@link #resultType()} = BigDecimal.class, // a resultant type of a calculated property expression (inferred) <br>
     * 
     * @param contextualExpression
     *            -- an expression string in <b>eQuery manner</b> that defines a calculated property.
     */
    ICalculatedProperty setContextualExpression(final String contextualExpression);

    /**
     * Returns the title of calculated property.
     * 
     * For e.g. :<br>
     * {@link #getRoot()} = WorkOrder.class, // a root of a calculated property<br>
     * {@link #getContextPath()} = vehicle.replacing.fuelUsages, // a context path of the calculated property<br>
     * {@link #getContextualExpression()} = AVG(purchasePrice), // should be expressed in context of contextPath type<br>
     * {@link #getTitle()} = Average of purchase price, // a title that form a unique name<br>
     * {@link #getDesc()} = The average of purchase price, // description<br>
     * {@link #getAttribute()} = CalculatedPropertyAttribute.NO_ATTR, // can be ALL or ANY in case of COLLECTIONAL_EXPRESSION category, otherwise should be NO_ATTR <br>
     * {@link #getOriginationProperty()} = purchasePrice, // the property (in context of contextPath type), on which calc property is based. This is required for
     * AGGREGATED_EXPRESSION (aka Totals) <br>
     * {@link #category()} = CalculatedPropertyCategory.AGGREGATED_COLLECTIONAL_EXPRESSION, // a category for calculated property (inferred) <br>
     * {@link #name()} = averageOfPurchasePrice, // name of calc property (inferred) <br>
     * {@link #path()} = vehicle.replacing, // path of calc property (inferred) <br>
     * {@link #pathAndName()} = vehicle.replacing.averageOfPurchasePrice, // path and name combined together (inferred) <br>
     * {@link #contextType()} = FuelUsage.class, // a context type of a calculated property (inferred) <br>
     * {@link #parentType()} = Vehicle.class, // a type of a calculated property parent (inferred) <br>
     * {@link #resultType()} = BigDecimal.class, // a resultant type of a calculated property expression (inferred) <br>
     * 
     * @return
     */
    String getTitle();

    /**
     * Sets the title of calculated property.
     * 
     * For e.g. :<br>
     * {@link #getRoot()} = WorkOrder.class, // a root of a calculated property<br>
     * {@link #getContextPath()} = vehicle.replacing.fuelUsages, // a context path of the calculated property<br>
     * {@link #getContextualExpression()} = AVG(purchasePrice), // should be expressed in context of contextPath type<br>
     * {@link #getTitle()} = Average of purchase price, // a title that form a unique name<br>
     * {@link #getDesc()} = The average of purchase price, // description<br>
     * {@link #getAttribute()} = CalculatedPropertyAttribute.NO_ATTR, // can be ALL or ANY in case of COLLECTIONAL_EXPRESSION category, otherwise should be NO_ATTR <br>
     * {@link #getOriginationProperty()} = purchasePrice, // the property (in context of contextPath type), on which calc property is based. This is required for
     * AGGREGATED_EXPRESSION (aka Totals) <br>
     * {@link #category()} = CalculatedPropertyCategory.AGGREGATED_COLLECTIONAL_EXPRESSION, // a category for calculated property (inferred) <br>
     * {@link #name()} = averageOfPurchasePrice, // name of calc property (inferred) <br>
     * {@link #path()} = vehicle.replacing, // path of calc property (inferred) <br>
     * {@link #pathAndName()} = vehicle.replacing.averageOfPurchasePrice, // path and name combined together (inferred) <br>
     * {@link #contextType()} = FuelUsage.class, // a context type of a calculated property (inferred) <br>
     * {@link #parentType()} = Vehicle.class, // a type of a calculated property parent (inferred) <br>
     * {@link #resultType()} = BigDecimal.class, // a resultant type of a calculated property expression (inferred) <br>
     */
    ICalculatedProperty setTitle(final String title);

    /**
     * Returns the description of calculated property.
     * 
     * For e.g. :<br>
     * {@link #getRoot()} = WorkOrder.class, // a root of a calculated property<br>
     * {@link #getContextPath()} = vehicle.replacing.fuelUsages, // a context path of the calculated property<br>
     * {@link #getContextualExpression()} = AVG(purchasePrice), // should be expressed in context of contextPath type<br>
     * {@link #getTitle()} = Average of purchase price, // a title that form a unique name<br>
     * {@link #getDesc()} = The average of purchase price, // description<br>
     * {@link #getAttribute()} = CalculatedPropertyAttribute.NO_ATTR, // can be ALL or ANY in case of COLLECTIONAL_EXPRESSION category, otherwise should be NO_ATTR <br>
     * {@link #getOriginationProperty()} = purchasePrice, // the property (in context of contextPath type), on which calc property is based. This is required for
     * AGGREGATED_EXPRESSION (aka Totals) <br>
     * {@link #category()} = CalculatedPropertyCategory.AGGREGATED_COLLECTIONAL_EXPRESSION, // a category for calculated property (inferred) <br>
     * {@link #name()} = averageOfPurchasePrice, // name of calc property (inferred) <br>
     * {@link #path()} = vehicle.replacing, // path of calc property (inferred) <br>
     * {@link #pathAndName()} = vehicle.replacing.averageOfPurchasePrice, // path and name combined together (inferred) <br>
     * {@link #contextType()} = FuelUsage.class, // a context type of a calculated property (inferred) <br>
     * {@link #parentType()} = Vehicle.class, // a type of a calculated property parent (inferred) <br>
     * {@link #resultType()} = BigDecimal.class, // a resultant type of a calculated property expression (inferred) <br>
     * 
     * @return
     */
    String getDesc();

    /**
     * Sets the description of calculated property.
     * 
     * For e.g. :<br>
     * {@link #getRoot()} = WorkOrder.class, // a root of a calculated property<br>
     * {@link #getContextPath()} = vehicle.replacing.fuelUsages, // a context path of the calculated property<br>
     * {@link #getContextualExpression()} = AVG(purchasePrice), // should be expressed in context of contextPath type<br>
     * {@link #getTitle()} = Average of purchase price, // a title that form a unique name<br>
     * {@link #getDesc()} = The average of purchase price, // description<br>
     * {@link #getAttribute()} = CalculatedPropertyAttribute.NO_ATTR, // can be ALL or ANY in case of COLLECTIONAL_EXPRESSION category, otherwise should be NO_ATTR <br>
     * {@link #getOriginationProperty()} = purchasePrice, // the property (in context of contextPath type), on which calc property is based. This is required for
     * AGGREGATED_EXPRESSION (aka Totals) <br>
     * {@link #category()} = CalculatedPropertyCategory.AGGREGATED_COLLECTIONAL_EXPRESSION, // a category for calculated property (inferred) <br>
     * {@link #name()} = averageOfPurchasePrice, // name of calc property (inferred) <br>
     * {@link #path()} = vehicle.replacing, // path of calc property (inferred) <br>
     * {@link #pathAndName()} = vehicle.replacing.averageOfPurchasePrice, // path and name combined together (inferred) <br>
     * {@link #contextType()} = FuelUsage.class, // a context type of a calculated property (inferred) <br>
     * {@link #parentType()} = Vehicle.class, // a type of a calculated property parent (inferred) <br>
     * {@link #resultType()} = BigDecimal.class, // a resultant type of a calculated property expression (inferred) <br>
     * 
     */
    ICalculatedProperty setDesc(final String desc);

    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////// Required contextually and mutable stuff /////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * An enumeration that defines ALL / ANY attributes for collectional expressions.
     * 
     * @author TG Team
     * 
     */
    public enum CalculatedPropertyAttribute implements IPropertyEnum {
        /** ALL attributes for collectional expressions. */
        ALL("All", "All"),
        /** ANY attribute for collectional expressions. */
        ANY("Any", "Any"),
        /** An empty attribute for collectional expressions (and also a placeholder for other types of expressions). */
        NO_ATTR("No attribute", "No attribute");

        /**
         * Represents the enumeration component title.
         */
        private final String title;

        /**
         * Represents the enumeration component description;
         */
        private final String desc;

        /**
         * Initiates this Calculated property component with specified title and description.
         * 
         * @param title
         * @param desc
         */
        private CalculatedPropertyAttribute(final String title, final String desc) {
            this.title = title;
            this.desc = desc;
        }

        @Override
        public String getTooltip() {
            return desc;
        }

        @Override
        public final String toString() {
            return super.toString(); // this should not be modified!
        }

        @Override
        public String getTitle() {
            return title;
        }
    }

    /**
     * An attribute for a calculated property. Can be ALL or ANY (or NO_ATTR) in case of COLLECTIONAL_EXPRESSION category, otherwise should be NO_ATTR.
     * 
     * For e.g. :<br>
     * {@link #getRoot()} = WorkOrder.class, // a root of a calculated property<br>
     * {@link #getContextPath()} = vehicle.replacing.fuelUsages, // a context path of the calculated property<br>
     * {@link #getContextualExpression()} = AVG(purchasePrice), // should be expressed in context of contextPath type<br>
     * {@link #getTitle()} = Average of purchase price, // a title that form a unique name<br>
     * {@link #getDesc()} = The average of purchase price, // description<br>
     * {@link #getAttribute()} = CalculatedPropertyAttribute.NO_ATTR, // can be ALL or ANY in case of COLLECTIONAL_EXPRESSION category, otherwise should be NO_ATTR <br>
     * {@link #getOriginationProperty()} = purchasePrice, // the property (in context of contextPath type), on which calc property is based. This is required for
     * AGGREGATED_EXPRESSION (aka Totals) <br>
     * {@link #category()} = CalculatedPropertyCategory.AGGREGATED_COLLECTIONAL_EXPRESSION, // a category for calculated property (inferred) <br>
     * {@link #name()} = averageOfPurchasePrice, // name of calc property (inferred) <br>
     * {@link #path()} = vehicle.replacing, // path of calc property (inferred) <br>
     * {@link #pathAndName()} = vehicle.replacing.averageOfPurchasePrice, // path and name combined together (inferred) <br>
     * {@link #contextType()} = FuelUsage.class, // a context type of a calculated property (inferred) <br>
     * {@link #parentType()} = Vehicle.class, // a type of a calculated property parent (inferred) <br>
     * {@link #resultType()} = BigDecimal.class, // a resultant type of a calculated property expression (inferred) <br>
     * 
     * @return
     */
    CalculatedPropertyAttribute getAttribute();

    /**
     * Sets an attribute for a calculated property. Can be ALL or ANY (or NO_ATTR) in case of COLLECTIONAL_EXPRESSION category, otherwise should be NO_ATTR.
     * 
     * For e.g. :<br>
     * {@link #getRoot()} = WorkOrder.class, // a root of a calculated property<br>
     * {@link #getContextPath()} = vehicle.replacing.fuelUsages, // a context path of the calculated property<br>
     * {@link #getContextualExpression()} = AVG(purchasePrice), // should be expressed in context of contextPath type<br>
     * {@link #getTitle()} = Average of purchase price, // a title that form a unique name<br>
     * {@link #getDesc()} = The average of purchase price, // description<br>
     * {@link #getAttribute()} = CalculatedPropertyAttribute.NO_ATTR, // can be ALL or ANY in case of COLLECTIONAL_EXPRESSION category, otherwise should be NO_ATTR <br>
     * {@link #getOriginationProperty()} = purchasePrice, // the property (in context of contextPath type), on which calc property is based. This is required for
     * AGGREGATED_EXPRESSION (aka Totals) <br>
     * {@link #category()} = CalculatedPropertyCategory.AGGREGATED_COLLECTIONAL_EXPRESSION, // a category for calculated property (inferred) <br>
     * {@link #name()} = averageOfPurchasePrice, // name of calc property (inferred) <br>
     * {@link #path()} = vehicle.replacing, // path of calc property (inferred) <br>
     * {@link #pathAndName()} = vehicle.replacing.averageOfPurchasePrice, // path and name combined together (inferred) <br>
     * {@link #contextType()} = FuelUsage.class, // a context type of a calculated property (inferred) <br>
     * {@link #parentType()} = Vehicle.class, // a type of a calculated property parent (inferred) <br>
     * {@link #resultType()} = BigDecimal.class, // a resultant type of a calculated property expression (inferred) <br>
     * 
     * @param attribute
     */
    ICalculatedProperty setAttribute(final CalculatedPropertyAttribute attribute);

    /**
     * A name of property in context type ({@link #contextType()}), from which this calculated property has been originated.
     * 
     * For e.g. :<br>
     * {@link #getRoot()} = WorkOrder.class, // a root of a calculated property<br>
     * {@link #getContextPath()} = vehicle.replacing.fuelUsages, // a context path of the calculated property<br>
     * {@link #getContextualExpression()} = AVG(purchasePrice), // should be expressed in context of contextPath type<br>
     * {@link #getTitle()} = Average of purchase price, // a title that form a unique name<br>
     * {@link #getDesc()} = The average of purchase price, // description<br>
     * {@link #getAttribute()} = CalculatedPropertyAttribute.NO_ATTR, // can be ALL or ANY in case of COLLECTIONAL_EXPRESSION category, otherwise should be NO_ATTR <br>
     * {@link #getOriginationProperty()} = purchasePrice, // the property (in context of contextPath type), on which calc property is based. This is required for
     * AGGREGATED_EXPRESSION (aka Totals) <br>
     * {@link #category()} = CalculatedPropertyCategory.AGGREGATED_COLLECTIONAL_EXPRESSION, // a category for calculated property (inferred) <br>
     * {@link #name()} = averageOfPurchasePrice, // name of calc property (inferred) <br>
     * {@link #path()} = vehicle.replacing, // path of calc property (inferred) <br>
     * {@link #pathAndName()} = vehicle.replacing.averageOfPurchasePrice, // path and name combined together (inferred) <br>
     * {@link #contextType()} = FuelUsage.class, // a context type of a calculated property (inferred) <br>
     * {@link #parentType()} = Vehicle.class, // a type of a calculated property parent (inferred) <br>
     * {@link #resultType()} = BigDecimal.class, // a resultant type of a calculated property expression (inferred) <br>
     * 
     * @return
     */
    String getOriginationProperty();

    /**
     * Sets a name of property in context type ({@link #contextType()}), from which this calculated property has been originated.
     * 
     * For e.g. :<br>
     * {@link #getRoot()} = WorkOrder.class, // a root of a calculated property<br>
     * {@link #getContextPath()} = vehicle.replacing.fuelUsages, // a context path of the calculated property<br>
     * {@link #getContextualExpression()} = AVG(purchasePrice), // should be expressed in context of contextPath type<br>
     * {@link #getTitle()} = Average of purchase price, // a title that form a unique name<br>
     * {@link #getDesc()} = The average of purchase price, // description<br>
     * {@link #getAttribute()} = CalculatedPropertyAttribute.NO_ATTR, // can be ALL or ANY in case of COLLECTIONAL_EXPRESSION category, otherwise should be NO_ATTR <br>
     * {@link #getOriginationProperty()} = purchasePrice, // the property (in context of contextPath type), on which calc property is based. This is required for
     * AGGREGATED_EXPRESSION (aka Totals) <br>
     * {@link #category()} = CalculatedPropertyCategory.AGGREGATED_COLLECTIONAL_EXPRESSION, // a category for calculated property (inferred) <br>
     * {@link #name()} = averageOfPurchasePrice, // name of calc property (inferred) <br>
     * {@link #path()} = vehicle.replacing, // path of calc property (inferred) <br>
     * {@link #pathAndName()} = vehicle.replacing.averageOfPurchasePrice, // path and name combined together (inferred) <br>
     * {@link #contextType()} = FuelUsage.class, // a context type of a calculated property (inferred) <br>
     * {@link #parentType()} = Vehicle.class, // a type of a calculated property parent (inferred) <br>
     * {@link #resultType()} = BigDecimal.class, // a resultant type of a calculated property expression (inferred) <br>
     * 
     * @return
     */
    ICalculatedProperty setOriginationProperty(final String originationProperty);

    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////// Inferred stuff //////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Represents a different categories (types) of <b>calculated properties</b>.<br>
     * <br>
     * 
     * For e.g. {@link CalculatedPropertyCategory#COLLECTIONAL_EXPRESSION}, {@link CalculatedPropertyCategory#EXPRESSION},
     * {@link CalculatedPropertyCategory#AGGREGATED_COLLECTIONAL_EXPRESSION}, {@link CalculatedPropertyCategory#ATTRIBUTED_COLLECTIONAL_EXPRESSION},
     * {@link CalculatedPropertyCategory#AGGREGATED_EXPRESSION}.<br>
     * <br>
     * 
     * TODO : currently the properties higher from contextPath cannot be used inside expression.
     * 
     * @author TG Team
     * 
     */
    public enum CalculatedPropertyCategory {
        /**
         * The category of <b>calculated properties</b> only with simple hierarchy members -- no members of collectional hierarchy. There are no restrictions for hierarchy levels
         * to be used in this expression (higher or lower in the actual calculated property place).<br>
         * <br>
         * 
         * Example: <br>
         * Place : Vehicle=><i>[status]</i>; <br>
         * Property = <i>[status.operational] and ([lastReading] > 1000 or [replacing.techDetails.amount] < 100)</i><br>
         * <br>
         * 
         * TODO : currently the properties higher from contextPath cannot be used inside expression.
         * 
         */
        EXPRESSION,
        /**
         * The category of <b>calculated properties</b> only with aggregated {@link #EXPRESSION}s. Neither simple nor collectional members are permitted outside of aggregated sub-
         * {@link #EXPRESSION}s in this expression.<br>
         * <br>
         * 
         * Example: <br>
         * Place : Vehicle=><i>[]</i>; <br>
         * Property = <i>2 * <b>SUM(</b>2 * [replacing.techDetails.amount] - [lastReading]<b>)</b> + 3 * <b>AVG(</b>[replacing.lastReading] * 7<b>)</b></i><br>
         * <br>
         * 
         * TODO : currently the properties higher from contextPath cannot be used inside expression.
         * 
         */
        AGGREGATED_EXPRESSION,
        /**
         * The category of <b>calculated properties</b> with simple members and at least one member of collectional hierarchy (the only one collectional hierarchy is permitted, and
         * that hierarchy will be the place of the calculated property).<br>
         * <br>
         * 
         * Example: <br>
         * Place : Vehicle=><i>[fuelUsages]</i>; <br>
         * Property = <i>2 * <b>[fuelUsages.oilQty]</b> - 4 * <b>[fuelUsages.details.oilPrice]</b> - [lastReading]</i><br>
         * <br>
         * 
         * TODO : currently the properties higher from contextPath cannot be used inside expression.
         * 
         */
        COLLECTIONAL_EXPRESSION,
        /**
         * The category of <b>calculated properties</b> with simple hierarchy members and with at least one aggregated {@link #COLLECTIONAL_EXPRESSION}.<br>
         * <br>
         * 
         * Example: <br>
         * Place : Vehicle=><i>[]</i>; <br>
         * Property = <i><b>SUM(</b>2 * [fuelUsages.oilQty] - [lastReading]<b>)</b> + [replacing.lastReading] * 7</i><br>
         * <br>
         * 
         * TODO : currently the properties higher from contextPath cannot be used inside expression.
         * 
         */
        AGGREGATED_COLLECTIONAL_EXPRESSION,
        /**
         * The category of <b>calculated properties</b> with one and only one attributed {@link #COLLECTIONAL_EXPRESSION} (with ALL / ANY attributes). No other expressions are
         * permitted outside of ALL / ANY attribute.<br>
         * <br>
         * 
         * Example: <br>
         * Place : Vehicle=><i>[]</i>; <br>
         * Property = <i><b>ALL(</b>2 * [fuelUsages.oilQty] - [lastReading]<b>)</b></i><br>
         * <br>
         * 
         * TODO : currently the properties higher from contextPath cannot be used inside expression.
         * 
         */
        ATTRIBUTED_COLLECTIONAL_EXPRESSION
    }

    /**
     * Returns a category (type, class -- see {@link CalculatedPropertyCategory}) of calculated property.
     * 
     * For e.g. :<br>
     * {@link #getRoot()} = WorkOrder.class, // a root of a calculated property<br>
     * {@link #getContextPath()} = vehicle.replacing.fuelUsages, // a context path of the calculated property<br>
     * {@link #getContextualExpression()} = AVG(purchasePrice), // should be expressed in context of contextPath type<br>
     * {@link #getTitle()} = Average of purchase price, // a title that form a unique name<br>
     * {@link #getDesc()} = The average of purchase price, // description<br>
     * {@link #getAttribute()} = CalculatedPropertyAttribute.NO_ATTR, // can be ALL or ANY in case of COLLECTIONAL_EXPRESSION category, otherwise should be NO_ATTR <br>
     * {@link #getOriginationProperty()} = purchasePrice, // the property (in context of contextPath type), on which calc property is based. This is required for
     * AGGREGATED_EXPRESSION (aka Totals) <br>
     * {@link #category()} = CalculatedPropertyCategory.AGGREGATED_COLLECTIONAL_EXPRESSION, // a category for calculated property (inferred) <br>
     * {@link #name()} = averageOfPurchasePrice, // name of calc property (inferred) <br>
     * {@link #path()} = vehicle.replacing, // path of calc property (inferred) <br>
     * {@link #pathAndName()} = vehicle.replacing.averageOfPurchasePrice, // path and name combined together (inferred) <br>
     * {@link #contextType()} = FuelUsage.class, // a context type of a calculated property (inferred) <br>
     * {@link #parentType()} = Vehicle.class, // a type of a calculated property parent (inferred) <br>
     * {@link #resultType()} = BigDecimal.class, // a resultant type of a calculated property expression (inferred) <br>
     * 
     * @return
     */
    CalculatedPropertyCategory category();

    /**
     * A name of calculated property in context of parent type ({@link #parentType()}). <br>
     * <br>
     * 
     * For e.g. :<br>
     * {@link #getRoot()} = WorkOrder.class, // a root of a calculated property<br>
     * {@link #getContextPath()} = vehicle.replacing.fuelUsages, // a context path of the calculated property<br>
     * {@link #getContextualExpression()} = AVG(purchasePrice), // should be expressed in context of contextPath type<br>
     * {@link #getTitle()} = Average of purchase price, // a title that form a unique name<br>
     * {@link #getDesc()} = The average of purchase price, // description<br>
     * {@link #getAttribute()} = CalculatedPropertyAttribute.NO_ATTR, // can be ALL or ANY in case of COLLECTIONAL_EXPRESSION category, otherwise should be NO_ATTR <br>
     * {@link #getOriginationProperty()} = purchasePrice, // the property (in context of contextPath type), on which calc property is based. This is required for
     * AGGREGATED_EXPRESSION (aka Totals) <br>
     * {@link #category()} = CalculatedPropertyCategory.AGGREGATED_COLLECTIONAL_EXPRESSION, // a category for calculated property (inferred) <br>
     * {@link #name()} = averageOfPurchasePrice, // name of calc property (inferred) <br>
     * {@link #path()} = vehicle.replacing, // path of calc property (inferred) <br>
     * {@link #pathAndName()} = vehicle.replacing.averageOfPurchasePrice, // path and name combined together (inferred) <br>
     * {@link #contextType()} = FuelUsage.class, // a context type of a calculated property (inferred) <br>
     * {@link #parentType()} = Vehicle.class, // a type of a calculated property parent (inferred) <br>
     * {@link #resultType()} = BigDecimal.class, // a resultant type of a calculated property expression (inferred) <br>
     * 
     * @return
     */
    String name();

    /**
     * A path where the calculated property exists in root type {@link #getRoot()}. It could be different from a {@link #getContextPath()}. <br>
     * <br>
     * 
     * For e.g. :<br>
     * {@link #getRoot()} = WorkOrder.class, // a root of a calculated property<br>
     * {@link #getContextPath()} = vehicle.replacing.fuelUsages, // a context path of the calculated property<br>
     * {@link #getContextualExpression()} = AVG(purchasePrice), // should be expressed in context of contextPath type<br>
     * {@link #getTitle()} = Average of purchase price, // a title that form a unique name<br>
     * {@link #getDesc()} = The average of purchase price, // description<br>
     * {@link #getAttribute()} = CalculatedPropertyAttribute.NO_ATTR, // can be ALL or ANY in case of COLLECTIONAL_EXPRESSION category, otherwise should be NO_ATTR <br>
     * {@link #getOriginationProperty()} = purchasePrice, // the property (in context of contextPath type), on which calc property is based. This is required for
     * AGGREGATED_EXPRESSION (aka Totals) <br>
     * {@link #category()} = CalculatedPropertyCategory.AGGREGATED_COLLECTIONAL_EXPRESSION, // a category for calculated property (inferred) <br>
     * {@link #name()} = averageOfPurchasePrice, // name of calc property (inferred) <br>
     * {@link #path()} = vehicle.replacing, // path of calc property (inferred) <br>
     * {@link #pathAndName()} = vehicle.replacing.averageOfPurchasePrice, // path and name combined together (inferred) <br>
     * {@link #contextType()} = FuelUsage.class, // a context type of a calculated property (inferred) <br>
     * {@link #parentType()} = Vehicle.class, // a type of a calculated property parent (inferred) <br>
     * {@link #resultType()} = BigDecimal.class, // a resultant type of a calculated property expression (inferred) <br>
     * 
     * @return
     */
    String path();

    /**
     * The calculated property {@link #path()} combined with a {@link #name()}.<br>
     * <br>
     * 
     * For e.g. :<br>
     * {@link #getRoot()} = WorkOrder.class, // a root of a calculated property<br>
     * {@link #getContextPath()} = vehicle.replacing.fuelUsages, // a context path of the calculated property<br>
     * {@link #getContextualExpression()} = AVG(purchasePrice), // should be expressed in context of contextPath type<br>
     * {@link #getTitle()} = Average of purchase price, // a title that form a unique name<br>
     * {@link #getDesc()} = The average of purchase price, // description<br>
     * {@link #getAttribute()} = CalculatedPropertyAttribute.NO_ATTR, // can be ALL or ANY in case of COLLECTIONAL_EXPRESSION category, otherwise should be NO_ATTR <br>
     * {@link #getOriginationProperty()} = purchasePrice, // the property (in context of contextPath type), on which calc property is based. This is required for
     * AGGREGATED_EXPRESSION (aka Totals) <br>
     * {@link #category()} = CalculatedPropertyCategory.AGGREGATED_COLLECTIONAL_EXPRESSION, // a category for calculated property (inferred) <br>
     * {@link #name()} = averageOfPurchasePrice, // name of calc property (inferred) <br>
     * {@link #path()} = vehicle.replacing, // path of calc property (inferred) <br>
     * {@link #pathAndName()} = vehicle.replacing.averageOfPurchasePrice, // path and name combined together (inferred) <br>
     * {@link #contextType()} = FuelUsage.class, // a context type of a calculated property (inferred) <br>
     * {@link #parentType()} = Vehicle.class, // a type of a calculated property parent (inferred) <br>
     * {@link #resultType()} = BigDecimal.class, // a resultant type of a calculated property expression (inferred) <br>
     * 
     * @return
     */
    String pathAndName();

    /**
     * The type of context path for the calculated property. {@link #getContextualExpression()} should be fully defined in context of this type. <br>
     * <br>
     * 
     * For e.g. :<br>
     * {@link #getRoot()} = WorkOrder.class, // a root of a calculated property<br>
     * {@link #getContextPath()} = vehicle.replacing.fuelUsages, // a context path of the calculated property<br>
     * {@link #getContextualExpression()} = AVG(purchasePrice), // should be expressed in context of contextPath type<br>
     * {@link #getTitle()} = Average of purchase price, // a title that form a unique name<br>
     * {@link #getDesc()} = The average of purchase price, // description<br>
     * {@link #getAttribute()} = CalculatedPropertyAttribute.NO_ATTR, // can be ALL or ANY in case of COLLECTIONAL_EXPRESSION category, otherwise should be NO_ATTR <br>
     * {@link #getOriginationProperty()} = purchasePrice, // the property (in context of contextPath type), on which calc property is based. This is required for
     * AGGREGATED_EXPRESSION (aka Totals) <br>
     * {@link #category()} = CalculatedPropertyCategory.AGGREGATED_COLLECTIONAL_EXPRESSION, // a category for calculated property (inferred) <br>
     * {@link #name()} = averageOfPurchasePrice, // name of calc property (inferred) <br>
     * {@link #path()} = vehicle.replacing, // path of calc property (inferred) <br>
     * {@link #pathAndName()} = vehicle.replacing.averageOfPurchasePrice, // path and name combined together (inferred) <br>
     * {@link #contextType()} = FuelUsage.class, // a context type of a calculated property (inferred) <br>
     * {@link #parentType()} = Vehicle.class, // a type of a calculated property parent (inferred) <br>
     * {@link #resultType()} = BigDecimal.class, // a resultant type of a calculated property expression (inferred) <br>
     * 
     * @return
     */
    Class<?> contextType();

    /**
     * The parent type of calculated property (the type of {@link #path()}, where the property actually exists).<br>
     * <br>
     * 
     * For e.g. :<br>
     * {@link #getRoot()} = WorkOrder.class, // a root of a calculated property<br>
     * {@link #getContextPath()} = vehicle.replacing.fuelUsages, // a context path of the calculated property<br>
     * {@link #getContextualExpression()} = AVG(purchasePrice), // should be expressed in context of contextPath type<br>
     * {@link #getTitle()} = Average of purchase price, // a title that form a unique name<br>
     * {@link #getDesc()} = The average of purchase price, // description<br>
     * {@link #getAttribute()} = CalculatedPropertyAttribute.NO_ATTR, // can be ALL or ANY in case of COLLECTIONAL_EXPRESSION category, otherwise should be NO_ATTR <br>
     * {@link #getOriginationProperty()} = purchasePrice, // the property (in context of contextPath type), on which calc property is based. This is required for
     * AGGREGATED_EXPRESSION (aka Totals) <br>
     * {@link #category()} = CalculatedPropertyCategory.AGGREGATED_COLLECTIONAL_EXPRESSION, // a category for calculated property (inferred) <br>
     * {@link #name()} = averageOfPurchasePrice, // name of calc property (inferred) <br>
     * {@link #path()} = vehicle.replacing, // path of calc property (inferred) <br>
     * {@link #pathAndName()} = vehicle.replacing.averageOfPurchasePrice, // path and name combined together (inferred) <br>
     * {@link #contextType()} = FuelUsage.class, // a context type of a calculated property (inferred) <br>
     * {@link #parentType()} = Vehicle.class, // a type of a calculated property parent (inferred) <br>
     * {@link #resultType()} = BigDecimal.class, // a resultant type of a calculated property expression (inferred) <br>
     * 
     * @return
     */
    Class<?> parentType();

    /**
     * A result type of calculated property (the type of resultant {@link #getContextualExpression()}).
     * 
     * For e.g. :<br>
     * {@link #getRoot()} = WorkOrder.class, // a root of a calculated property<br>
     * {@link #getContextPath()} = vehicle.replacing.fuelUsages, // a context path of the calculated property<br>
     * {@link #getContextualExpression()} = AVG(purchasePrice), // should be expressed in context of contextPath type<br>
     * {@link #getTitle()} = Average of purchase price, // a title that form a unique name<br>
     * {@link #getDesc()} = The average of purchase price, // description<br>
     * {@link #getAttribute()} = CalculatedPropertyAttribute.NO_ATTR, // can be ALL or ANY in case of COLLECTIONAL_EXPRESSION category, otherwise should be NO_ATTR <br>
     * {@link #getOriginationProperty()} = purchasePrice, // the property (in context of contextPath type), on which calc property is based. This is required for
     * AGGREGATED_EXPRESSION (aka Totals) <br>
     * {@link #category()} = CalculatedPropertyCategory.AGGREGATED_COLLECTIONAL_EXPRESSION, // a category for calculated property (inferred) <br>
     * {@link #name()} = averageOfPurchasePrice, // name of calc property (inferred) <br>
     * {@link #path()} = vehicle.replacing, // path of calc property (inferred) <br>
     * {@link #pathAndName()} = vehicle.replacing.averageOfPurchasePrice, // path and name combined together (inferred) <br>
     * {@link #contextType()} = FuelUsage.class, // a context type of a calculated property (inferred) <br>
     * {@link #parentType()} = Vehicle.class, // a type of a calculated property parent (inferred) <br>
     * {@link #resultType()} = BigDecimal.class, // a resultant type of a calculated property expression (inferred) <br>
     * 
     * @return
     */
    Class<?> resultType();

    /**
     * Returns an expression model that is currently associated with calculated property. The expression model is typically changed when {@link #setContextualExpression(String)}
     * invokes. If expression model has not been initialised -- it returns <code>null</code>.
     * 
     * @return
     */
    ExpressionModel getExpressionModel();

    @Override
    public boolean equals(Object obj);

    @Override
    public int hashCode();
}
