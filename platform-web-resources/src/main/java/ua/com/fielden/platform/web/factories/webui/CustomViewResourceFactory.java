package ua.com.fielden.platform.web.factories.webui;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.app.IWebResourceLoader;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.webui.CustomViewResource;

/**
 * The server resource factory for custom views.
 *
 * The custom view identification information is a part of the URI: "/custom_view/{viewName}".
 *
 * @author TG Team
 *
 */
public class CustomViewResourceFactory extends Restlet {
    private final IWebResourceLoader webResourceLoader;
    private final RestServerUtil restUtil;
    private final IDeviceProvider deviceProvider;
    private final IDates dates;

    /**
     * Creates the {@link CustomViewResourceFactory} instance.
     *
     */
    public CustomViewResourceFactory(final IWebResourceLoader webResourceLoader, final RestServerUtil restUtil, final IDeviceProvider deviceProvider, final IDates dates) {
        this.webResourceLoader = webResourceLoader;
        this.restUtil = restUtil;
        this.deviceProvider = deviceProvider;
        this.dates = dates;
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.GET.equals(request.getMethod())) {
            new CustomViewResource(
                    webResourceLoader,
                    restUtil,
                    deviceProvider,
                    dates,
                    getContext(),
                    request,
                    response //
            ).handle();
        }
    }
}
