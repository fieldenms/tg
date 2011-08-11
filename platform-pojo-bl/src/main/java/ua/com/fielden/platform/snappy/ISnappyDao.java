package ua.com.fielden.platform.snappy;

import java.util.List;

import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.snappy.Result;

/**
 * The general data access interface for snappy querying (should be implemented in Dao and Rao terms).
 * 
 * @author Jhou
 * 
 */
public interface ISnappyDao {
    public static final int PAGE_CAPACITY = 10;

    /**
     * A central method for processing snappy rule represented in {@link SnappyQuery} form. Should return a pair of "snappy result" (containing "RULE_FAILED" or "RULE_SUCCESSED"
     * value, aggregated values and its captions etc) and a page of "filtered entities".
     * 
     * @param snappyQuery
     * @return
     */
    Pair<Result, IPage> process(final SnappyQuery snappyQuery);

    /**
     * Returns a list of "filtered entities" that corresponds to <code>queryString</code> filtering hql.
     * 
     * @param queryString
     * @param pageNumber
     * @param numberOfPages
     * @param pageCapacity
     * @return
     */
    List list(final String queryString, final int pageNumber, final int pageCapacity);

}
