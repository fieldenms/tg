package ua.com.fielden.platform.web.resources.webui;

import java.util.Map;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web_api.IWebApi;

/**
 * The web resource for {@link IWebApi}.
 *
 * @author TG Team
 *
 */
public class WebApiResource extends ServerResource {
    private final IWebApi webApi;
    private final RestServerUtil restUtil;
    
    public WebApiResource(
            final IWebApi webApi,
            final RestServerUtil restUtil,
            final Context context,
            final Request request,
            final Response response) {
        init(context, request, response);
        
        this.webApi = webApi;
        this.restUtil = restUtil;
    }
    
    /**
     * Handles GraphQL query POST request.
     */
    @Post
    public Representation query(final Representation envelope) {
        return restUtil.webApiResultRepresentation(webApi.execute((Map<String, Object>) restUtil.restoreJsonMap(envelope)));
    }
    
}