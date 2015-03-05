package ua.com.fielden.platform.web.factories.webui;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.reflection.ClassesRetriever;
import ua.com.fielden.platform.security.provider.IUserController;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.web.app.WebApp;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.webui.CriteriaResource;

import com.google.inject.Injector;

/**
 * A factory for criteria resources which instantiate resources based on entity type.
 *
 * The entity type information is a part of the URI: "/users/{username}/criteria/{mitype}".
 *
 * @author TG Team
 *
 */
public class CriteriaResourceFactory extends Restlet {
    private final Injector injector;
    private final RestServerUtil restUtil;
    private final EntityFactory factory;
    private final ICompanionObjectFinder companionFinder;
    private final WebApp webApp;
    private final ICriteriaGenerator critGenerator;

    /**
     * Instantiates a factory for entity resources.
     *
     */
    public CriteriaResourceFactory(final WebApp webApp, final Injector injector) {
        this.webApp = webApp;
        this.injector = injector;
        this.restUtil = injector.getInstance(RestServerUtil.class);
        this.critGenerator = injector.getInstance(ICriteriaGenerator.class);
        this.factory = injector.getInstance(EntityFactory.class);
        this.companionFinder = injector.getInstance(ICompanionObjectFinder.class);
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.GET == request.getMethod() || Method.POST == request.getMethod()) {
            final String username = (String) request.getAttributes().get("username");
            injector.getInstance(IUserProvider.class).setUsername(username, injector.getInstance(IUserController.class));

            final String mitypeString = (String) request.getAttributes().get("mitype");
            final Class<? extends MiWithConfigurationSupport<?>> miType = (Class<? extends MiWithConfigurationSupport<?>>) ClassesRetriever.findClass(mitypeString);
            final EntityCentre centre = this.webApp.getCentres().get(miType);

            final IGlobalDomainTreeManager gdtm = injector.getInstance(IGlobalDomainTreeManager.class);

            final CriteriaResource<AbstractEntity<?>> resource = new CriteriaResource<>(restUtil, factory, companionFinder, centre, gdtm, this.critGenerator, getContext(), request, response);
            resource.handle();
        }
    }
}
