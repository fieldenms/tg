package ua.com.fielden.platform.web.resources.webui;

import static ua.com.fielden.platform.web.utils.WebUiResourceUtils.handleUndesiredExceptions;

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
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.RestServerUtil;

/**
 * Represents web server resource that returns entity grid inspector component for the specified 'miType' to the client.
 *
 * @author TG Team
 *
 */
public class CentreEgiResource extends DeviceProfileDifferentiatorResource {
    private final String mitypeString;
    private final ISourceController sourceController;
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
            final ISourceController sourceController,//
            final RestServerUtil restUtil,
            final IDeviceProvider deviceProvider,
            final Context context, //
            final Request request, //
            final Response response) {
        super(context, request, response, deviceProvider);
        this.mitypeString = (String) request.getAttributes().get("mitype");
        this.sourceController = sourceController;
        this.restUtil = restUtil;
    }
    
    @Override
    protected Representation get() throws ResourceException {
        return handleUndesiredExceptions(getResponse(), () -> {
            final String source = sourceController.loadSource("/centre_ui/egi/" + this.mitypeString, device());
            return new EncodeRepresentation(Encoding.GZIP, new InputRepresentation(new ByteArrayInputStream(source.getBytes(Charsets.UTF_8))));
        }, restUtil);
    }
    
}
