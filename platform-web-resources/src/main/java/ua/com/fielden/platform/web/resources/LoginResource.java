package ua.com.fielden.platform.web.resources;

import static java.lang.String.*;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Encoding;
import org.restlet.data.Status;
import org.restlet.engine.application.EncodeRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.esotericsoftware.kryo.util.Util;
import com.fasterxml.jackson.core.JsonProcessingException;

import ua.com.fielden.platform.utils.ResourceLoader;

/**
 * A web resource handling explicit user logins.
 *
 * @author TG Team
 *
 */
public class LoginResource extends ServerResource {

    protected final RestServerUtil restUtil;
    /**
     * Creates {@link LoginResource} and initialises it with centre instance.
     *
     * @param centre
     * @param context
     * @param request
     * @param response
     */
    public LoginResource(//
            final RestServerUtil restUtil,//
            final Context context, //
            final Request request, //
            final Response response) {
        init(context, request, response);
        this.restUtil = restUtil;
    }

    @Override
    protected Representation get() throws ResourceException {
        try {
            // check if there is a valid authenticator
            // if there is then should respond with redirection to root /.

            final byte[] body = ResourceLoader.getText("ua/com/fielden/platform/web/login.html").replaceAll("@title", "Login").getBytes("UTF-8");

            return new EncodeRepresentation(Encoding.GZIP, new InputRepresentation(new ByteArrayInputStream(body)));
        } catch (final UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new ResourceException(e);
        }
    }

    @Put
    @Override
    public Representation put(final Representation entity) throws ResourceException {
        try {
            final String username = getRequest().getResourceRef().getQueryAsForm().getFirstValue("username");
            final String password = getRequest().getResourceRef().getQueryAsForm().getFirstValue("passwd");
            final Boolean trustedDevice = Boolean.parseBoolean(getRequest().getResourceRef().getQueryAsForm().getFirstValue("trusted-device"));

            System.out.println(format("Form data: %s, %s, %s", username, password, trustedDevice));

            if (username.length() < 5) {
                getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            }
            final byte[] body = "/".getBytes("UTF-8");
            return new EncodeRepresentation(Encoding.GZIP, new InputRepresentation(new ByteArrayInputStream(body)));
        } catch (final UnsupportedEncodingException ex) {
            ex.printStackTrace();
            return restUtil.errorJSONRepresentation(ex);
        }
    }

}
