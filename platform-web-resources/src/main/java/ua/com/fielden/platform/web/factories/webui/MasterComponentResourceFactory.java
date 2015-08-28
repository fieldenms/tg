package ua.com.fielden.platform.web.factories.webui;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import com.google.inject.Injector;

import ua.com.fielden.platform.web.app.IPreloadedResources;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.webui.MasterComponentResource;

/**
 * The server resource factory for entity masters.
 *
 * The master identification information is a part of the URI: "/master_ui/{entityType}".
 *
 * @author TG Team
 *
 */
public class MasterComponentResourceFactory extends Restlet {
    private final IPreloadedResources preloadedResources;
    private final RestServerUtil restUtil;

    /**
     * Creates the {@link MasterComponentResourceFactory} instance.
     *
     * @param centres
     */
    public MasterComponentResourceFactory(final IPreloadedResources preloadedResources, final Injector injector) {
        this.preloadedResources = preloadedResources;
        this.restUtil = injector.getInstance(RestServerUtil.class);
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.GET.equals(request.getMethod())) {
            new MasterComponentResource(
                    restUtil,
                    preloadedResources,
                    getContext(),
                    request,
                    response //
            ).handle();
        }
    }
}
