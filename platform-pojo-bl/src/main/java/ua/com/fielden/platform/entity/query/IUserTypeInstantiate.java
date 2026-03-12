package ua.com.fielden.platform.entity.query;

import jakarta.annotation.Nullable;
import ua.com.fielden.platform.entity.factory.EntityFactory;

/// A contract for Hibernate types representing scalar values that supports their instantiation from a persisted value.
///
/// @param <T>  the type of values instantiated by the implementaiton of this contract
///
public interface IUserTypeInstantiate<T> {

    /// Creates an instance from its persisted representation `argument`.
    ///
    /// @param factory  an entity factory provided for those cases where an entity needs to be created.
    ///
    T instantiate(@Nullable Object argument, EntityFactory factory);

}
