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
 *
 * Responds to GET requests with generated application specific main Web UI component, which basically represents a scaffolding for the whole application Web UI client.
 *
 * @author TG Team
 *
 */
public class MainWebUiComponentResource  extends AbstractWebResource {
    private final ISourceController sourceController;

    /**
     * Creates {@link MainWebUiComponentResource} instance.
     *
     * @param sourceController
     * @param context
     * @param request
     * @param response
     */
    public MainWebUiComponentResource(final ISourceController sourceController, final IDeviceProvider deviceProvider, final Context context, final Request request, final Response response) {
        super(context, request, response, deviceProvider);
        this.sourceController = sourceController;
    }

    @Get
    @Override
    public Representation get() {
        final String source = sourceController.loadSource("/app/tg-app.html", device());
        return new EncodeRepresentation(Encoding.GZIP, new InputRepresentation(new ByteArrayInputStream(source.getBytes(Charsets.UTF_8)), MediaType.TEXT_JAVASCRIPT));
    }

}
