package ua.com.fielden.platform.web.factories.webui;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.IServerGlobalDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.ClassesRetriever;
import ua.com.fielden.platform.security.provider.IUserController;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.webui.CentreResource;

import com.google.inject.Injector;

/**
 * A factory for centre resources which instantiate resources based on mi type.
 *
 * The entity type information is a part of the URI: "/users/{username}/centre/{mitype}".
 *
 * @author TG Team
 *
 */
public class CentreResourceFactory extends Restlet {
    private final Injector injector;
    private final RestServerUtil restUtil;
    private final IWebUiConfig webApp;

    /**
     * Instantiates a factory for centre resources.
     *
     */
    public CentreResourceFactory(final IWebUiConfig webApp, final Injector injector) {
        this.webApp = webApp;
        this.injector = injector;
        this.restUtil = injector.getInstance(RestServerUtil.class);
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.POST == request.getMethod() || Method.DELETE == request.getMethod()) {
            final String username = (String) request.getAttributes().get("username");
            injector.getInstance(IUserProvider.class).setUsername(username, injector.getInstance(IUserController.class));

            final String mitypeString = (String) request.getAttributes().get("mitype");
            final Class<? extends MiWithConfigurationSupport<?>> miType = (Class<? extends MiWithConfigurationSupport<?>>) ClassesRetriever.findClass(mitypeString);
            final EntityCentre centre = this.webApp.getCentres().get(miType);

            final IGlobalDomainTreeManager gdtm = injector.getInstance(IServerGlobalDomainTreeManager.class).get(username);

            final CentreResource<AbstractEntity<?>> resource = new CentreResource<>(restUtil, centre, gdtm, getContext(), request, response);
            resource.handle();
        }
    }
}
