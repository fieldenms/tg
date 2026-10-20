package ua.com.fielden.platform.domaintree;

import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.domaintree.impl.CalculatedProperty;
import ua.com.fielden.platform.domaintree.impl.CustomProperty;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.error.Warning;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/// This interface defines how a domain can be enhanced with **calculated properties**.
///
/// A domain consists of a set of entity types, each of which can be enriched with calculated properties.
/// Calculated properties can be added / removed / changed through this interface.
///
/// Methods that modify the domain, such as [#addCalculatedProperty], do not take effect immediately.
/// Their results are stored in memory and must be _applied_ by calling [#apply], for the changes to take effect.
/// This is true for all methods unless specified otherwise.
///
/// Enhanced domain types can be obtained via [#getManagedType].
///
/// @see ICalculatedProperty
///
public interface IDomainTreeEnhancer extends IRootTyped {

    /// Applies all domain changes.
    ///
    void apply();

    /// Discards all unapplied domain changes.
    /// Enhancements that have already been applied are unaffected.
    ///
    /// Calling [#apply] after this method has no effect, as only the changes stored in memory are discarded.
    /// To also discard changes that have already been applied, use [#restoreAll].
    ///
    void discard();

    /// Restores all domain types to their original forms, removing all previously applied and not yet applied enhancements.
    /// This method **does not** apply changes.
    ///
    void restoreAll();

    /// Returns the managed type for `type`.
    /// Returns the same type when `type` is not a "root" type.
    ///
    Class<?> getManagedType(Class<?> type);
    
    /// Adds `calculatedProperty` to the hierarchy of its root type.
    ///
    /// **Important**: `calculatedProperty` should be expressed strictly in the context of [ICalculatedProperty#contextType] hierarchy.
    ///
    /// @param calculatedProperty  fully defined calculated property to be added
    /// @throws IncorrectCalcPropertyException  when the calculated property is incorrect
    ///
    ICalculatedProperty addCalculatedProperty(ICalculatedProperty calculatedProperty);

    /// Creates a new calculated property based on the provided meta-information and adds it to the hierarchy of its root type.
    ///
    /// @throws IncorrectCalcPropertyException  when the calculated property is incorrect
    ///
    ICalculatedProperty addCalculatedProperty(
            Class<?> root,
            String contextPath,
            String contextualExpression,
            String title,
            String desc,
            CalculatedPropertyAttribute attribute,
            String originationProperty,
            Integer precision,
            Integer scale);

    /// Creates a new calculated property based on the provided meta-information and adds it to the hierarchy of its root type.
    ///
    /// @throws IncorrectCalcPropertyException  when the calculated property is incorrect
    ///
    ICalculatedProperty addCalculatedProperty(
            Class<?> root,
            String contextPath,
            String customPropertyName,
            String contextualExpression,
            String title,
            String desc,
            CalculatedPropertyAttribute attribute,
            String originationProperty,
            Integer precision,
            Integer scale);

    /// Removes the calculated property whose path is `propertyPath` from `rootType` hierarchy.
    ///
    /// @param rootType  type of a **root** entity, from which the calculated property should be removed (not derived type)
    /// @param propertyPath  the property path of the calculated property to be removed
    /// @throws IncorrectCalcPropertyException  when the calculated property is incorrect
    ///
    void removeCalculatedProperty(Class<?> rootType, String propertyPath);

    /// Returns a calculated property whose path is `propertyPath` from `rootType` hierarchy.
    ///
    /// @param rootType  type of **root** entity, from which the calculated property should be obtained (not derived type)
    /// @param propertyPath  the property path of the requested calculated property
    /// @throws IncorrectCalcPropertyException  when the calculated property is incorrect
    ///
    ICalculatedProperty getCalculatedProperty(Class<?> rootType, String propertyPath);

    /// Finds a calculated property whose path is `propertyPath` from `rootType` hierarchy.
    ///
    /// @param rootType  type of **root** entity, from which the calculated property should be obtained (not derived type)
    /// @param propertyPath  the property path of the requested calculated property
    ///
    Optional<ICalculatedProperty> findCalculatedProperty(Class<?> rootType, String propertyPath);

    /// Copies a calculated property whose path is `propertyPath` from `rootType` hierarchy.
    ///
    /// @param rootType  type of **root** entity, from which the calculated property should be copied (not derived type)
    /// @param propertyPath  the property path of the requested calculated property
    /// @throws IncorrectCalcPropertyException  when the calculated property is incorrect
    ///
    ICalculatedProperty copyCalculatedProperty(Class<?> rootType, String propertyPath);

    /// Represents a situation when a calculated property is incorrect.
    ///
    class IncorrectCalcPropertyException extends Result {
        private static final long serialVersionUID = 435410515344805056L;

        public IncorrectCalcPropertyException(final String msg) {
            this(msg, null);
        }

        public IncorrectCalcPropertyException(final String msg, final Exception cause) {
            // The created Exception will capture a stack trace, hence no need to do the same for Result.
            super(null, new Exception(msg, cause), false);
        }
    }

    /// Represents a situation when a calculated property is correct but some warning exists.
    ///
    class CalcPropertyWarning extends Warning {
        private static final long serialVersionUID = 435410515344805056L;

        public CalcPropertyWarning(final String s) {
            super(s);
        }
    }

    /// Current snapshot of calculated properties.
    ///
    Map<Class<?>, List<CalculatedProperty>> calculatedProperties();

    /// Current snapshot of custom properties.
    ///
    Map<Class<?>, List<CustomProperty>> customProperties();

    Map<Class<?>, Class<?>> originalAndEnhancedRootTypes();

    EntityFactory getFactory();

    /// Creates a new custom property based on the provided meta-information and adds it to the root type's hierarchy.
    ///
    /// @throws IncorrectCalcPropertyException when the property is incorrect in context of other properties
    ///
    IDomainTreeEnhancer addCustomProperty(
            Class<?> root,
            String contextPath,
            String name,
            String title,
            String desc,
            Class<?> type,
            Integer precision,
            Integer scale);

}
