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

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.resources.RestServerUtil;

/**
 * Represents web server resource that returns entity grid inspector component for the specified 'miType' to the client.
 *
 * @author TG Team
 *
 */
public class CentreEgiResource extends ServerResource {
    private final EntityCentre<? extends AbstractEntity<?>> centre;
    private final RestServerUtil restUtil;

    /**
     * Creates {@link CentreEgiResource} and initialises it with centre instance.
     *
     * @param centre
     * @param context
     * @param request
     * @param response
     */
    public CentreEgiResource(
            final RestServerUtil restUtil,
            final EntityCentre<? extends AbstractEntity<?>> centre,//
            final Context context, //
            final Request request, //
            final Response response) {
        init(context, request, response);
        this.restUtil = restUtil;
        this.centre = centre;
    }

    @Override
    protected Representation get() throws ResourceException {
        return EntityResourceUtils.handleUndesiredExceptions(() -> {
            try {
                return new EncodeRepresentation(Encoding.GZIP, new InputRepresentation(new ByteArrayInputStream(centre.build().render().toString().getBytes("UTF-8"))));
            } catch (final UnsupportedEncodingException e) {
                e.printStackTrace();
                throw new ResourceException(e);
            }
        }, restUtil);
    }
}
