package ua.com.fielden.platform.web.resources.webui;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchKeyAndDescOnly;
import static ua.com.fielden.platform.web.utils.WebUiResourceUtils.handleUndesiredExceptions;

import java.util.Map;

import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.utils.EntityResourceUtils;

/**
 * The web resource for entity serves as a back-end mechanism of entity retrieval by key representation. It retrieves entities with id only.
 * It provides a base implementation for handling the following methods:
 * <ul>
 * <li>retrieves entity by key representation -- POST request with an envelope containing an key as string;
 * </ul>
 *
 * @author TG Team
 *
 */
public class EntityByKeyResource extends AbstractWebResource {
    private static final Logger LOGGER = Logger.getLogger(EntityByKeyResource.class);

    private final Class<AbstractEntity<?>> entityType;
    private final IEntityDao<AbstractEntity<?>> companion;
    private final RestServerUtil restUtil;

    public EntityByKeyResource(
            final Class<AbstractEntity<?>> entityType,
            final Context context,
            final Request request,
            final Response response,
            final ICompanionObjectFinder companionFinder,
            final IDeviceProvider deviceProvider,
            final IDates dates,
            final RestServerUtil restUtil) {
        super(context, request, response, deviceProvider, dates);
        this.restUtil = restUtil;
        this.entityType = entityType;
        this.companion = companionFinder.find(this.entityType);
    }

    /**
     * Handles POST requests Which returns entity with id only representation.
     */
    @SuppressWarnings("unchecked")
    @Post
    public Representation findByKey(final Representation envelope) {
        LOGGER.debug("ENTITY_BY_KEY_RESOURCE: fetching started.");
        final Representation result = handleUndesiredExceptions(getResponse(), () -> {
            final Map<String, Object> queryObject = (Map<String, Object>) restUtil.restoreJsonMap(envelope);
            return restUtil.singleJsonRepresentation(EntityResourceUtils.findAndFetchBy(queryObject.get("key").toString(), entityType, fetchKeyAndDescOnly(entityType), companion));
        }, restUtil);
        LOGGER.debug("ENTITY_BY_KEY_RESOURCE: fetch finished.");
        return result;
    }
}
