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
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.centre.EntityCentre;

/**
 * Represents web server resource that returns entity centre component for specified mi type to the client.
 *
 * @author TG Team
 *
 */
public class LoginResource extends ServerResource {

    /**
     * Creates {@link LoginResource} and initialises it with centre instance.
     *
     * @param centre
     * @param context
     * @param request
     * @param response
     */
    public LoginResource(//
            final Context context, //
            final Request request, //
            final Response response) {
        init(context, request, response);
    }

    @Override
    protected Representation get() throws ResourceException {
        try {
            // check if there is a valid authenticator
            // if there is then should respond with redirection to root /.

            final byte[] body = ResourceLoader.getText("ua/com/fielden/platform/web/index.html").replaceAll("@title", "").getBytes("UTF-8");

            return new EncodeRepresentation(Encoding.GZIP, new InputRepresentation(new ByteArrayInputStream(body)));
        } catch (final UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new ResourceException(e);
        }
    }


}
