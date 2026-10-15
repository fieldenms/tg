package ua.com.fielden.platform.domaintree;

import jakarta.annotation.Nullable;
import ua.com.fielden.platform.basic.IPropertyEnum;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;

/// **Calculated property** is an abstraction for an expression that can be used in queries and their results like an ordinary property.
///
/// **Important:** it is necessary to override [#equals] and [#hashCode] in implementors to provide logical comparison of instances.
///
/// For example, for a root entity `Vehicle`:
///
/// 1. `Vehicle.doubleTanksQty = 2 * SUM(Vehicle.fuelUsages.oilQuantity) / Vehicle.techDetails.tankCapacity`
///
///    Collectional aggregation, could contain an _aggregated collectional_ atoms or simple atoms -- adds as domain tree extension, can be queried/resulted.
///
/// 2. `Vehicle.readingWarrantyBalance = 3 * Vehicle.lastReading - Vehicle.techDetails.warrantyKms`
///
///    Several properties inside an expression -- adds as domain tree extension, can be queried/resulted.
///
/// 3. `Vehicle.yearsFrom1970OfInitDate = YEAR(Vehicle.initDate) - 1970`
///
///    Date or another property inside an expression -- adds as domain tree extension, can be queried/resulted.
///
/// 4. `Vehicle.averageReading = AVG(Vehicle.readingWarrantyBalance - 100000)`
///
///    Totals / analysisAggregation expression -- calculated property strictly **assigned** to a property from which it is originated, appears in "origination" property context only.
///
/// Example of an [ICalculatedProperty] instance:
///
/// * [#getRoot()] = `WorkOrder.class`
///
/// * [#getContextPath()] = `vehicle.replacing.fuelUsages`
///
/// * [#getContextualExpression()] = `AVG(purchasePrice)`, should be expressed in the context of [#contextType]
///
/// * [#getTitle()] = `Average of purchase price`, a title that forms a unique name
///
/// * [#getDesc()] = `The average of purchase price`
///
/// * [#getAttribute()] = [CalculatedPropertyAttribute#NO_ATTR]
///
/// * [#getOriginationProperty()] = `purchasePrice`
///
/// * [#category()] = [CalculatedPropertyCategory#AGGREGATED_COLLECTIONAL_EXPRESSION]
///
/// * [#name()] = `averageOfPurchasePrice`
///
/// * [#path()] = `vehicle.replacing`
///
/// * [#pathAndName()] = `vehicle.replacing.averageOfPurchasePrice`
///
/// * [#contextType()] = `FuelUsage.class`
///
/// * [#parentType()] = `Vehicle.class`
///
/// * [#resultType()] = `BigDecimal.class`
///
public interface ICalculatedProperty extends IProperty {

    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////// Required and mutable stuff //////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////

    /// Returns an expression string in **eQuery manner** that defines the calculated property.
    /// The expression should be fully defined in context of [#contextType()].
    /// Concrete parts of expression (simple or other calculated properties) should be incorporated into this expression using dot-notation.
    ///
    String getContextualExpression();

    /// Sets an expression string in **eQuery manner** that defines a calculated property.
    /// The expression should be fully defined in context of [#contextType()].
    /// Concrete parts of expression (simple or other calculated properties) should be incorporated into this expression using dot-notation.
    ///
    /// @param contextualExpression  an expression string in **eQuery manner** that defines the calculated property
    ///
    ICalculatedProperty setContextualExpression(String contextualExpression);

    /// Sets the title.
    ///
    ICalculatedProperty setTitle(String title);

    /// Sets the description.
    ///
    ICalculatedProperty setDesc(String desc);

    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////// Required contextually and mutable stuff /////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////

    /// An enumeration that defines ALL / ANY attributes for collectional expressions.
    ///
    enum CalculatedPropertyAttribute implements IPropertyEnum {
        /// ALL attributes for collectional expressions.
        ALL("All", "All"),
        /// ANY attribute for collectional expressions.
        ANY("Any", "Any"),
        /// An empty attribute for collectional expressions (and also a placeholder for other types of expressions).
        NO_ATTR("No attribute", "No attribute");

        /// Represents the enumeration component title.
        private final String title;

        /// Represents the enumeration component description;
        private final String desc;

        CalculatedPropertyAttribute(final String title, final String desc) {
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

    /// An attribute for the calculated property.
    /// Can be [CalculatedPropertyAttribute#ALL] or [CalculatedPropertyAttribute#ANY] (or [CalculatedPropertyAttribute#NO_ATTR])
    /// in case of [CalculatedPropertyCategory#COLLECTIONAL_EXPRESSION] category, otherwise should be [CalculatedPropertyAttribute#NO_ATTR].
    ///
    CalculatedPropertyAttribute getAttribute();

    /// Sets the attribute.
    /// Can be [CalculatedPropertyAttribute#ALL] or [CalculatedPropertyAttribute#ANY] (or [CalculatedPropertyAttribute#NO_ATTR])
    /// in case of [CalculatedPropertyCategory#COLLECTIONAL_EXPRESSION] category, otherwise should be [CalculatedPropertyAttribute#NO_ATTR].
    ///
    ICalculatedProperty setAttribute(CalculatedPropertyAttribute attribute);

    /// The name of the property in the context type ([#contextType()]), from which this calculated property originates.
    /// This is required for [CalculatedPropertyCategory#AGGREGATED_EXPRESSION] (aka Totals).
    ///
    String getOriginationProperty();

    /// Sets the name of the property in context type ([#contextType()]), from which this calculated property originates.
    ///
    ICalculatedProperty setOriginationProperty(String originationProperty);

    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////// Inferred stuff //////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////

    /// Represents different categories (types) of calculated properties.
    ///
    /// TODO : currently the properties higher from contextPath cannot be used inside expression.
    ///
    enum CalculatedPropertyCategory {

        /// Implicit calculated properties represent either one-2-one or one-2-many relationships.
        /// Properties with this category should only be defined for "master" entities, representing the left side of a relationship.
        ///
        IMPLICIT,

        /// The category of calculated properties with simple hierarchy members only -- no members of collectional hierarchy.
        /// There are no restrictions for hierarchy levels to be used in this expression (higher or lower in the actual calculated property place).
        ///
        /// Example:
        /// * Place: `Vehicle.status`
        /// * Property: `status.operational and (lastReading > 1000 or replacing.techDetails.amount < 100)`
        ///
        /// TODO : currently the properties higher from contextPath cannot be used inside expression.
        ///
        EXPRESSION,

        /// The category of calculated properties with aggregated [#EXPRESSION]s.
        /// Neither simple nor collectional members are permitted outside of aggregated sub-[#EXPRESSION]s in this expression.
        ///
        /// Example:
        /// * Place: `Vehicle.[]`
        /// * Property: `2 * SUM(2 * replacing.techDetails.amount - lastReading) + 3 * AVG(replacing.lastReading * 7)`
        ///
        /// TODO : currently the properties higher from contextPath cannot be used inside expression.
        ///
        AGGREGATED_EXPRESSION,

        /// The category of calculated properties with simple members and at least one member of collectional hierarchy
        /// (the only one collectional hierarchy is permitted, and that hierarchy will be the place of the calculated property).
        ///
        /// Example:
        /// * Place: `Vehicle.fuelUsages`
        /// * Property: `2 * fuelUsages.oilQty - 4 * fuelUsages.details.oilPrice - lastReading`
        ///
        /// TODO : currently the properties higher from contextPath cannot be used inside expression.
        ///
        COLLECTIONAL_EXPRESSION,

        /// The category of calculated properties with simple hierarchy members and with at least one aggregated [#COLLECTIONAL_EXPRESSION].
        ///
        /// Example:
        /// * Place: `Vehicle.[]`
        /// * Property: `SUM(2 * fuelUsages.oilQty - lastReading) + replacing.lastReading * 7`
        ///
        /// TODO : currently the properties higher from contextPath cannot be used inside expression.
        ///
        AGGREGATED_COLLECTIONAL_EXPRESSION,

        /// The category of calculated properties with one and only one attributed [#COLLECTIONAL_EXPRESSION] (with ALL / ANY attributes).
        /// No other expressions are permitted outside of ALL / ANY attribute.
        ///
        /// Example:
        /// * Place : `Vehicle.[]`
        /// * Property: `ALL(2 * fuelUsages.oilQty - lastReading)`
        ///
        /// TODO : currently the properties higher from contextPath cannot be used inside expression.
        ///
        ATTRIBUTED_COLLECTIONAL_EXPRESSION

    }

    /// Returns the property's category.
    ///
    CalculatedPropertyCategory category();

    /// The name of the calculated property in the context of parent type ([#parentType()]).
    ///
    @Override
    String name();

    /// The property's [#path()] combined with its [#name()].
    ///
    String pathAndName();

    /// The type of the context path for this property.
    /// [#getContextualExpression()] should be fully defined in context of this type.
    ///
    Class<?> contextType();

    /// The property's parent type -- the type of [#path()], where the property actually exists.
    ///
    Class<?> parentType();

    /// The property's type -- the type of the [expression's][#getContextualExpression()] result.
    ///
    @Override
    Class<?> resultType();

    /// Returns the expression model that is currently associated with this property, or `null` if none is.
    /// It is typically changed with [#setContextualExpression(String)].
    ///
    @Nullable ExpressionModel getExpressionModel();

    @Override
    boolean equals(Object obj);

    @Override
    int hashCode();

}
