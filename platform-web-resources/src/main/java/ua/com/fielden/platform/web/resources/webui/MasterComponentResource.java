package ua.com.fielden.platform.web.resources.webui;

import static org.restlet.data.MediaType.TEXT_JAVASCRIPT;
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
 * Represents web server resource that returns entity master component for specified entity type to the client.
 *
 * @author TG Team
 *
 */
public class MasterComponentResource extends AbstractWebResource {
    private final String entityTypeString;
    private final ISourceController sourceController;
    private final RestServerUtil restUtil;
    
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
            final IDeviceProvider deviceProvider,
            final Context context,
            final Request request,
            final Response response //
    ) {
        super(context, request, response, deviceProvider);
        this.entityTypeString = (String) request.getAttributes().get("entityType");
        this.sourceController = sourceController;
        this.restUtil = restUtil;
    }
    
    @Get
    @Override
    public Representation get() {
        return handleUndesiredExceptions(getResponse(), () -> {
            return createRepresentation(sourceController, TEXT_JAVASCRIPT, "/master_ui/" + this.entityTypeString, getReference().getRemainingPart());
        }, restUtil);
    }
    
}