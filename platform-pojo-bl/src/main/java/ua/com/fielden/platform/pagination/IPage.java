package ua.com.fielden.platform.pagination;

import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;

/**
 * Represents a page abstraction for the data pagination functionality, which provides size of the page (i.e. the maximum number of instance page can include, the actual size could
 * be smaller), sequential page number and a list of actual instances associated with the page.
 *
 * It is envisaged that there will be concrete implementations, let's say Hibernate driven, which would implement this interface. At the same time, there should also be some glue
 * code that would tie together that implementation via this interface with some TableModel.
 *
 *
 * @author 01es
 *
 * @param <T>
 */
public interface IPage<T extends AbstractEntity<?>> {
    /**
     * Should return the number representing the maximum number of instance that page can contain. This value can (and in many cases will) be greater than the actual number of
     * instance associated with the page.
     *
     * @return
     */
    int capacity();

    /**
     * Should return a sequential page number, which is zero based.
     *
     * @return
     */
    int no();

    /**
     * Should return a total number of pages in the range of pages this page belongs to.
     *
     * @return
     */
    int numberOfPages();

    /**
     * Should return a list of instances held by the page. The size of the resultant list can be less than the value of page size.
     *
     * @return
     */
    List<T> data();

    /**
     * Should return true if there is the next page.
     *
     * @return
     */
    boolean hasNext();

    /**
     * Should return true if there is the previous page.
     *
     * @return
     */
    boolean hasPrev();

    /**
     * Should move to the next page.
     *
     * @return
     */
    IPage<T> next();

    /**
     * Should move to the previous page.
     *
     * @return
     */
    IPage<T> prev();

    /**
     * Should move to the last page.
     *
     * @return
     */
    IPage<T> last();

    /**
     * Should move to the first page.
     *
     * @return
     */
    IPage<T> first();

    /**
     * Returns summary associated with this page, or null of it does not exist.
     *
     * @return
     */
    EntityAggregates summary();
}