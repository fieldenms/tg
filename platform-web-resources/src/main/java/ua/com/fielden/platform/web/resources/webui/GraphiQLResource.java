package ua.com.fielden.platform.web.resources.webui;

import static com.google.common.base.Charsets.UTF_8;
import static org.restlet.data.MediaType.TEXT_HTML;
import static ua.com.fielden.platform.web.resources.RestServerUtil.encodedRepresentation;

import java.io.ByteArrayInputStream;

import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import ua.com.fielden.platform.web.app.IWebResourceLoader;

/**
 * A web resource for GraphiQL Web API interface.
 *
 * @author TG Team
 *
 */
public class GraphiQLResource extends ServerResource {
    private final Logger logger = Logger.getLogger(GraphiQLResource.class);
    private final IWebResourceLoader webResourceLoader;
    
    /**
     * Creates {@link GraphiQLResource}.
     */
    public GraphiQLResource(
            final IWebResourceLoader webResourceLoader,
            final Context context,
            final Request request,
            final Response response) {
        init(context, request, response);
        this.webResourceLoader = webResourceLoader;
    }
    
    @Get
    public Representation graphiQL() {
        try {
            final String source = webResourceLoader.loadSource("/resources/api/graphiql.html");
            return encodedRepresentation(new ByteArrayInputStream(source.getBytes(UTF_8)), TEXT_HTML);
        } catch (final Exception ex) {
            logger.fatal(ex);
            throw new IllegalStateException(ex);
        }
    }
    
}