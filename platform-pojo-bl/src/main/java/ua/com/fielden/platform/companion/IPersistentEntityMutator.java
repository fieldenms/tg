package ua.com.fielden.platform.companion;

import ua.com.fielden.platform.entity.AbstractEntity;

/// A contract that combines [IPersistentEntityDeleter] and [IEntityActuator] as a convenience in cases where
/// both sets of mutating methods (save and delete) are required.
///
/// In addition, this contract adds method [#quickSave(AbstractEntity)], which could only be applicable to persistent entities.
///
public interface IPersistentEntityMutator<T extends AbstractEntity<?>> extends IPersistentEntityDeleter<T>, IEntityActuator<T> {

    /// Similar to method [#save(AbstractEntity)], but applicable only to *persistent* entities.
    /// It returns an `id` of the saved entity.
    ///
    /// The implication is that this method should execute faster by skipping the steps required to re-fetch the resultant entity.
    /// This method is relevant only for simple cases where method `save` is not overridden to provide an application specific logic.
    ///
    /// **Important:** A more modern and better alternative is method `save(T entity, Optional<fetch<T>> maybeFetch)` in class `CommonEntityDao`.
    /// Prefer that method over [#quickSave(AbstractEntity)].
    ///
    default long quickSave(final T entity) {
        throw new UnsupportedOperationException(); 
    }

}
