package ua.com.fielden.platform.web.resources.webui;

import static org.restlet.data.MediaType.TEXT_JAVASCRIPT;
import static ua.com.fielden.platform.web.resources.webui.FileResource.createRepresentation;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.app.IWebResourceLoader;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;

/**
 * Resource for tg-reflector component.
 *
 * @author TG Team
 *
 * @param <T>
 * @param <DAO>
 */
public class TgReflectorComponentResource extends AbstractWebResource {
    private final IWebResourceLoader webResourceLoader;

    public TgReflectorComponentResource(final IWebResourceLoader webResourceLoader, final IDeviceProvider deviceProvider, final IDates dates, final Context context, final Request request, final Response response) {
        super(context, request, response, deviceProvider, dates);
        this.webResourceLoader = webResourceLoader;
    }

    /**
     * Handles sending of the serialised testing entities to the Web UI client (GET method).
     */
    @Get
    @Override
    public Representation get() {
        return createRepresentation(webResourceLoader, TEXT_JAVASCRIPT, "/app/tg-reflector.js", getReference().getRemainingPart());
    }

}