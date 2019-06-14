package ua.com.fielden.platform.web.application;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;
import org.restlet.routing.TemplateRoute;

import ua.com.fielden.platform.web.sse.SseUtils;
import ua.com.fielden.platform.web.sse.exceptions.InvalidSseUriException;
import ua.com.fielden.platform.web.sse.resources.EventSourcingResourceFactory;

/**
 * This router should be used for attaching application specific resources. It provides additional validation for safety and convention compliance.
 * For example, it checks whether SSE resources are bound to valid SSE URIs.
 * 
 * @author TG Team
 *
 */
public class ResourceRouter extends Router {
    public ResourceRouter(Context context) {
        super(context);
    }
    
    /**
     * Validates {@code pathTemplate} for targets of type {@link EventSourcingResourceFactory}.
     */
    @Override
    public TemplateRoute attach(final String pathTemplate, final Restlet target) {
        if ((target instanceof EventSourcingResourceFactory) && !SseUtils.isEventSourceUri(pathTemplate)) {
            throw new InvalidSseUriException(String.format("Path tempalte [%s] is not valid for binding SSE resources.", pathTemplate));
        }
        return super.attach(pathTemplate, target);
    }
}
