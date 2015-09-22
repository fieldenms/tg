package ua.com.fielden.platform.web.resources.webui;

import java.io.ByteArrayInputStream;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Encoding;
import org.restlet.engine.application.EncodeRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import ua.com.fielden.platform.web.app.ISourceController;
import ua.com.fielden.platform.web.resources.RestServerUtil;

import com.google.common.base.Charsets;

/**
 * Represents web server resource that returns entity grid inspector component for the specified 'miType' to the client.
 *
 * @author TG Team
 *
 */
public class CentreEgiResource extends DeviceProfileDifferentiatorResource {
    private final String mitypeString;

    /**
     * Creates {@link CentreEgiResource} and initialises it with centre instance.
     *
     * @param centre
     * @param context
     * @param request
     * @param response
     */
    public CentreEgiResource(
            final ISourceController sourceController,//
            final RestServerUtil restUtil,
            final Context context, //
            final Request request, //
            final Response response) {
        super(sourceController, restUtil, context, request, response);
        this.mitypeString = (String) request.getAttributes().get("mitype");
    }

    @Override
    protected Representation get() throws ResourceException {
        return EntityResourceUtils.handleUndesiredExceptions(getResponse(), () -> {
            final String source = sourceController().loadSource("/centre_ui/egi/" + this.mitypeString, deviceProfile());
            return new EncodeRepresentation(Encoding.GZIP, new InputRepresentation(new ByteArrayInputStream(source.getBytes(Charsets.UTF_8))));
        }, restUtil());
    }
}
