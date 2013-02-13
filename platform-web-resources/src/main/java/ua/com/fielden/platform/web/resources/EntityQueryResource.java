package ua.com.fielden.platform.web.resources;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Post;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.roa.HttpHeaders;

/**
 * Represents a web resource mapped to URI /query/entity-alias-type. It handles POST requests provided with {@link QueryExecutionModel}
 * <p>
 * Each request is handled by a new resource instance, thus the only thread-safety requirement is to have provided DAO and entity factory thread-safe.
 *
 * @author TG Team
 */
public class EntityQueryResource<T extends AbstractEntity<?>> extends ServerResource {
    private static final int DEFAULT_PAGE_CAPACITY = 25;
    private static final String FIRST = "first";
    // the following properties are determined from request
    private final Integer pageCapacity;
    private final int pageNo;
    private final int pageCount;
    /** Indicates whether response should return count. */
    private final boolean shouldReturnCount;
    /** Indicates whether response should return the whole result. */
    private final boolean shouldReturnAll;
    /** Indicates whether response should return only first items (number limited by page capacity. */
    private final boolean shouldReturnFirst;
    /** Indicates whether the provided request indicated a deletion query. */
    private final boolean shouldDelete;

    private final IEntityDao<T> dao;
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
    public EntityQueryResource(final IEntityDao<T> dao, final RestServerUtil restUtil, final Context context, final Request request, final Response response) {
	init(context, request, response);
	setNegotiated(false);
	getVariants().add(new Variant(MediaType.APPLICATION_OCTET_STREAM));
	this.dao = dao;
	this.restUtil = restUtil;

	final String pageCapacityParam = request.getResourceRef().getQueryAsForm().getFirstValue("page-capacity");
	final String pageNoParam = request.getResourceRef().getQueryAsForm().getFirstValue("page-no");

	pageCapacity = initPageCapacity(pageCapacityParam);
	pageNo = initPageNoOrCount(pageNoParam);
	pageCount = initPageNoOrCount(request.getResourceRef().getQueryAsForm().getFirstValue("page-count"));

	shouldReturnCount = (pageCapacity == null) && !"all".equalsIgnoreCase(pageCapacityParam) && !FIRST.equalsIgnoreCase(pageNoParam);
	shouldReturnAll = "all".equalsIgnoreCase(pageCapacityParam);
	shouldReturnFirst = FIRST.equalsIgnoreCase(pageNoParam);
	shouldDelete = request.getResourceRef().getQueryAsForm().getFirstValue("deletion") != null;
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
	    return pageNoParamName != null && !pageNoParamName.equalsIgnoreCase(FIRST) ? Integer.parseInt(pageNoParamName) : 0;
	} catch (final Exception e) {
	    e.printStackTrace();
	    return 0;
	}
    }

    // /////////////////////////////////////////////////////////////////
    // //////////////////// request handlers ///////////////////////////
    // /////////////////////////////////////////////////////////////////

    /**
     * Handles POST request resulting from RAO call. It is expected that envelope is a serialised representation of {@link QueryExecutionModel}.
     */
    @Post
    @Override
    public Representation post(final Representation envelope) throws ResourceException {
	try {
	    final QueryExecutionModel<T, EntityResultQueryModel<T>> qem = (QueryExecutionModel<T, EntityResultQueryModel<T>>) restUtil.restoreQueryExecutionModel(envelope);
	    if (shouldDelete) {
		dao.delete(qem.getQueryModel(), qem.getParamValues());
		//getResponse().setEntity(restUtil.resultRepresentation( Result.successful(null)));
		return restUtil.resultRepresentation( Result.successful(null));
	    } else if (shouldReturnCount) {
		final int count = dao.count(qem.getQueryModel(), qem.getParamValues());
		restUtil.setHeaderEntry(getResponse(), HttpHeaders.COUNT, count + "");
		return new StringRepresentation("count");
	    } else if (shouldReturnAll) {
		//getResponse().setEntity(restUtil.listRepresentation(dao.getAllEntities(qem)));
		return restUtil.listRepresentation(dao.getAllEntities(qem));
	    } else if (shouldReturnFirst) {
		return restUtil.listRepresentation(dao.getFirstEntities(qem, pageCapacity));
	    } else {
		final IPage<T> page = dao.getPage(qem, pageNo, pageCount, pageCapacity);
		restUtil.setHeaderEntry(getResponse(), HttpHeaders.PAGES, page.numberOfPages() + "");
		restUtil.setHeaderEntry(getResponse(), HttpHeaders.PAGE_NO, page.no() + "");
		//getResponse().setEntity(restUtil.listRepresentation(page.data()));
		return restUtil.listRepresentation(page.data());
	    }
	} catch (final Exception ex) {
	    ex.printStackTrace();
	    //getResponse().setEntity(restUtil.errorRepresentation("Could not process POST request:\n" + ex.getMessage()));
	    return restUtil.errorRepresentation("Could not process POST request:\n" + ex.getMessage());
	}
    }
}
