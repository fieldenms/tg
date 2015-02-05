package ua.com.fielden.platform.web.factories.webui;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.reflection.ClassesRetriever;
import ua.com.fielden.platform.security.provider.IUserController;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.web.master.EntityMaster;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.webui.EntityResource;

import com.google.inject.Injector;

/**
 * A factory for entity resources which instantiate resources based on entity type.
 *
 * The entity type information is a part of the URI: "/users/{username}/entity/{entityType}/{entity-id}".
 *
 * @author TG Team
 *
 */
public class EntityResourceFactory extends Restlet {

    private final Injector injector;
    private final RestServerUtil restUtil;
    private final EntityFactory factory;
    private final Map<Class<? extends AbstractEntity<?>>, EntityMaster<? extends AbstractEntity<?>>> masters;

    /**
     * Instantiates a factory for entity resources.
     *
     * @param masters
     *            -- a list of {@link EntityMaster}s from which fetch models and other information arrive
     * @param injector
     */
    public EntityResourceFactory(final List<EntityMaster<? extends AbstractEntity<?>>> masters, final Injector injector) {
        this.masters = new LinkedHashMap<>();
        for (final EntityMaster<? extends AbstractEntity<?>> master : masters) {
            this.masters.put(master.getEntityType(), master);
        }
        this.injector = injector;
        this.restUtil = injector.getInstance(RestServerUtil.class);
        this.factory = injector.getInstance(EntityFactory.class);
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.GET == request.getMethod() || Method.POST == request.getMethod() || Method.PUT == request.getMethod()) {
            final String username = (String) request.getAttributes().get("username");
            injector.getInstance(IUserProvider.class).setUsername(username, injector.getInstance(IUserController.class));

            final String entityTypeString = (String) request.getAttributes().get("entityType");
            final Class<? extends AbstractEntity<?>> entityType = (Class<? extends AbstractEntity<?>>) ClassesRetriever.findClass(entityTypeString);
            final EntityMaster<? extends AbstractEntity<?>> master = this.masters.get(entityType);

            final EntityResource<AbstractEntity<?>> resource = new EntityResource<AbstractEntity<?>>((Class<AbstractEntity<?>>) entityType, (IEntityProducer<AbstractEntity<?>>) master.createEntityProducer(injector), factory, restUtil, injector.getInstance(ICompanionObjectFinder.class), getContext(), request, response);
            resource.handle();
        }
    }
}
