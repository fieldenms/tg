package ua.com.fielden.platform.web.factories.webui;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import com.google.inject.Injector;

import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.webui.GraphQLQueryResource;
import ua.com.fielden.platform.web_api.GraphQLService;

/**
 * A factory for Web API resources.
 *
 * @author TG Team
 *
 */
public class WebApiResourceFactory extends Restlet {
    private final GraphQLService graphQLService;
    private final RestServerUtil restUtil;
    
    /**
     * Instantiates a factory for Web API resources.
     *
     * @param injector
     */
    public WebApiResourceFactory(final Injector injector) {
        graphQLService = injector.getInstance(GraphQLService.class);
        restUtil = injector.getInstance(RestServerUtil.class);
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.POST == request.getMethod()) {
            final GraphQLQueryResource resource = new GraphQLQueryResource(
                    graphQLService,
                    restUtil,
                    getContext(),
                    request,
                    response
            );
            resource.handle();
        }
    }
}
