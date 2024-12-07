package ua.com.fielden.platform.web.resources.webui;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.app.IWebResourceLoader;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;

import static org.restlet.data.MediaType.TEXT_JAVASCRIPT;
import static ua.com.fielden.platform.web.application.AbstractWebUiResources.FULLCALENDAR_PATH;
import static ua.com.fielden.platform.web.resources.webui.FileResource.createRepresentation;

/**
 * Responds to GET requests with generated application specific Fullcalendar component.
 *
 * @author TG Team
 *
 */
public class FullcalendarResource extends AbstractWebResource {
    private final IWebResourceLoader webResourceLoader;

    public FullcalendarResource(final IWebResourceLoader webResourceLoader, final IDeviceProvider deviceProvider, final IDates dates, final Context context, final Request request, final Response response) {
        super(context, request, response, deviceProvider, dates);
        this.webResourceLoader = webResourceLoader;
    }

    @Get
    @Override
    public Representation get() {
        return createRepresentation(webResourceLoader, TEXT_JAVASCRIPT, FULLCALENDAR_PATH, getReference().getRemainingPart());
    }

}