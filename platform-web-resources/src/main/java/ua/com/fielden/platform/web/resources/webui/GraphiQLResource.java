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

import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.security.tokens.web_api.GraphiQL_CanExecute_Token;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.app.IWebResourceLoader;
import ua.com.fielden.platform.web.ioc.exceptions.MissingWebResourceException;
import ua.com.fielden.platform.web.resources.RestServerUtil;

/**
 * A web resource for GraphiQL Web API interface.
 *
 * @author TG Team
 *
 */
public class GraphiQLResource extends ServerResource {
    private final Logger logger = Logger.getLogger(GraphiQLResource.class);
    private final IWebResourceLoader webResourceLoader;
    private final IAuthorisationModel authModel;
    
    /**
     * Creates {@link GraphiQLResource}.
     */
    public GraphiQLResource(
            final IWebResourceLoader webResourceLoader,
            final IAuthorisationModel authModel,
            final Context context,
            final Request request,
            final Response response) {
        init(context, request, response);
        this.webResourceLoader = webResourceLoader;
        this.authModel = authModel;
    }
    
    @Get
    public Representation graphiQL() {
        // first check request authorisation
        final Result authRes = authModel.authorise(GraphiQL_CanExecute_Token.class);
        if (!authRes.isSuccessful()) {
            return encodedRepresentation(new ByteArrayInputStream(authRes.getMessage().getBytes(UTF_8)), TEXT_HTML);
        }

        // if authorisation succeeded we can proceed with service the resource.
        try {
            final String source = ResourceLoader.getText("ua/com/fielden/platform/restricted_resources/graphiql.html");
            if (source == null) {
                new MissingWebResourceException("GraphiQL resource is missing.");
            }
            return encodedRepresentation(new ByteArrayInputStream(source.getBytes(UTF_8)), TEXT_HTML);
        } catch (final Exception ex) {
            logger.fatal(ex);
            return encodedRepresentation(new ByteArrayInputStream(ex.getMessage().getBytes(UTF_8)), TEXT_HTML);
        }
    }

}