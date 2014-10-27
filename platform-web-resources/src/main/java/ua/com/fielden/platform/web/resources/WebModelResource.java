package ua.com.fielden.platform.web.resources;

import org.apache.commons.io.IOUtils;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Encoding;
import org.restlet.data.MediaType;
import org.restlet.engine.application.EncodeRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ua.com.fielden.platform.web.centre.EntityCentre;

/**
 * Represents web server resource that retrievers the entity centre configuration and returns it to the client..
 *
 * @author TG Team
 *
 */
public class WebModelResource extends ServerResource {
    private final String generatedWebModel;

    /**
     * Creates {@link WebModelResource} and initialises it with {@link EntityCentre} instance.
     *
     * @param centre
     * @param context
     * @param request
     * @param response
     * @param gdtm
     */
    public WebModelResource(//
	    final String generatedWebModel,//
            final Context context, //
            final Request request, //
            final Response response) {
        init(context, request, response);
        this.generatedWebModel = generatedWebModel;
    }

    @Override
    protected Representation get() throws ResourceException {
	return new EncodeRepresentation(Encoding.GZIP, new InputRepresentation(IOUtils.toInputStream(generatedWebModel), MediaType.TEXT_JAVASCRIPT));
    }
}
