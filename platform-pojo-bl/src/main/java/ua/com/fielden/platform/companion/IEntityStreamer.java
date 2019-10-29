package ua.com.fielden.platform.companion;

import java.util.stream.Stream;

import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * Declares the ability to stream entities based on the specified EQL query.
 * 
 * @author TG Team
 *
 * @param <T>
 */
public interface IEntityStreamer<T extends AbstractEntity<?>> {

    /**
     * Returns a non-parallel stream with the data based on the provided query.
     * The returned stream must always be wrapped into <code>try with resources</code> clause to ensure that the underlying resultset is closed.
     * 
     * @param qem -- EQL model
     * @param fetchSize -- a batch size for retrieve the next lot of data to feed the stream
     * @return
     */
    Stream<T> stream(final QueryExecutionModel<T, ?> qem, final int fetchSize);
    
    /**
     * A convenience method based on {@link #stream(QueryExecutionModel, int), but with a default fetch size. 
     * The returned stream must always be wrapped into <code>try with resources</code> clause to ensure that the underlying resultset is closed.
     * 
     * @param qem
     * @return
     */
    Stream<T> stream(final QueryExecutionModel<T, ?> qem);

}
