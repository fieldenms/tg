package ua.com.fielden.platform.web.factories.webui;

import static org.restlet.data.Method.GET;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;

import ua.com.fielden.platform.utils.IUniversalConstants;
import ua.com.fielden.platform.web.app.IWebResourceLoader;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.webui.ServiceWorkerResource;

/**
 * Resource factory for service worker resource.
 *
 * @author TG Team
 *
 */
public class ServiceWorkerResourceFactory extends Restlet {
    private final IWebResourceLoader webResourceLoader;
    private final IDeviceProvider deviceProvider;
    private final IUniversalConstants universalConstants;
    
    public ServiceWorkerResourceFactory(
            final IWebResourceLoader webResourceLoader, 
            final IDeviceProvider deviceProvider,
            final IUniversalConstants universalConstants) {
        this.webResourceLoader = webResourceLoader;
        this.deviceProvider = deviceProvider;
        this.universalConstants = universalConstants;
    }
    
    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);
        
        if (GET == request.getMethod()) {
            new ServiceWorkerResource(webResourceLoader, deviceProvider, universalConstants, getContext(), request, response).handle();
        }
    }
    
}