package ua.com.fielden.platform.companion;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;

/// This interface defines the generator of entity companion object types.
///
public interface ICompanionGenerator {

    /// Returns a companion object implementation for the specified entity type, generating it on first request.
    ///
    /// The generated type will implement [IEntityDao].
    ///
    /// A companion type is generated at most once per base entity type: all requests for the same base entity type return the same type.
    /// Implementations must uphold this contract, because each generated type is a new class that cannot be unloaded once instantiated by the injector.
    ///
    Class<?> generateCompanion(Class<? extends AbstractEntity<?>> type);

}
