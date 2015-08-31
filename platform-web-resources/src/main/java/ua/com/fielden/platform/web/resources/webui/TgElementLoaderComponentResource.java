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
import org.restlet.resource.ServerResource;

import com.google.common.base.Charsets;

import ua.com.fielden.platform.web.app.IPreloadedResources;

/**
 * Resource for tg-element-loader component.
 *
 * @author TG Team
 *
 * @param <T>
 * @param <DAO>
 */
public class TgElementLoaderComponentResource extends ServerResource {
    private final IPreloadedResources preloadedResources;

    public TgElementLoaderComponentResource(final IPreloadedResources preloadedResources, final Context context, final Request request, final Response response) {
        init(context, request, response);
        this.preloadedResources = preloadedResources;
    }


    /**
     * Handles sending of generated tg-element-loader to the Web UI client (GET method).
     */
    @Override
    protected Representation get() throws ResourceException {
        final String source = preloadedResources.getSourceOnTheFly("/app/tg-element-loader.html");
        return new EncodeRepresentation(Encoding.GZIP, new InputRepresentation(new ByteArrayInputStream(source.getBytes(Charsets.UTF_8))));
    }
}
