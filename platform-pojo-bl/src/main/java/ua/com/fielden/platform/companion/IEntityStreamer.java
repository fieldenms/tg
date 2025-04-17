package ua.com.fielden.platform.companion;

import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;

import java.util.stream.Stream;

/**
 * Declares the ability to stream entities based on the specified EQL query.
 * 
 * @author TG Team
 *
 * @param <T>
 */
public interface IEntityStreamer<T extends AbstractEntity<?>> {

    /**
     * Returns a stream of entities that match the provided query.
     * <p>
     * The returned stream must be closed to ensure that the underlying resultset is closed.
     *
     * @param fetchSize  a hint for the number of rows that should be fetched per batch
     */
    Stream<T> stream(final QueryExecutionModel<T, ?> qem, final int fetchSize);

    /**
     * Returns a stream of entities that match the provided query.
     * <p>
     * The returned stream must be closed to ensure that the underlying resultset is closed.
     */
    Stream<T> stream(final QueryExecutionModel<T, ?> qem);

}
