package ua.com.fielden.platform.web.factories.webui;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.IServerGlobalDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.reflection.ClassesRetriever;
import ua.com.fielden.platform.security.provider.IUserEx;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.webui.EntityResource;
import ua.com.fielden.platform.web.view.master.EntityMaster;

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
    private final IWebUiConfig webApp;
    private final ICriteriaGenerator critGenerator;

    /**
     * Instantiates a factory for entity resources.
     *
     * @param masters2
     *            -- a list of {@link EntityMaster}s from which fetch models and other information arrive
     * @param injector
     */
    public EntityResourceFactory(final IWebUiConfig webApp, final Injector injector) {
        this.webApp = webApp;
        this.injector = injector;
        this.restUtil = injector.getInstance(RestServerUtil.class);
        this.factory = injector.getInstance(EntityFactory.class);
        this.critGenerator = injector.getInstance(ICriteriaGenerator.class);
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.POST == request.getMethod() || Method.PUT == request.getMethod() || Method.DELETE == request.getMethod()) {
            final String username = (String) request.getAttributes().get("username");
            injector.getInstance(IUserProvider.class).setUsername(username, injector.getInstance(IUserEx.class));

            final String entityTypeString = (String) request.getAttributes().get("entityType");
            final Class<? extends AbstractEntity<?>> entityType = (Class<? extends AbstractEntity<?>>) ClassesRetriever.findClass(entityTypeString);
            final EntityMaster<? extends AbstractEntity<?>> master = this.webApp.getMasters().get(entityType);

            final IGlobalDomainTreeManager gdtm = injector.getInstance(IServerGlobalDomainTreeManager.class).get(username);

            final EntityResource<AbstractEntity<?>> resource = new EntityResource<>((Class<AbstractEntity<?>>) entityType, (IEntityProducer<AbstractEntity<?>>) master.createEntityProducer(), factory, restUtil, injector.getInstance(ICompanionObjectFinder.class), gdtm, this.critGenerator, getContext(), request, response);
            resource.handle();
        }
    }
}
