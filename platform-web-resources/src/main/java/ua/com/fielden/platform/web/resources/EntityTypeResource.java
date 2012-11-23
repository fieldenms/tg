package ua.com.fielden.platform.web.resources;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.roa.HttpHeaders;

/**
 * EntityTypeResource represents a web resource mapped to URI /entity-alias-type. It provides a base implementation for handling the following {@link IEntityDao} methods:
 * <ul>
 * <li>findAll, firstPage -- GET request; should limit the return result to some sane number... such as 10-25
 * <li>getPage -- GET request; expects parameters "page-capacity" and "page-no".
 * <li>save -- PUT request with an envelope containing a new instance of an entity to be persisted for the first time.
 * </ul>
 * Each request is handled by a new resource instance, thus the only thread-safety requirement is to have provided DAO and entity factory thread-safe.
 *
 * @author TG Team
 */
public class EntityTypeResource<T extends AbstractEntity<?>> extends Resource {
    // the following properties are determined from request
    private final int pageCapacity;
    private final int pageNo;

    private final IEntityDao<T> dao;
    private final EntityFactory factory;
    private final RestServerUtil restUtil;

    public IEntityDao<T> getDao() {
        return dao;
    }

    public EntityFactory getFactory() {
        return factory;
    }

    public RestServerUtil getRestUtil() {
        return restUtil;
    }

    ////////////////////////////////////////////////////////////////////
    // let's specify what HTTP methods are supported by this resource //
    ////////////////////////////////////////////////////////////////////
    @Override
    public boolean allowGet() {
	return true;
    }

    @Override
    public boolean allowHead() {
	return true;
    }

    @Override
    public boolean allowPut() {
	return true;
    }

    @Override
    public boolean allowPost() {
	return false;
    }

    /**
     * The main resource constructor accepting a DAO instance in addition to the standard {@link Resource} parameters.
     * <p>
     * DAO is required for DB interoperability, whereas entity factory is required for enhancement of entities provided in request envelopes.
     *
     * @param dao
     * @param factory
     * @param context
     * @param request
     * @param response
     */
    public EntityTypeResource(final IEntityDao<T> dao, final EntityFactory factory, final RestServerUtil restUtil, final Context context, final Request request, final Response response) {
	super(context, request, response);
	getVariants().add(new Variant(MediaType.APPLICATION_OCTET_STREAM));
	this.dao = dao;
	this.factory = factory;
	this.restUtil = restUtil;
	// Method request.getResourceRef() points to the URI and thus provides facilities to access its parts
	pageCapacity = initPageCapacity(request.getResourceRef().getQueryAsForm().getFirstValue("page-capacity"));
	pageNo = initPageNo(request.getResourceRef().getQueryAsForm().getFirstValue("page-no"));
    }

    /**
     * Initialisation of property <code>pageCapacity</code>.
     *
     * @param pageCapacityParamName
     * @return
     */
    private int initPageCapacity(final String pageCapacityParamName) {
	try {
	    return pageCapacityParamName != null ? Integer.parseInt(pageCapacityParamName) : 25;
	} catch (final Exception e) {
	    e.printStackTrace();
	    return 25;
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

    ///////////////////////////////////////////////////////////////////
    ////////////////////// request handlers ///////////////////////////
    ///////////////////////////////////////////////////////////////////

    /**
     * Handles GET requests resulting from RAO call to {@link IEntityDao#getPage(int, int)}.
     */
    @Override
    public Representation represent(final Variant variant) {
	// ensure that request media type is supported
	if (!MediaType.APPLICATION_OCTET_STREAM.equals(variant.getMediaType())) {
	    return restUtil.errorRepresentation("Unsupported media type " + variant.getMediaType() + ".");
	}

	try {
	    final IPage<T> page = dao.getPage(pageNo, pageCapacity);
	    restUtil.setHeaderEntry(getResponse(), HttpHeaders.PAGES, page.numberOfPages() + "");
	    restUtil.setHeaderEntry(getResponse(), HttpHeaders.PAGE_NO, page.no() + "");
	    return restUtil.listRepresentation(page.data());
	} catch (final Exception ex) {
	    ex.printStackTrace();
	    return restUtil.errorRepresentation("Could not process GET request:\n" + ex.getMessage());
	}
    }

    /**
     * Handles PUT request resulting from RAO call to method save in case of a new entity instance.
     */
    @Override
    public void storeRepresentation(final Representation envelope) throws ResourceException {
	try {
	    final T entity =  restUtil.restoreEntity(envelope, dao.getEntityType());
	    getResponse().setEntity(restUtil.singleRepresentation(dao.save(entity)));
	} catch (final Exception ex) {
	    getResponse().setEntity(restUtil.errorRepresentation(ex.getMessage()));
	}
    }
}
