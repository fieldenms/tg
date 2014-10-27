package ua.com.fielden.platform.web.resources;

import org.apache.commons.lang.StringUtils;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.error.Result;

public class FunctionalEntityResource<T extends AbstractEntity<?>, DAO extends IEntityDao<T>> extends ServerResource {

    private final RestServerUtil restUtil;
    private final DAO dao;
    private final EntityFactory entityFactory;

    public FunctionalEntityResource(final DAO dao, final EntityFactory entityFactory, final RestServerUtil restUtil, final Context context, final Request request, final Response response) {
	init(context, request, response);
	this.dao = dao;
	this.restUtil = restUtil;
	this.entityFactory = entityFactory;
    }

    /**
     * Handles POST request resulting from RAO call to method save.
     */
    @Post
    @Override
    public Representation post(final Representation envelope) throws ResourceException {
        try {
            final T entity = restUtil.restoreJSONEntity(envelope, dao.getEntityType());
            // TODO: This validation does not really validate anything since restored entity would not have anything validated,
            //       so a full revalidation is required to be enforced for every property.
            //       However, since validation happens at the client side (only clients controlled by FMS)
            //       there is not apparent reason at this stage to enforce rigorous server side validation.
            final Result validationResult = entity.isValid();
            if (validationResult.isSuccessful()) {
                //getResponse().setEntity(restUtil.singleRepresentation(dao.save(entity)));
                return restUtil.singleJSONRepresentation(dao.save(entity));
            } else {
                //getResponse().setEntity(restUtil.resultRepresentation(validationResult));
                return restUtil.resultJSONRepresentation(validationResult);
            }
        } catch (final Exception ex) {
            ex.printStackTrace();
            getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT);
            final String msg = !StringUtils.isEmpty(ex.getMessage()) ? ex.getMessage() : "Exception does not contain any specific message.";
            //getResponse().setEntity(restUtil.errorRepresentation(msg));
            return restUtil.errorJSONRepresentation(msg);
        }
    }
}
