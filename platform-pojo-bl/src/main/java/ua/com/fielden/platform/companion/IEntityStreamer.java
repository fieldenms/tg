package ua.com.fielden.platform.companion;

import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;

import java.util.stream.Stream;

/// Declares the ability to stream entities based on the specified EQL query.
///
public interface IEntityStreamer<T extends AbstractEntity<?>> {

    /// Returns a stream of entities that match the given query.
    ///
    /// The returned stream **must** be closed to ensure that the underlying result set
    /// and its database resources are released.
    /// Failing to do so may keep the database transaction open and lead to subtle,
    /// hard‑to‑diagnose side effects.
    ///
    /// @param qem       the query execution model defining which entities to stream
    /// @param fetchSize a hint for the number of rows to fetch per batch
    /// @return a lazily-evaluated stream of matching entities
    ///
    Stream<T> stream(final QueryExecutionModel<T, ?> qem, final int fetchSize);

    /// A convenience overload of [#stream(QueryExecutionModel, int)] that uses a default fetch size.
    ///
    Stream<T> stream(final QueryExecutionModel<T, ?> qem);

}
