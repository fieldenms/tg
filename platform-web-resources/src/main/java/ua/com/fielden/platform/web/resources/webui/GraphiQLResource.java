package ua.com.fielden.platform.web.resources.webui;

import static com.google.common.base.Charsets.UTF_8;
import static org.restlet.data.MediaType.TEXT_HTML;
import static ua.com.fielden.platform.web.resources.RestServerUtil.encodedRepresentation;

import java.io.ByteArrayInputStream;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;

import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.security.tokens.web_api.GraphiQL_CanExecute_Token;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.interfaces.IDeviceProvider;
import ua.com.fielden.platform.web.ioc.exceptions.MissingWebResourceException;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web_api.IWebApi;

/**
 * A web resource for GraphiQL Web API interface.
 *
 * @author TG Team
 *
 */
public class GraphiQLResource extends AbstractWebResource {
    private final Logger logger = LogManager.getLogger(GraphiQLResource.class);
    private final IAuthorisationModel authModel;
    private final IWebApi webApi;
    private final RestServerUtil restUtil;

    /**
     * Creates {@link GraphiQLResource}.
     */
    public GraphiQLResource(
            final IWebApi webApi,
            final IAuthorisationModel authModel,
            final RestServerUtil restUtil,
            final IDeviceProvider deviceProvider,
            final IDates dates,
            final Context context,
            final Request request,
            final Response response) {
        super(context, request, response, deviceProvider, dates);
        this.authModel = authModel;
        this.webApi = webApi;
        this.restUtil = restUtil;
    }
    
    @Get
    public Representation graphiQL() {
        // first check request authorisation
        final Result authRes = authModel.authorise(GraphiQL_CanExecute_Token.class);
        if (!authRes.isSuccessful()) {
            getResponse().setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
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

    /**
     * Handles GraphQL query POST request.
     */
    @Post
    public Representation query(final Representation envelope) {
        // first check request authorisation
        final Result authRes = authModel.authorise(GraphiQL_CanExecute_Token.class);
        if (!authRes.isSuccessful()) {
            getResponse().setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            return encodedRepresentation(new ByteArrayInputStream(authRes.getMessage().getBytes(UTF_8)), TEXT_HTML);
        }

        // if authorisation succeeded, we can proceed with query processing
        return restUtil.webApiResultRepresentation(webApi.execute((Map<String, Object>) restUtil.restoreJsonMap(envelope)));
    }

}