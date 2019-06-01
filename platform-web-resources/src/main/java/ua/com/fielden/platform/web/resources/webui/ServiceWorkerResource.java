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

import ua.com.fielden.platform.web.app.ISourceController;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;

/**
 * Responds to GET request with service worker resource.
 *
 * @author TG Team
 *
 */
public class ServiceWorkerResource extends AbstractWebResource {
    private final ISourceController sourceController;
    
    /**
     * Creates {@link ServiceWorkerResource} instance.
     *
     * @param context
     * @param request
     * @param response
     */
    public ServiceWorkerResource(
            final ISourceController sourceController,
            final IDeviceProvider deviceProvider,
            final Context context, 
            final Request request, 
            final Response response) {
        super(context, request, response, deviceProvider);
        this.sourceController = sourceController;
    }
    
    @Get
    @Override
    public Representation get() {
        return encodedRepresentation(new ByteArrayInputStream(sourceController.loadSource("/resources/service-worker.js").getBytes(UTF_8)), TEXT_JAVASCRIPT);
    }
    
}