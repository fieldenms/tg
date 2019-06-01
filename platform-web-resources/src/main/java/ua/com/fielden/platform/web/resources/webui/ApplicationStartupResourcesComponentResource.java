package ua.com.fielden.platform.web.resources.webui;

import static org.restlet.data.MediaType.TEXT_JAVASCRIPT;
import static ua.com.fielden.platform.web.resources.webui.FileResource.createRepresentation;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

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
        return createRepresentation(sourceController, TEXT_JAVASCRIPT, "/app/application-startup-resources.js", getReference().getRemainingPart());
    }

}
