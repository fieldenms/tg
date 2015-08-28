package ua.com.fielden.platform.web.factories.webui;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import com.google.inject.Injector;

import ua.com.fielden.platform.web.app.IPreloadedResources;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.webui.CentreComponentResource;

/**
 * The server resource factory for entity centres.
 *
 * The centre identification information is a part of the URI: "/centre_ui/{mitype}".
 *
 * @author TG Team
 *
 */
public class CentreComponentResourceFactory extends Restlet {
    private final IPreloadedResources preloadedResources;
    private final RestServerUtil restUtil;

    /**
     * Creates the {@link CentreComponentResourceFactory} instance.
     *
     * @param centres
     */
    public CentreComponentResourceFactory(final IPreloadedResources preloadedResources, final Injector injector) {
        this.preloadedResources = preloadedResources;
        this.restUtil = injector.getInstance(RestServerUtil.class);
    }

    /**
     * Invokes on GET request from client.
     */
    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.GET.equals(request.getMethod())) {
            new CentreComponentResource(
                    restUtil,
                    preloadedResources,
                    getContext(),
                    request,
                    response //
            ).handle();
        }
    }
}
