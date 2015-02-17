package ua.com.fielden.platform.web.resources.webui;

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

/**
 * Represents web server resource that retrievers the entity centre configuration and returns it to the client..
 *
 * @author TG Team
 *
 */
public class MasterComponentResource extends ServerResource {

    private final String master;

    /**
     * Creates {@link MasterComponentResource} and initialises it with master instance.
     *
     * @param centre
     * @param context
     * @param request
     * @param response
     */
    public MasterComponentResource(//
    final String master,//
            final Context context, //
            final Request request, //
            final Response response) {
        init(context, request, response);
        this.master = master;
    }

    @Override
    protected Representation get() throws ResourceException {
        try {
            return new EncodeRepresentation(Encoding.GZIP, new InputRepresentation(new ByteArrayInputStream(master.getBytes("UTF-8"))));
        } catch (final UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new ResourceException(e);
        }
    }
}
