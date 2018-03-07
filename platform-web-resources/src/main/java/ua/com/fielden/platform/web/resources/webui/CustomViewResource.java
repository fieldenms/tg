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

import ua.com.fielden.platform.web.app.ISourceController;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import com.google.common.base.Charsets;

/**
 * Represents web server resource that returns custom view component for specified view name to the client.
 *
 * @author TG Team
 *
 */
public class CustomViewResource extends DeviceProfileDifferentiatorResource {
    private final String viewName;
    private final ISourceController sourceController;
    private final RestServerUtil restUtil;
    
    /**
     * Creates {@link CustomViewResource} and initialises it view name.
     *
     * @param centre
     * @param context
     * @param request
     * @param response
     */
    public CustomViewResource(
            final ISourceController sourceController,//
            final RestServerUtil restUtil,
            final Context context,
            final Request request,
            final Response response //
    ) {
        super(context, request, response);
        this.viewName = (String) request.getAttributes().get("viewName");
        this.sourceController = sourceController;
        this.restUtil = restUtil;
    }

    @Override
    protected Representation get() throws ResourceException {
        return handleUndesiredExceptions(getResponse(), () -> {
            final String source = sourceController.loadSource("/custom_view/" + this.viewName, deviceProfile());
            return new EncodeRepresentation(Encoding.GZIP, new InputRepresentation(new ByteArrayInputStream(source.getBytes(Charsets.UTF_8))));
        }, restUtil);
    }
}
