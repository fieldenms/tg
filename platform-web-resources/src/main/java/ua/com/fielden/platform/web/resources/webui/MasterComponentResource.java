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

import com.google.common.base.Charsets;

import ua.com.fielden.platform.web.app.ISourceController;
import ua.com.fielden.platform.web.resources.RestServerUtil;

/**
 * Represents web server resource that returns entity master component for specified entity type to the client.
 *
 * @author TG Team
 *
 */
public class MasterComponentResource extends DeviceProfileDifferentiatorResource {
    private final String entityTypeString;

    /**
     * Creates {@link MasterComponentResource} and initialises it with master instance.
     *
     * @param centre
     * @param context
     * @param request
     * @param response
     */
    public MasterComponentResource(
            final ISourceController sourceController,//
            final RestServerUtil restUtil,
            final Context context,
            final Request request,
            final Response response //
    ) {
        super(sourceController, restUtil, context, request, response);
        this.entityTypeString = (String) request.getAttributes().get("entityType");
    }

    @Override
    protected Representation get() throws ResourceException {
        return EntityResourceUtils.handleUndesiredExceptions(getResponse(), () -> {
            final String source = sourceController().loadSource("/master_ui/" + this.entityTypeString, deviceProfile());
            return new EncodeRepresentation(Encoding.GZIP, new InputRepresentation(new ByteArrayInputStream(source.getBytes(Charsets.UTF_8))));
        }, restUtil());
    }
}
