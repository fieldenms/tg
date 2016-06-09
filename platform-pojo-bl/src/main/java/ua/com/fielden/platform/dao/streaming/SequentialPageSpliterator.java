package ua.com.fielden.platform.dao.streaming;

import java.util.Spliterator;
import java.util.function.Consumer;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.pagination.IPage;

/**
 * A spliterator that is based on <code>IPage</code> concept for constructing a sequential (non-parallel) stream of entities that are retrieved by the specified EQL.
 * 
 * @author TG Team
 *
 * @param <T>
 */
public class SequentialPageSpliterator<T extends AbstractEntity<?>> implements Spliterator<T> {

    private transient final Logger logger = Logger.getLogger(this.getClass());
    
    private final IEntityDao<T> companion;
    private final QueryExecutionModel<T, ?> qem;
    private final int pageCapacity;
    private IPage<T> currPage;
    private Spliterator<T> currPageSliterator;
    
    // this is just for debugging purposes to count the number of page retrievals
    private int countOfPageRetrievals;
    
    public SequentialPageSpliterator(
            final IEntityDao<T> companion,
            final QueryExecutionModel<T, ?> qem,
            final int pageCapacity
            ) {
        this.companion = companion;
        this.qem = qem;
        this.pageCapacity = pageCapacity;
    }
    
    @Override
    public boolean tryAdvance(final Consumer<? super T> action) {
        // if the currPage is not assigned then this is the first time the data being accessed
        // which means that the first page needs to be obtained
        if (currPage == null) {
            countOfPageRetrievals++;
            currPage = companion.firstPage(qem, pageCapacity);
            currPageSliterator = currPage.data().stream().spliterator();
        }
        
        // try to advance on the data for the current page
        final boolean hasAdvanced = currPageSliterator.tryAdvance(action);
        // if there was no data to advance upon then the next page needs to be loaded if present
        // otherwise, this is the end of the data
        if (!hasAdvanced) {
            if (currPage.hasNext()) {
                countOfPageRetrievals++;
                currPage = currPage.next();
                currPageSliterator = currPage.data().stream().spliterator();
                return currPageSliterator.tryAdvance(action);
            } else {
                logger.debug(String.format("The number of page calls: %s", countOfPageRetrievals));
                return false;
            }
        }
        return true;
    }

    @Override
    public Spliterator<T> trySplit() {
        // no parallel processing support is currently envisaged...
        return null;
    }

    @Override
    public long estimateSize() {
        // the requirement of this method is to be able to compute the size before stream traversal has started
        // in our case this means that we would need to execute a query to compute the count even before someone tried to access the data
        // so, for now lets consider the size too expensive to compute by returning Long.MAX_VALUE.
        return Long.MAX_VALUE;
    }

    @Override
    public int characteristics() {
        return ORDERED & NONNULL & IMMUTABLE;
    }

}
