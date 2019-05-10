package ua.com.fielden.platform.web.resources.webui;

import java.io.ByteArrayInputStream;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Encoding;
import org.restlet.data.MediaType;
import org.restlet.engine.application.EncodeRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import com.google.common.base.Charsets;

import ua.com.fielden.platform.web.app.ISourceController;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;

/**
 * Resource for 'application-startup-resources' component.
 *
 * @author TG Team
 *
 */
public class ApplicationStartupResourcesComponentResource extends AbstractWebResource {
    private final ISourceController sourceController;

    public ApplicationStartupResourcesComponentResource(final ISourceController sourceController, final IDeviceProvider deviceProvider, final Context context, final Request request, final Response response) {
        super(context, request, response, deviceProvider);
        this.sourceController = sourceController;
    }

    /**
     * Handles sending of generated 'application-startup-resources' to the Web UI client (GET method).
     */
    @Get
    public Representation loadDesktopAppResources() {
        final String source = sourceController.loadSource("/app/application-startup-resources.js");
        return new EncodeRepresentation(Encoding.GZIP, new InputRepresentation(new ByteArrayInputStream(source.getBytes(Charsets.UTF_8)), MediaType.TEXT_JAVASCRIPT));
    }

}
