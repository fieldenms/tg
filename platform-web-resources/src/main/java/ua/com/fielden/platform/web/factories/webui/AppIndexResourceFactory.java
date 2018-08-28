package ua.com.fielden.platform.web.factories.webui;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.web.app.ISourceController;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.webui.AppIndexResource;

/**
 * The resource factory for main application 'html' resource (similar to 'index.html' in its classical meaning).
 *
 * @author TG Team
 *
 */
public class AppIndexResourceFactory extends Restlet {
    private final ISourceController sourceController;
    private final IWebUiConfig webUiConfig;
    private final IUserProvider userProvider;
    private final IDeviceProvider deviceProvider;
    
    public AppIndexResourceFactory(
            final ISourceController sourceController, 
            final IWebUiConfig webUiConfig,
            final IUserProvider userProvider,
            final IDeviceProvider deviceProvider) {
        this.sourceController = sourceController;
        this.webUiConfig = webUiConfig;
        this.userProvider = userProvider;
        this.deviceProvider = deviceProvider;
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.GET == request.getMethod()) {
            new AppIndexResource(sourceController, webUiConfig, userProvider, deviceProvider, getContext(), request, response).handle();
        }
    }

}