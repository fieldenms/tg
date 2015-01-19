package ua.com.fielden.platform.web.resources.webui;

import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.web.resources.RestServerUtil;

/**
 * The web resource for entity serves as a back-end mechanism of entity retrieval, saving and deletion. It provides a base implementation for handling the following methods:
 * <ul>
 * <li>retrieve entity -- GET request;
 * <li>save new entity -- PUT request with an envelope containing an instance of an entity to be persisted;
 * <li>save already persisted entity -- POST request with an envelope containing an instance of an modified entity to be changed;
 * <li>delete entity -- DELETE request.
 * </ul>
 *
 * @author TG Team
 *
 */
public class EntityResource<T extends AbstractEntity<?>> extends ServerResource {
    private final EntityResourceMixin<T> mixin;
    private final RestServerUtil restUtil;
    private final Long entityId;
    private final Logger logger = Logger.getLogger(getClass());

    public EntityResource(final Class<T> entityType, final IEntityProducer<T> entityProducer, final fetch<T> fetchStrategy, final EntityFactory entityFactory, final RestServerUtil restUtil, final ICompanionObjectFinder companionFinder, final Context context, final Request request, final Response response) {
        init(context, request, response);

        mixin = new EntityResourceMixin<T>(entityType, entityProducer, fetchStrategy, entityFactory, restUtil, companionFinder);
        this.restUtil = restUtil;

        final String entityIdString = request.getAttributes().get("entity-id").toString();
        this.entityId = entityIdString.equalsIgnoreCase("null") ? null : Long.parseLong(entityIdString);
    }

    /**
     * Handles GET requests resulting from RAO call to {@link IEntityDao#findById(Long)}
     */
    @Get
    @Override
    public Representation get() throws ResourceException {
        // process GET request
        return restUtil.singleJSONRepresentation(mixin.createEntityForRetrieval(entityId));
    }

    //    /**
    //     * Handles POST request resulting from RAO call to method save.
    //     */
    //    @Post
    //    @Override
    //    public Representation post(final Representation envelope) throws ResourceException {
    //        final T restoredEntity = restoreEntityFrom(envelope); // final Class<?> entityType = restoredEntity.getType();
    //        final T validationPrototype = initValidationPrototype(restoredEntity.getId());
    //        if (isRequestEntity(restoredEntity)) {
    //            return restUtil.singleJSONRepresentation(validationPrototype);
    //        } else {
    //            return restUtil.singleJSONRepresentation(apply(restoredEntity, validationPrototype));
    //        }
    //    }

}
