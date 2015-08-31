package ua.com.fielden.platform.web.resources;

import java.io.ByteArrayInputStream;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Encoding;
import org.restlet.engine.application.EncodeRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.google.common.base.Charsets;

import ua.com.fielden.platform.web.app.ISourceController;

/**
 *
 * Responds to GET requests with generated application specific main Web UI component, which basically represents a scaffolding for the whole application Web UI client.
 *
 * @author TG Team
 *
 */
public class MainWebUiComponentResource extends ServerResource {
    private final ISourceController sourceController;

    public MainWebUiComponentResource(final ISourceController sourceController, final Context context, final Request request, final Response response) {
        init(context, request, response);
        this.sourceController = sourceController;
    }

    @Override
    protected Representation get() throws ResourceException {
        final String source = sourceController.loadSource("/app/tg-app.html");
        return new EncodeRepresentation(Encoding.GZIP, new InputRepresentation(new ByteArrayInputStream(source.getBytes(Charsets.UTF_8))));
    }
}
