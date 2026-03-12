package ua.com.fielden.platform.entity.query;

import jakarta.annotation.Nullable;
import ua.com.fielden.platform.entity.factory.EntityFactory;

/// A contract for Hibernate types representing scalar values that supports their instantiation from a persisted value.
///
public interface IUserTypeInstantiate {

    /// Creates an instance from its persisted representation `argument`.
    ///
    /// @param factory  an entity factory provided for those cases where an entity needs to be created.
    ///
    Object instantiate(@Nullable Object argument, EntityFactory factory);

}
