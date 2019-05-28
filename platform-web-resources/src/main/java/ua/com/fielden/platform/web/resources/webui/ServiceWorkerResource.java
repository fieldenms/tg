package ua.com.fielden.platform.web.resources.webui;

import static com.google.common.base.Charsets.UTF_8;
import static org.restlet.data.MediaType.APPLICATION_JAVASCRIPT;

import java.io.ByteArrayInputStream;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Encoding;
import org.restlet.engine.application.EncodeRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import ua.com.fielden.platform.web.app.ISourceController;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;

/**
 * Responds to GET request with a generated application specific service worker resource.
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
        final String source = sourceController.loadSource("/resources/sw.js");
        return new EncodeRepresentation(Encoding.GZIP, new InputRepresentation(new ByteArrayInputStream(source.getBytes(UTF_8)), APPLICATION_JAVASCRIPT));
    }
    
}