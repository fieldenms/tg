package ua.com.fielden.platform.companion;

import ua.com.fielden.platform.entity.AbstractEntity;

import java.util.stream.Stream;

/// A contract that combines [IPersistentEntityDeleter] and [IEntityActuator] as a convenience in cases where
/// both sets of mutating methods (save and delete) are required.
///
/// In addition, this contract adds method [#quickSave(AbstractEntity)], which could only be applicable to persistent entities.
///
public interface IPersistentEntityMutator<T extends AbstractEntity<?>> extends IPersistentEntityDeleter<T>, IEntityActuator<T> {

    String ERR_BATCH_INSERTION_IS_UNSUPPORTED = "By default batch insertion is not supported. Carefully consider if it is appropriate in your case.";

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

    /// Performs batch insertion of new entities for optimised database performance.
    ///
    /// This method inserts multiple new entities in batches rather than individually,
    /// which can significantly improve performance when dealing with large volumes of data.
    /// The entities are processed in chunks according to the specified batch size.
    ///
    /// **Important:**
    /// - This method should only be used for inserting **NEW** entities.
    ///   For updating existing entities, use the standard save operations.
    /// - When overriding consider carefully if the method should be annotated with `@SessionRequired`.
    ///   It is not applicable if batch insertion should happen **only** in the scope of an open session.
    ///
    ///
    /// **Implementation note:** By default, this operation is not supported and will throw
    /// an [UnsupportedOperationException].
    /// Implementers should carefully consider whether batch insertion is appropriate for their specific entity type before
    /// providing an implementation.
    ///
    /// @param newEntities a stream of new entities to be inserted into the database
    /// @param batchSize   the number of entities to process in each batch;
    ///                    typical values range from 100 to 1000 depending on entity complexity
    ///
    /// @return the total number of entities successfully inserted
    ///
    default int batchInsert(final Stream<T> newEntities, final int batchSize) {
        throw new UnsupportedOperationException(ERR_BATCH_INSERTION_IS_UNSUPPORTED);
    }

}
