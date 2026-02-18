package ua.com.fielden.platform.companion;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.types.either.Either;

import java.util.Optional;

/// A contract to be implemented by companions that support _save-with-fetch_, which is a save operation with an explicit refetching strategy.
///
/// The platform will recognise companions that implement this interface, and may use that information for certain optimisations.
///
/// In the future, it is likely that the operations in this interface will be integrated into [IEntityActuator].
///
public interface ISaveWithFetch<T extends AbstractEntity<?>> {

    /// Saves `entity` and returns either its ID (left, only if `maybeFetch` is an empty optional)
    /// or a refetched instance of the saved entity (right, `maybeFetch` is used for refetching).
    ///
    /// The same rules apply if `entity` is not persistent (i.e., functional, union, or synthetic), but without refetching.
    /// For non-persistent entities, a left value (ID) may be null (i.e., `Either.left(null)`).
    ///
    /// This method is the single extension point for custom saving logic.
    /// Implementations must put all custom saving logic here and must not implement [IEntityActuator#save(AbstractEntity)],
    /// to avoid multiple execution paths.
    ///
    /// This method should be used as an alternative to [IPersistentEntityMutator#quickSave(AbstractEntity)] by specifying an empty optional for `maybeFetch`.
    ///
    Either<Long, T> save(T entity, Optional<fetch<T>> maybeFetch);

}
