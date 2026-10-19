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
/// A domain consists of a tree of properties, which can be enriched with calculated properties in a particular branch of the hierarchy.
///
/// Calculated properties can be added / removed / changed through this interface.
/// Mutated root domain entities meta-information can be obtained via [#getManagedType].
///
/// @see ICalculatedProperty
///
public interface IDomainTreeEnhancer extends IRootTyped {

    /// Applies (commits) all domain changes.
    /// The **apply** action is necessary to complete domain enhancements and to load the adjusted domain into memory.
    ///
    void apply();

    /// Discards (removes, rollbacks, cancels) all domain changes.
    /// The **discard** action provides the ability to start enhancements again.
    ///
    void discard();

    /// Returns the managed type for `type`.
    /// The managed type may be enhanced with calculated properties.
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

    Map<Class<?>, List<CalculatedProperty>> calculatedProperties();

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
