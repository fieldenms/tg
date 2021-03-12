package ua.com.fielden.platform.web.factories.webui;

import static org.restlet.data.Method.GET;
import static org.restlet.data.Method.POST;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;

import com.google.inject.Injector;

import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.webui.GraphiQLResource;
import ua.com.fielden.platform.web_api.IWebApi;

/**
 * A factory for GraphiQL resource.
 *
 * @author TG Team
 *
 */
public class GraphiQLResourceFactory extends Restlet {
    private final Injector injector;
    
    public GraphiQLResourceFactory(final Injector injector) {
        this.injector = injector;
    }
    
    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);
        
        if (GET == request.getMethod() || POST == request.getMethod()) {
            new GraphiQLResource(
                    injector.getInstance(IWebApi.class),
                    injector.getInstance(IAuthorisationModel.class),
                    injector.getInstance(RestServerUtil.class),
                    getContext(), request, response).handle();
        }
    }
    
}