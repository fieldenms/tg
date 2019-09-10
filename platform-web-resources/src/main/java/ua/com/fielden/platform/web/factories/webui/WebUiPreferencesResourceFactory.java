package ua.com.fielden.platform.web.factories.webui;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import ua.com.fielden.platform.web.app.IWebResourceLoader;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.webui.WebUiPreferencesResource;

public class WebUiPreferencesResourceFactory extends Restlet {
    private final IWebResourceLoader webResourceLoader;
    private final IDeviceProvider deviceProvider;

    public WebUiPreferencesResourceFactory(final IWebResourceLoader webResourceLoader, final IDeviceProvider deviceProvider) {
        this.webResourceLoader = webResourceLoader;
        this.deviceProvider = deviceProvider;
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.GET.equals(request.getMethod())) {
            new WebUiPreferencesResource(webResourceLoader, deviceProvider, getContext(), request, response).handle();
        }
    }

}