package ua.com.fielden.platform.web.factories.webui;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.web.app.ISourceController;
import ua.com.fielden.platform.web.resources.webui.WebUiPreferencesResource;

public class WebUiPreferencesResourceFactory extends Restlet {
    private final ISourceController sourceController;
    private final IUserProvider userProvider;
    
    public WebUiPreferencesResourceFactory(final ISourceController sourceController, final IUserProvider userProvider) {
        this.sourceController = sourceController;
        this.userProvider = userProvider;
    }
    
    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);
        
        if (Method.GET.equals(request.getMethod())) {
            new WebUiPreferencesResource(sourceController, userProvider, getContext(), request, response).handle();
        }
    }
    
}