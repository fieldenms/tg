package ua.com.fielden.platform.dao;

import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.joda.time.DateTime;
import org.joda.time.Period;

import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.EntityAggregates;
import ua.com.fielden.platform.equery.interfaces.IFilter;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.snappy.ISnappyDao;
import ua.com.fielden.platform.snappy.SnappyQuery;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.snappy.DbDateUtilities;
import ua.com.fielden.snappy.FiltResult;
import ua.com.fielden.snappy.Result;
import ua.com.fielden.snappy.Result.ResultState;
import ua.com.fielden.snappy.RsAggrResult;

import com.google.inject.Inject;

@EntityType(AbstractEntity.class)
public class SnappyDao extends CommonEntityDao implements ISnappyDao {

    @Inject
    protected SnappyDao(final IFilter filter) {
	super(filter);
    }

    @Override
    @SessionRequired
    public Pair<Result, IPage> process(final SnappyQuery snappyQuery) {
	System.out.println("\t\t\tDAO PROCESS : ");
	final int filteringPagesNumber;

	final Session session = getSession();
	// retrieve current database date! this date will be used in date conditions (previously date parameters should be initialized)
	final Date currentDbDate = retrieveCurrentTimestamp();
	final Result result;
	if (snappyQuery.isFilteringQuery()) { // filtering query:
	    // Hql parsing succeeded. Causes JDBC and other exceptions.
	    System.out.print("2. Execute filtering query size determining ...");
	    filteringPagesNumber = (int) evalNumOfPages(snappyQuery.getMainQueryString(), currentDbDate);
	    System.out.println("success");
	    result = new FiltResult(filteringPagesNumber == 0 ? ResultState.SUCCESSED : ResultState.FAILED);
	} else { // rs-aggregating query:
	    // Causes Hql parsing exceptions.
	    System.out.print("1. Creating rs-aggr query...");
	    final Query rsAggrQuery = session.createQuery(removeOrdering(snappyQuery.getMainQueryString()));
	    new DbDateUtilities().initializeSnappyDateParameters(rsAggrQuery, currentDbDate);
	    System.out.println("success");

	    // Hql parsing succeeded. Causes JDBC and other exceptions.
	    System.out.print("2. Execute rs-aggr query...");
	    final Object result0 = rsAggrQuery.setMaxResults(1).uniqueResult();
	    System.out.println("success");

	    // Hql parsing succeeded. Causes JDBC and other exceptions.
	    System.out.print("4. Execute evaluation of size of filtering part of aggregating query...");
	    filteringPagesNumber = (int) evalNumOfPages(snappyQuery.getSecondaryQueryString(), currentDbDate);
	    System.out.println("success");
	    result = new RsAggrResult(result0, snappyQuery.getAggrAccessors());
	}
	System.out.println("filterPagesNumber == " + filteringPagesNumber);
	return new Pair<Result, IPage>(result, new SnappyDaoPage(snappyQuery.isFilteringQuery() ? snappyQuery.getMainQueryString() : snappyQuery.getSecondaryQueryString(), 0, filteringPagesNumber, PAGE_CAPACITY));
    }

    /**
     * Calculates the number of pages of the given size required to fit the whole result set.
     *
     *
     * @param model
     * @param pageCapacity
     * @return
     */
    @SessionRequired
    private long evalNumOfPages(final String queryString, final Date currentDbDate) {
	final String queryWithoutOrdering = removeOrdering(queryString);
	final Query query = getSession().createQuery("select count(*) " + queryWithoutOrdering.substring(queryWithoutOrdering.indexOf("from ")).replace("fetch ", ""));
	new DbDateUtilities().initializeSnappyDateParameters(query, currentDbDate);
	final long resultSize = (Long) query.uniqueResult();
	return resultSize % PAGE_CAPACITY == 0 ? resultSize / PAGE_CAPACITY : resultSize / PAGE_CAPACITY + 1;
    }

    private String removeOrdering(final String query) {
	final int orderByPlace = query.indexOf("order by ");
	return orderByPlace < 0 ? query : query.replace(query.substring(orderByPlace, query.length()), "");
    }

    /**
     * Retrieves current database-related date.
     *
     * @return
     */
    @SessionRequired
    private Date retrieveCurrentTimestamp() {
	return (Date) getSession().createQuery("select current_timestamp from SingletonEntity").uniqueResult();
    }

    @SessionRequired
    public List list(final String queryString, final int pageNumber, final int pageCapacity) {
	final Query filteringQuery = getSession().createQuery(queryString);
	new DbDateUtilities().initializeSnappyDateParameters(filteringQuery, retrieveCurrentTimestamp());
	System.out.println("\n  [Snappy] HQL QUERY LINES: " + queryString);
	filteringQuery.setFirstResult(pageNumber * pageCapacity)//
	.setFetchSize(pageCapacity)//
	.setMaxResults(pageCapacity);

	final DateTime st = new DateTime();
	final List list = filteringQuery.list();
	final Period pd = new Period(st, new DateTime());
	System.out.println("  [Snappy] QUERY EXEC TIME: " + pd.getMinutes() + " m " + pd.getSeconds() + " s " + pd.getMillis() + " ms\n  RESULTS FETCHED: " + list.size());
	return list;
    }

    public class SnappyDaoPage implements IPage {
	private final int pageNumber; // zero-based
	private final int numberOfPages;
	private final int pageCapacity;
	private final List data;
	private final String queryString;

	public SnappyDaoPage(final String queryString, final int pageNumber, final int numberOfPages, final int pageCapacity) {
	    data = list(queryString, pageNumber, pageCapacity);
	    this.pageNumber = pageNumber;
	    this.numberOfPages = numberOfPages;
	    this.pageCapacity = pageCapacity;
	    this.queryString = queryString;
	}

	@Override
	public EntityAggregates summary() {
	    return null;
	}

	@Override
	public List data() {
	    return hasNext() ? data.subList(0, capacity()) : data;
	}

	@Override
	public boolean hasNext() {
	    return pageNumber < numberOfPages - 1;
	}

	@Override
	public boolean hasPrev() {
	    return no() != 0;
	}

	@Override
	public IPage next() {
	    if (hasNext()) {
		return new SnappyDaoPage(queryString, no() + 1, numberOfPages(), capacity());
	    }
	    return null;
	}

	@Override
	public int no() {
	    return pageNumber;
	}

	@Override
	public IPage prev() {
	    if (hasPrev()) {
		return new SnappyDaoPage(queryString, no() - 1, numberOfPages(), capacity());
	    }
	    return null;
	}

	@Override
	public int capacity() {
	    return pageCapacity;
	}

	@Override
	public IPage first() {
	    if (hasPrev()) {
		return new SnappyDaoPage(queryString, 0, numberOfPages(), capacity());
	    }
	    return null;
	}

	@Override
	public IPage last() {
	    if (hasNext()) {
		return new SnappyDaoPage(queryString, numberOfPages - 1 /* -1 */, numberOfPages(), capacity());
	    }
	    return null;
	}

	@Override
	public int numberOfPages() {
	    return numberOfPages;
	}

	@Override
	public String toString() {
	    return "Page " + (no() + 1) + " of " + ((numberOfPages == 0) ? 1 : numberOfPages);
	}
    }

}
