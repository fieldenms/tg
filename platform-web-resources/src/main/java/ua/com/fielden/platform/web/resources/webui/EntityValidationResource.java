package ua.com.fielden.platform.web.resources.webui;

import java.util.Map;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.web.resources.RestServerUtil;

/**
 * The web resource for entity validation serves as a back-end mechanism of changing entity properties and validating that changes.
 *
 * The server does not keep any state about the entities to be modified.
 *
 * @author TG Team
 *
 */
public class EntityValidationResource<T extends AbstractEntity<?>> extends ServerResource {
    private final EntityResourceUtils<T> utils;
    private final RestServerUtil restUtil;

    public EntityValidationResource(final Class<T> entityType, final IEntityProducer<T> entityProducer, final EntityFactory entityFactory, final RestServerUtil restUtil, final ICompanionObjectFinder companionFinder, final Context context, final Request request, final Response response) {
        init(context, request, response);

        this.restUtil = restUtil;
        utils = new EntityResourceUtils<T>(entityType, entityProducer, entityFactory, restUtil, companionFinder);
    }

    /**
     * Handles POST request resulting from RAO call to method save.
     */
    @Post
    @Override
    public Representation post(final Representation envelope) throws ResourceException {
        final Map<String, Object> modifiedPropertiesHolder = EntityResourceUtils.restoreModifiedPropertiesHolderFrom(envelope, restUtil);
        final T applied = utils.constructEntity(modifiedPropertiesHolder).getKey();
        return restUtil.rawListJSONRepresentation(applied);
    }
}
