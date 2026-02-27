package ua.com.fielden.platform.entity;

import com.google.inject.ImplementedBy;
import ua.com.fielden.platform.entity.exceptions.EntityDefinitionException;

/// Performs runtime verification of entity types, ensuring that property declarations and other structural components
/// satisfy invariants that are not enforced statically.
///
@ImplementedBy(EntityTypeVerifier.class)
public interface IEntityTypeVerifier {

    /// Verifies the entity type, throwing an exception if verification fails.
    ///
    void verify(Class<? extends AbstractEntity<?>> entityType) throws EntityDefinitionException;

}
