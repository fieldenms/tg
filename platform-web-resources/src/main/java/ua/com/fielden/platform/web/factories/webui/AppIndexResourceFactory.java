package ua.com.fielden.platform.web.factories.webui;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import ua.com.fielden.platform.domaintree.IServerGlobalDomainTreeManager;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.web.app.ISourceController;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.resources.webui.AppIndexResource;

/**
 * The resource factory for main application 'html' resource (similar to 'index.html' in its classical meaning).
 *
 * @author TG Team
 *
 */
public class AppIndexResourceFactory extends Restlet {
    private final ISourceController sourceController;
    private final IServerGlobalDomainTreeManager serverGdtm;
    private final IWebUiConfig webUiConfig;
    private final IUserProvider userProvider;
    
    public AppIndexResourceFactory(
            final ISourceController sourceController, 
            final IServerGlobalDomainTreeManager serverGdtm,
            final IWebUiConfig webUiConfig,
            final IUserProvider userProvider) {
        this.sourceController = sourceController;
        this.serverGdtm = serverGdtm;
        this.webUiConfig = webUiConfig;
        this.userProvider = userProvider;
    }
    
    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);
        
        if (Method.GET == request.getMethod()) {
            new AppIndexResource(sourceController, serverGdtm, webUiConfig, userProvider, getContext(), request, response).handle();
        }
    }
    
}