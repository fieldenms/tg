package ua.com.fielden.platform.web.resources;

import org.apache.commons.lang.StringUtils;
import org.hibernate.ObjectNotFoundException;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.IComputationMonitor;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.roa.HttpHeaders;

/**
 * EntityInstanceResource represents a web resource mapped to an URI template /entity-alias-type/{entity-id}. It provides a base implementation for handling the following
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
public class EntityInstanceResource<T extends AbstractEntity<?>> extends ServerResource implements IComputationMonitor {
    // the following properties are determined from request
    protected final Long entityId;

    protected final Long entityVersion; // is initialised with value other than null only in cases where head request came to check entity staleness.

    protected final IEntityDao<T> dao;
    protected final EntityFactory factory;
    protected final RestServerUtil restUtil;

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
        init(context, request, response);
        setNegotiated(false);
        getVariants().add(new Variant(MediaType.APPLICATION_OCTET_STREAM));
        this.dao = dao;
        this.factory = factory;
        this.restUtil = restUtil;
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
     * Handles GET requests resulting from RAO call to {@link IEntityDao#findById(Long)}
     */
    @Get
    @Override
    public Representation get() {
        // process GET request
        try {
            return restUtil.singleRepresentation(dao.findById(entityId));
        } catch (final Exception ex) {
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return restUtil.errorRepresentation("Could not process GET request:\n" + ex.getMessage());
        }
    }

    /**
     * Handles HEAD request resulting from RAO call to {@link IEntityDao#entityExists(AbstractEntity)} and {@link IEntityDao#isStale(Long, Long)}.
     */
    @Get
    // used in place of HEAD
    @Override
    public Representation head() {
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

        return new EmptyRepresentation();
    }

    /**
     * Handles POST request resulting from RAO call to method save.
     */
    @Post
    @Override
    public Representation post(final Representation envelope) throws ResourceException {
        try {
            final T entity = restUtil.restoreEntity(envelope, dao.getEntityType());
            // TODO: This validation does not really validate anything since restored entity would not have anything validated,
            //       so a full revalidation is required to be enforced for every property.
            //       However, since validation happens at the client side (only clients controlled by FMS)
            //       there is not apparent reason at this stage to enforce rigorous server side validation.
            final Result validationResult = entity.isValid();
            if (validationResult.isSuccessful()) {
                //getResponse().setEntity(restUtil.singleRepresentation(dao.save(entity)));
                return restUtil.singleRepresentation(dao.save(entity));
            } else {
                //getResponse().setEntity(restUtil.resultRepresentation(validationResult));
                return restUtil.resultRepresentation(validationResult);
            }
        } catch (final Exception ex) {
            getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT);
            final String msg = !StringUtils.isEmpty(ex.getMessage()) ? ex.getMessage() : "Exception does not contain any specific message.";
            //getResponse().setEntity(restUtil.errorRepresentation(msg));
            return restUtil.errorRepresentation(msg);
        }
    }

    @Delete
    @Override
    public Representation delete() {
        try {
            dao.delete(factory.newEntity(dao.getEntityType(), entityId));
        } catch (final Exception ex) {
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            restUtil.setHeaderEntry(getResponse(), HttpHeaders.ERROR, ex.getMessage());
        }

        return new StringRepresentation("delete");
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

    @Override
    public boolean stop() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Integer progress() {
        // TODO Auto-generated method stub
        return null;
    }

}
