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

    /// Returns the number of companion types generated so far, which is the number of base entity types with a generated companion type.
    ///
    /// It is intended for application services that periodically report cache sizes, such as the size of [ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader].
    /// The number levels off at the number of base entity types whose generated companions are requested.
    /// If Metaspace keeps growing while this number stays the same, the growth is not caused by companion types generated through this generator.
    ///
    int generatedTypeCount();

}
