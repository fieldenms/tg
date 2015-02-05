package ua.com.fielden.platform.web.resources.webui;

import java.util.Map;

import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.error.Result;
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

    public EntityResource(final Class<T> entityType, final IEntityProducer<T> entityProducer, final EntityFactory entityFactory, final RestServerUtil restUtil, final ICompanionObjectFinder companionFinder, final Context context, final Request request, final Response response) {
        init(context, request, response);

        mixin = new EntityResourceMixin<T>(entityType, entityProducer, entityFactory, restUtil, companionFinder);
        this.restUtil = restUtil;

        final String entityIdString = request.getAttributes().get("entity-id").toString();
        this.entityId = entityIdString.equalsIgnoreCase("new") ? null : Long.parseLong(entityIdString);
    }

    /**
     * Handles GET requests resulting from tg-entity-master <code>retrieve()</code> method (new or persisted entity).
     */
    @Get
    @Override
    public Representation get() throws ResourceException {
        return restUtil.singleJSONRepresentation(mixin.createEntityForRetrieval(entityId));
    }

    /**
     * Handles POST requests resulting from tg-entity-master <code>save()</code> method (persisted entity).
     */
    @Post
    @Override
    public Representation post(final Representation envelope) throws ResourceException {
        return tryToSave(envelope);
    }

    /**
     * Handles PUT requests resulting from tg-entity-master <code>save()</code> method (new entity).
     */
    @Put
    @Override
    public Representation put(final Representation envelope) throws ResourceException {
        return tryToSave(envelope);
    }

    /**
     * Tries to save the changes for the entity and returns it in JSON format.
     *
     * @param envelope
     * @return
     */
    private Representation tryToSave(final Representation envelope) {
        final Map<String, Object> modifiedPropertiesHolder = mixin.restoreModifiedPropertiesHolderFrom(envelope, restUtil);

        final T validationPrototype = mixin.createEntityForRetrieval(this.entityId);
        final T potentiallySaved = save(mixin.apply(modifiedPropertiesHolder, validationPrototype));
        return restUtil.singleJSONRepresentation(potentiallySaved);
    }

    /**
     * Checks the entity on validation errors and saves it if validation was successful, returns it "as is" if validation was not successful.
     *
     * @param validatedEntity
     * @return
     */
    private T save(final T validatedEntity) {
        try {
            final Result validationResult = validatedEntity.isValid();
            if (validationResult.isSuccessful()) {
                return mixin.save(validatedEntity);
            } else {
                return validatedEntity;
            }
        } catch (final Exception ex) {
            logger.error("An undesirable error has occured during saving of already successfully validated entity.", ex);
            throw new IllegalStateException(ex);
        }
    }

}
