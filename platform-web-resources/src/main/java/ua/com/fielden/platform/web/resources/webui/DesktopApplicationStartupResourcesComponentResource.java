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

/**
 * Resource for 'desktop-application-startup-resources' component.
 *
 * @author TG Team
 *
 * @param <T>
 * @param <DAO>
 */
public class DesktopApplicationStartupResourcesComponentResource extends DeviceProfileDifferentiatorResource {
    private final ISourceController sourceController;
    
    public DesktopApplicationStartupResourcesComponentResource(final ISourceController sourceController, final Context context, final Request request, final Response response) {
        super(context, request, response);
        this.sourceController = sourceController;
    }
    
    /**
     * Handles sending of generated 'desktop-application-startup-resources' to the Web UI client (GET method).
     */
    @Override
    protected Representation get() throws ResourceException {
        final String source = sourceController.loadSource("/app/desktop-application-startup-resources.html", deviceProfile());
        return new EncodeRepresentation(Encoding.GZIP, new InputRepresentation(new ByteArrayInputStream(source.getBytes(Charsets.UTF_8))));
    }
    
}
