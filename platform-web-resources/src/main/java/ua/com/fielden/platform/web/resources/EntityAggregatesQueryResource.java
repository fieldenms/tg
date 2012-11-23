package ua.com.fielden.platform.web.resources;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

import ua.com.fielden.platform.dao.IEntityAggregatesDao;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.roa.HttpHeaders;

/**
 * Represents a web resource mapped to URI /query/EntityAggregates. It handles POST requests to entity aggregates.
 * <p>
 * Each request is handled by a new resource instance, thus the only thread-safety requirement is to have provided DAO and entity factory thread-safe.
 *
 * @author TG Team
 */
public class EntityAggregatesQueryResource extends Resource {
    // the following properties are determined from request
    private final Integer pageCapacity;
    private final int pageCount;
    private final int pageNo;
    /** Indicates whether response should return count. */
    private final boolean shouldReturnCount;
    private final boolean shouldReturnAll;

    private final IEntityAggregatesDao dao;
    private final RestServerUtil restUtil;

    /**
     * The main resource constructor accepting a DAO instance in addition to the standard {@link Resource} parameters.
     * <p>
     * DAO is required for DB interoperability, whereas entity factory is required for enhancement of entities provided in request envelopes.
     *
     * @param dao
     * @param context
     * @param request
     * @param response
     */
    public EntityAggregatesQueryResource(final IEntityAggregatesDao dao, final RestServerUtil restUtil, final Context context, final Request request, final Response response) {
	super(context, request, response);
	getVariants().add(new Variant(MediaType.APPLICATION_OCTET_STREAM));
	this.dao = dao;
	this.restUtil = restUtil;

	final String param = request.getResourceRef().getQueryAsForm().getFirstValue("page-capacity");

	pageCapacity = initPageCapacity(param);
	pageNo = initPageNoOrCount(request.getResourceRef().getQueryAsForm().getFirstValue("page-no"));
	pageCount = initPageNoOrCount(request.getResourceRef().getQueryAsForm().getFirstValue("page-count"));

	shouldReturnCount = (pageCapacity == null) && !"all".equalsIgnoreCase(param);
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
    private int initPageNoOrCount(final String pageNoParamName) {
	try {
	    return pageNoParamName != null ? Integer.parseInt(pageNoParamName) : 0;
	} catch (final Exception e) {
	    e.printStackTrace();
	    return 0;
	}
    }

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
     * Handles POST request resulting from RAO call. It is expected that envelope is a serialised representation of {@link AggregatesQueryExecutionModel}.
     */
    @Override
    public void acceptRepresentation(final Representation envelope) throws ResourceException {
	try {
	    final  QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel> queryAndFetch = (QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel>) restUtil.restoreQueryExecutionModel(envelope);
	    if (shouldReturnCount) {
		final int count = dao.count(queryAndFetch.getQueryModel(), queryAndFetch.getParamValues());
		restUtil.setHeaderEntry(getResponse(), HttpHeaders.COUNT, count + "");
	    } else if (shouldReturnAll) {
		getResponse().setEntity(restUtil.listRepresentation(dao.getAllEntities(queryAndFetch)));
	    } else {
		final IPage<EntityAggregates> page = dao.getPage(queryAndFetch, pageNo, pageCount, pageCapacity);
		restUtil.setHeaderEntry(getResponse(), HttpHeaders.PAGES, page.numberOfPages() + "");
		restUtil.setHeaderEntry(getResponse(), HttpHeaders.PAGE_NO, page.no() + "");
		getResponse().setEntity(restUtil.listRepresentation(page.data()));
	    }
	} catch (final Exception ex) {
	    ex.printStackTrace();
	    getResponse().setEntity(restUtil.errorRepresentation("Could not process POST request:\n" + ex.getMessage()));
	}
    }
}
