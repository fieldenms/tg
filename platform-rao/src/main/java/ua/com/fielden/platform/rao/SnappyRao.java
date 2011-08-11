package ua.com.fielden.platform.rao;

import java.util.ArrayList;
import java.util.List;

import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.EntityAggregates;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.snappy.ISnappyDao;
import ua.com.fielden.platform.snappy.SnappyQuery;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.utils.Pair;

import com.google.inject.Inject;

@EntityType(AbstractEntity.class)
public class SnappyRao extends CommonEntityRao implements ISnappyDao {

    @Inject
    public SnappyRao(final RestClientUtil restUtil) {
	super(restUtil);
    }

    @Override
    public Pair<ua.com.fielden.snappy.Result, IPage> process(final SnappyQuery snappyQuery) {
	throw new UnsupportedOperationException("Snappy web communication is not yet implemented.");
	//	System.out.println("\t\t\tRAO PROCESS : ");
	//	// create request envelope containing Snappy Query
	//	final Representation envelope = restUtil.represent(snappyQuery);
	//	// create a request URI
	//	final String uri = restUtil.getSnappyQueryUri();
	//	final Request request = new Request(Method.POST, uri, envelope);
	//	// process request
	//	final Pair<Response, Result> res = restUtil.process(request);
	//
	//	final Response response = res.getKey();
	//	final Result result = res.getValue();
	//
	//	// process result
	//	if (result.isSuccessful()) {
	//	    final List filteredEntities = (List) result.getInstance();
	//	    System.err.println("\t\t\tdeserialized =  " + filteredEntities);
	//
	//	    final String aggrValues = restUtil.getHeaderValue(response, HttpHeaders.AGGR_VALUES);
	//	    final ua.com.fielden.snappy.Result snappyResult = (!StringUtils.isEmpty(aggrValues)) //
	//	    ? new RsAggrResult((List<Object>) restUtil.simplyDeserialize(aggrValues), snappyQuery.getAggrAccessors())//
	//		    : new FiltResult(filteredEntities.size() > 0 ? ResultState.FAILED : ResultState.SUCCESSED);
	//
	//	    // page-no, num-of-pages and page-capacity:
	//	    // process header values
	//	    final String pageNo = restUtil.getHeaderValue(response, HttpHeaders.PAGE_NO);
	//	    final String numberOfPages = restUtil.getHeaderValue(response, HttpHeaders.PAGES);
	//	    return new Pair<ua.com.fielden.snappy.Result, IPage>(snappyResult, new SnappyRaoPage(filteredEntities, snappyQuery.isFilteringQuery() ? snappyQuery.getMainQueryString()
	//		    : snappyQuery.getSecondaryQueryString(), Integer.parseInt(pageNo), Integer.parseInt(numberOfPages), ISnappyDao.PAGE_CAPACITY));
	//	} else {
	//	    throw result;
	//	}
    }

    /**
     * Sends a POST request to /snappyquery?page-capacity=pageCapacity&page-no=pageNumber with an envelope containing instance of <code>queryString</code>. The response suppose to
     * return an envelope containing entities resulting from the query.
     * 
     * @param pageNumber
     *            -- numbers from a set of N+{0} indicate a page number to be retrieved.
     */
    @Override
    public List list(final String queryString, final int pageNumber, final int pageCapacity) {
	// create request envelope containing Snappy Query with only "queryString" to be used.
	final Representation envelope = restUtil.represent(new SnappyQuery(queryString, null, new ArrayList<String>()));
	// create a request URI containing page capacity and number
	final String uri = restUtil.getSnappyQueryUri() + "?page-capacity=" + pageCapacity + "&page-no=" + pageNumber;
	final Request request = new Request(Method.POST, uri, envelope);
	// process request
	final Pair<Response, Result> res = restUtil.process(request);

	final Response response = res.getKey();
	final Result result = res.getValue();
	// process result
	if (result.isSuccessful()) {
	    final List filteredEntities = (List) result.getInstance();
	    System.err.println("\t\t\tdeserialized page no == " + pageNumber + " =>  " + filteredEntities);
	    return filteredEntities;
	} else {
	    throw result;
	}
    }

    private class SnappyRaoPage implements IPage {
	private final int pageNumber; // zero-based
	private final int numberOfPages;
	private final int pageCapacity;
	private final List data;
	private final String queryString;

	private SnappyRaoPage(final List data, final String queryString, final int pageNumber, final int numberOfPages, final int pageCapacity) {
	    this.data = data;
	    this.pageNumber = pageNumber;
	    this.numberOfPages = numberOfPages;
	    this.pageCapacity = pageCapacity;
	    this.queryString = queryString;
	}

	private SnappyRaoPage(final String queryString, final int pageNumber, final int numberOfPages, final int pageCapacity) {
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
		return new SnappyRaoPage(queryString, no() + 1, numberOfPages(), capacity());
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
		return new SnappyRaoPage(queryString, no() - 1, numberOfPages(), capacity());
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
		return new SnappyRaoPage(queryString, 0, numberOfPages(), capacity());
	    }
	    return null;
	}

	@Override
	public IPage last() {
	    if (hasNext()) {
		return new SnappyRaoPage(queryString, numberOfPages - 1, numberOfPages(), capacity());
	    }
	    return null;
	}

	@Override
	public int numberOfPages() {
	    return numberOfPages;
	}

	@Override
	public String toString() {
	    return "Page " + (no() + 1) + " of " + (numberOfPages == 0 ? 1 : numberOfPages);
	}
    }

}
