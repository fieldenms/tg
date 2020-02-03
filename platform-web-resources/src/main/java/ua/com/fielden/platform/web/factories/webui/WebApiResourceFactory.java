package ua.com.fielden.platform.web.factories.webui;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import com.google.inject.Injector;

import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.webui.WebApiResource;
import ua.com.fielden.platform.web_api.IWebApi;

/**
 * A factory for Web API resources.
 *
 * @author TG Team
 *
 */
public class WebApiResourceFactory extends Restlet {
    private final IWebApi webApi;
    private final RestServerUtil restUtil;
    
    /**
     * Instantiates a factory for Web API resources.
     *
     * @param injector
     */
    public WebApiResourceFactory(final Injector injector) {
        webApi = injector.getInstance(IWebApi.class);
        restUtil = injector.getInstance(RestServerUtil.class);
    }
    
    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);
        if (Method.POST == request.getMethod()) {
            new WebApiResource(webApi, restUtil, getContext(), request, response).handle();
        }
    }
    
}