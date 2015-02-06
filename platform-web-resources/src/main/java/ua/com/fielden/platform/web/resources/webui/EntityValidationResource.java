package ua.com.fielden.platform.web.resources.webui;

import java.util.Map;

import org.apache.log4j.Logger;
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
    private final EntityResourceMixin<T> mixin;
    private final RestServerUtil restUtil;
    private final Logger logger = Logger.getLogger(getClass());

    public EntityValidationResource(final Class<T> entityType, final IEntityProducer<T> entityProducer, final EntityFactory entityFactory, final RestServerUtil restUtil, final ICompanionObjectFinder companionFinder, final Context context, final Request request, final Response response) {
        init(context, request, response);

        mixin = new EntityResourceMixin<T>(entityType, entityProducer, entityFactory, restUtil, companionFinder);
        this.restUtil = restUtil;
    }

    /**
     * Handles POST request resulting from RAO call to method save.
     */
    @Post
    @Override
    public Representation post(final Representation envelope) throws ResourceException {
        final Map<String, Object> modifiedPropertiesHolder = mixin.restoreModifiedPropertiesHolderFrom(envelope, restUtil);

        final Object arrivedIdVal = modifiedPropertiesHolder.get(AbstractEntity.ID);
        final Long id = arrivedIdVal == null ? null : ((Integer) arrivedIdVal).longValue();

        // Initialises the "validation prototype" entity, which modification will be made upon:
        final T validationPrototype = mixin.createEntityForRetrieval(id);
        return restUtil.singleJSONRepresentation(mixin.apply(modifiedPropertiesHolder, validationPrototype));
    }

}
