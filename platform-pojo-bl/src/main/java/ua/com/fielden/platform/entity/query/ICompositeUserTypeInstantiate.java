package ua.com.fielden.platform.entity.query;

import java.util.Map;

/// A contract for Hibernate types representing composite values that supports their instantiation from a persisted representation.
///
/// @param <T>  the type of values instantiated by the implementaiton of this contract
///
public interface ICompositeUserTypeInstantiate<T> {

    /// Creates a composite value from a map of components.
    ///
    /// @param arguments  a map of component values `{name: value}`, where each value has already been instantiated from
    ///                   its persisted representation.
    ///
    T instantiate(Map<String, Object> arguments);

    /// Returns the names of components supported by this Hibernate type mapping.
    ///
    String[] getPropertyNames();

    /// Returns the type of values instantiated by the implementaiton of this contract.
    ///
    Class<T> returnedClass();

    /// Returns the Hibernate types of components supported by this Hibernate type mapping.
    ///
    Object[] getPropertyTypes();

}
