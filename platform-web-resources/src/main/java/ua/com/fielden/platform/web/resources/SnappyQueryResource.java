package ua.com.fielden.platform.web.resources;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.snappy.ISnappyDao;
import ua.com.fielden.platform.snappy.SnappyQuery;

/**
 * Represents a web resource mapped to URI /snappyquery. It handles POST
 * requests provided with {@link SnappyQuery} - simple string hql query with
 * parameters.
 * <p>
 * Each request is handled by a new resource instance, thus the only
 * thread-safety requirement is to have provided DAO and entity factory
 * thread-safe.
 *
 * @author TG Team
 */
public class SnappyQueryResource<T extends AbstractEntity> extends Resource {
    private static final int DEFAULT_PAGE_CAPACITY = 25;
    // the following properties are determined from request
    private final Integer pageCapacity;
    private final int pageNo;
    /** Indicates whether response should return count. */
    private final boolean shouldReturnCount;
    private final boolean shouldReturnAll;

    private final RestServerUtil restUtil;

    private final ISnappyDao dao;

    // //////////////////////////////////////////////////////////////////
    // let's specify what HTTP methods are supported by this resource //
    // //////////////////////////////////////////////////////////////////
    @Override
    public boolean allowPost() {
	return true;
    }

    @Override
    public boolean allowGet() {
	return false;
    }

    /**
     * The main resource constructor accepting a DAO instance in addition to the
     * standard {@link Resource} parameters.
     * <p>
     * DAO is required for DB interoperability, whereas entity factory is
     * required for enhancement of entities provided in request envelopes.
     *
     * @param dao
     * @param context
     * @param request
     * @param response
     */
    public SnappyQueryResource(final ISnappyDao dao, final RestServerUtil restUtil, final Context context, final Request request, final Response response) {
	super(context, request, response);

	this.dao = dao;
	getVariants().add(new Variant(MediaType.APPLICATION_OCTET_STREAM));
	this.restUtil = restUtil;

	final String param = request.getResourceRef().getQueryAsForm().getFirstValue("page-capacity");

	pageCapacity = initPageCapacity(param);
	pageNo = initPageNo(request.getResourceRef().getQueryAsForm().getFirstValue("page-no"));

	shouldReturnCount = pageCapacity == null && !"all".equalsIgnoreCase(param);
	shouldReturnAll = "all".equalsIgnoreCase(param);
    }

    /**
     * Initialisation of property <code>pageCapacity</code>.
     *
     * @param pageCapacityParamName
     * @return
     */
    private Integer initPageCapacity(final String pageCapacityParamName) {
	try {
	    return pageCapacityParamName != null ? Integer.parseInt(pageCapacityParamName) : null;
	} catch (final Exception e) {
	    return null;
	}
    }

    /**
     * Initialisation of property <code>pageNo</code>.
     *
     * @param pageNoParamName
     * @return
     */
    private int initPageNo(final String pageNoParamName) {
	try {
	    return pageNoParamName != null ? Integer.parseInt(pageNoParamName) : 0;
	} catch (final Exception e) {
	    e.printStackTrace();
	    return 0;
	}
    }

    // /////////////////////////////////////////////////////////////////
    // //////////////////// request handlers ///////////////////////////
    // /////////////////////////////////////////////////////////////////

    /**
     * Handles POST request resulting from RAO call. It is expected that
     * envelope is a serialised representation of {@link SnappyQuery}.
     */
    @Override
    public void acceptRepresentation(final Representation envelope) throws ResourceException {
	try {
	    throw new UnsupportedOperationException("Snappy web communication is not yet supported.");
	    //	    final SnappyQuery snappyQuery = restUtil.restoreSnappyQuery(envelope);
	    //	    if (pageCapacity == null) {
	    //		final Pair<ua.com.fielden.snappy.Result, IPage> pair = dao.process(snappyQuery);
	    //		restUtil.setHeaderEntry(getResponse(), HttpHeaders.PAGE_NO, pair.getValue().no() + "");
	    //		restUtil.setHeaderEntry(getResponse(), HttpHeaders.PAGES, pair.getValue().numberOfPages() + "");
	    //		if (pair.getKey() instanceof RsAggrResult) {
	    //		    restUtil.setHeaderEntry(getResponse(), HttpHeaders.AGGR_VALUES, restUtil.snappyAggrValues((RsAggrResult) pair.getKey()));
	    //		}
	    //		getResponse().setEntity(restUtil.snappyResultRepresentation(pair.getValue().data()));
	    //	    } else {
	    //		getResponse().setEntity(restUtil.snappyResultRepresentation(dao.list(snappyQuery.getMainQueryString(), pageNo, pageCapacity)));
	    //	    }
	} catch (final Exception ex) {
	    ex.printStackTrace();
	    getResponse().setEntity(restUtil.errorRepresentation("Could not process snappy query POST request:\n" + ex.getMessage()));
	}
    }
}
