package ua.com.fielden.platform.web.resources.webui;

import static org.restlet.data.MediaType.TEXT_HTML;
import static ua.com.fielden.platform.web.resources.webui.FileResource.createRepresentation;
import static ua.com.fielden.platform.web.utils.WebUiResourceUtils.handleUndesiredExceptions;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import ua.com.fielden.platform.web.app.ISourceController;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.resources.RestServerUtil;

/**
 * Represents web server resource that returns custom view component for specified view name to the client.
 *
 * @author TG Team
 *
 */
public class CustomViewResource extends AbstractWebResource {
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
            final IDeviceProvider deviceProvider,
            final Context context,
            final Request request,
            final Response response //
    ) {
        super(context, request, response, deviceProvider);
        this.viewName = (String) request.getAttributes().get("viewName");
        this.sourceController = sourceController;
        this.restUtil = restUtil;
    }

    @Get
    @Override
    public Representation get() {
        return handleUndesiredExceptions(getResponse(), () -> {
            return createRepresentation(sourceController, TEXT_HTML, "/custom_view/" + this.viewName, getReference().getRemainingPart());
        }, restUtil);
    }
}
