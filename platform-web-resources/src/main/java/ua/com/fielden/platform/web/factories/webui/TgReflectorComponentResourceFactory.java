package ua.com.fielden.platform.web.factories.webui;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.app.IWebResourceLoader;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.webui.TgReflectorComponentResource;

/**
 * Resource factory for tg-reflector component.
 *
 * @author TG Team
 *
 */
public class TgReflectorComponentResourceFactory extends Restlet {
    private final IWebResourceLoader webResourceLoader;
    private final IDeviceProvider deviceProvider;
    private final IDates dates;

    public TgReflectorComponentResourceFactory(final IWebResourceLoader webResourceLoader, final IDeviceProvider deviceProvider, final IDates dates) {
        this.webResourceLoader = webResourceLoader;
        this.deviceProvider = deviceProvider;
        this.dates = dates;
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.GET == request.getMethod()) {
            final TgReflectorComponentResource resource = new TgReflectorComponentResource(webResourceLoader, deviceProvider, dates, getContext(), request, response);
            resource.handle();
        }
    }

}