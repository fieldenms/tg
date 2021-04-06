package ua.com.fielden.platform.web.resources.webui;

import static com.google.common.base.Charsets.UTF_8;
import static org.restlet.data.MediaType.TEXT_JAVASCRIPT;
import static ua.com.fielden.platform.web.resources.RestServerUtil.encodedRepresentation;

import java.io.ByteArrayInputStream;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.app.IWebResourceLoader;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;

/**
 * Responds to GET request with service worker resource.
 *
 * @author TG Team
 *
 */
public class ServiceWorkerResource extends AbstractWebResource {
    private final IWebResourceLoader webResourceLoader;
    
    /**
     * Creates {@link ServiceWorkerResource} instance.
     *
     * @param context
     * @param request
     * @param response
     */
    public ServiceWorkerResource(
            final IWebResourceLoader webResourceLoader,
            final IDeviceProvider deviceProvider,
            final IDates dates,
            final Context context, 
            final Request request, 
            final Response response) {
        super(context, request, response, deviceProvider, dates);
        this.webResourceLoader = webResourceLoader;
    }
    
    @Get
    @Override
    public Representation get() {
        return encodedRepresentation(new ByteArrayInputStream(webResourceLoader.loadSource("/resources/service-worker.js").get().getBytes(UTF_8)), TEXT_JAVASCRIPT);
    }
    
}