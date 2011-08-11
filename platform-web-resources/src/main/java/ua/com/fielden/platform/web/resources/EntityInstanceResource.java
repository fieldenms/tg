package ua.com.fielden.platform.web.resources;

import org.apache.commons.lang.StringUtils;
import org.hibernate.ObjectNotFoundException;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.roa.HttpHeaders;

/**
 * EntityInstanceResource represents a web resource mapped to an URI template /entity-alias-type/{entity-if}. It provides a base implementation for handling the following
 * {@link IEntityDao} methods:
 * <ul>
 * <li>entityExists -- HEAD request.
 * <li>findById -- GET request.
 * <li>save -- POST request with an envelope containing an instance of an entity to be persisted.
 * </ul>
 * Each request is handled by a new resource instance, thus the only thread-safety requirement is to have provided DAO and entity factory thread-safe.
 *
 * @author TG Team
 */
public class EntityInstanceResource<T extends AbstractEntity> extends Resource {
    // the following properties are determined from request
    protected final String username;
    protected final Long entityId;

    protected final Long entityVersion; // is initialised with value other than null only in cases where head request came to check entity staleness.

    protected final IEntityDao<T> dao;
    protected final EntityFactory factory;
    protected final RestServerUtil restUtil;

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
    public boolean allowPost() {
	return true;
    }

    @Override
    public boolean allowDelete() {
	return true;
    }

    /**
     * The main resource constructor accepting a DAO instance and an entity factory in addition to the standard {@link Resource} parameters.
     * <p>
     * DAO is required for DB interoperability, whereas entity factory is required for enhancement of entities provided in request envelopes.
     *
     * @param dao
     * @param factory
     * @param context
     * @param request
     * @param response
     */
    public EntityInstanceResource(final IEntityDao<T> dao, final EntityFactory factory, final RestServerUtil restUtil, final Context context, final Request request, final Response response) {
	super(context, request, response);
	getVariants().add(new Variant(MediaType.APPLICATION_OCTET_STREAM));
	this.dao = dao;
	this.factory = factory;
	this.restUtil = restUtil;
	this.username = (String) request.getAttributes().get("username");
	dao.setUsername(username);
	this.entityId = Long.parseLong(request.getAttributes().get("entity-id").toString());
	this.entityVersion = initEntityVersion(request.getResourceRef().getQueryAsForm().getFirstValue("version"));
    }

    ///////////////////////////////////////////////////////////////////
    ////////////////////// request handlers ///////////////////////////
    ///////////////////////////////////////////////////////////////////

    private Long initEntityVersion(final String version) {
	try {
	    return version != null ? Long.parseLong(version) : null;
	} catch (final Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }

    /**
     * Handles GET requests resulting from RAO call to IEntityDao.findById
     */
    @Override
    public Representation represent(final Variant variant) {
	// ensure that request media type is supported
	if (!MediaType.APPLICATION_OCTET_STREAM.equals(variant.getMediaType())) {
	    return restUtil.errorRepresentation("Unsupported media type " + variant.getMediaType() + ".");
	}
	// process GET request
	try {
	    return restUtil.singleRepresentation(dao.findById(entityId));
	} catch (final Exception ex) {
	    getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
	    return restUtil.errorRepresentation("Could not process GET request:\n" + ex.getMessage());
	}
    }

    /**
     * Handles HEAD request resulting from RAO call to IEntityDao.entityExists and IEntityDao.isStale.
     */
    @Override
    public void handleHead() {
	if (checkStaleness()) {
	    try {
		final boolean stale = dao.isStale(entityId, entityVersion);
		restUtil.setHeaderEntry(getResponse(), HttpHeaders.STALE, stale ? "true" : "false");
	    } catch (final Exception ex) {
		getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		restUtil.setHeaderEntry(getResponse(), HttpHeaders.ERROR, ex.getMessage());
	    }
	} else {
	    try {
		restUtil.setHeaderEntry(getResponse(), HttpHeaders.EXISTS, Boolean.toString(dao.entityExists(entityId)));
	    } catch (final ObjectNotFoundException ex) {
		restUtil.setHeaderEntry(getResponse(), HttpHeaders.EXISTS, "false");
	    } catch (final Exception ex) {
		getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		restUtil.setHeaderEntry(getResponse(), HttpHeaders.ERROR, ex.getMessage());
	    }
	}
    }

    /**
     * Handles POST request resulting from RAO call to method save.
     */
    @Override
    public void acceptRepresentation(final Representation envelope) throws ResourceException {
	try {
	    final T entity = restUtil.restoreEntity(envelope, dao.getEntityType());
	    // TODO: This validation does not really validate anything since restored entity would not have anything validated,
	    //       so a full revalidation is required to be enforced for every property.
	    //       However, since validation happens at the client side (only clients controlled by FMS)
	    //       there is not apparent reason at this stage to enforce rigorous server side validation.
	    final Result validationResult = entity.isValid();
	    if (validationResult.isSuccessful()) {
		getResponse().setEntity(restUtil.singleRepresentation(dao.save(entity)));
	    } else {
		getResponse().setEntity(restUtil.resultRepresentation(validationResult));
	    }
	} catch (final Exception ex) {
	    getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT);
	    final String msg = !StringUtils.isEmpty(ex.getMessage()) ? ex.getMessage() : "Exception does not contain any specific message.";
	    getResponse().setEntity(restUtil.errorRepresentation(msg));
	}
    }

    @Override
    public void handleDelete() {
	try {
	    dao.delete(factory.newEntity(dao.getEntityType(), entityId));
	} catch (final Exception ex) {
	    getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
	    restUtil.setHeaderEntry(getResponse(), HttpHeaders.ERROR, ex.getMessage());
	}
    }

    private boolean checkStaleness() {
	return entityVersion != null;
    }

    public IEntityDao<T> getDao() {
        return dao;
    }

    public EntityFactory getFactory() {
        return factory;
    }

    public Long getEntityId() {
        return entityId;
    }

    public String getUsername() {
        return username;
    }

}
