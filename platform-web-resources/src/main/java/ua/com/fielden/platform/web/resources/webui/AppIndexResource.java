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
 * Responds to GET request with a generated application specific index resource (for desktop and mobile web apps).
 * <p>
 * The returned HTML should be thought of as <code>index.html</code> in its classical meaning.
 *
 * @author TG Team
 *
 */
public class AppIndexResource extends DeviceProfileDifferentiatorResource {
    /**
     * Creates {@link AppIndexResource} instance.
     *
     * @param sourceController
     * @param context
     * @param request
     * @param response
     */
    public AppIndexResource(final ISourceController sourceController, final RestServerUtil restUtil, final Context context, final Request request, final Response response) {
        super(sourceController, restUtil, context, request, response);
    }

    @Override
    protected Representation get() throws ResourceException {
        final String source = sourceController().loadSource("/app/tg-app-index.html", deviceProfile());
        return new EncodeRepresentation(Encoding.GZIP, new InputRepresentation(new ByteArrayInputStream(source.getBytes(Charsets.UTF_8))));
    }
}
