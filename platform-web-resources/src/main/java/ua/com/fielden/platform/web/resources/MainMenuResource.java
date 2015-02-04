package ua.com.fielden.platform.web.resources;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Encoding;
import org.restlet.engine.application.EncodeRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ua.com.fielden.platform.web.app.WebApp;

public class MainMenuResource extends ServerResource {

    private final WebApp app;

    public MainMenuResource(final WebApp app, final Context context, final Request request, final Response response) {
        init(context, request, response);
        this.app = app;
    }

    @Override
    protected Representation get() throws ResourceException {
        try {
            return new EncodeRepresentation(Encoding.GZIP, new InputRepresentation(new ByteArrayInputStream(app.generateMainMenu().getBytes("UTF-8"))));
        } catch (final UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new ResourceException(e);
        }
    }

}
