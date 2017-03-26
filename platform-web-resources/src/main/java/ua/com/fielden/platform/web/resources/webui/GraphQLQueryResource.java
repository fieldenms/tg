package ua.com.fielden.platform.web.resources.webui;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import graphql.ExecutionResult;
import graphql.GraphQLError;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web_api.GraphQLService;
import ua.com.fielden.platform.web_api.Prettyfier;

/**
 * The web resource for GraphQL queries.
 *
 * @author TG Team
 *
 */
public class GraphQLQueryResource extends ServerResource {
    private final Logger logger = Logger.getLogger(getClass());
    private final GraphQLService graphQLService;
    private final RestServerUtil restUtil;

    public GraphQLQueryResource(
            final GraphQLService graphQLService,
            final RestServerUtil restUtil,
            final Context context,
            final Request request,
            final Response response) {
        init(context, request, response);

        this.graphQLService = graphQLService;
        this.restUtil = restUtil;
    }

    /**
     * Handles GraphQL query POST request.
     */
    @Post
    public Representation query(final Representation envelope) {
        final Map<String, Object> input = (Map<String, Object>) restUtil.restoreJSONMap(envelope);
        logger.error("envelope = " + input);
        
        final Object operationNameObj = input.get("operationName");
        final String operationName = operationNameObj == null ? null : (String) operationNameObj;
        final String queryString = (String) input.get("query");
        final Map<String, Object> variables = (Map<String, Object>) Optional.ofNullable(input.getOrDefault("variables", Collections.emptyMap())).orElse(Collections.emptyMap());
        logger.error("============ GraphQL Query: ============");
        logger.error(queryString);
        logger.error("============ GraphQL Variables: ============");
        logger.error(variables);
        logger.error("============ GraphQL query+variables execute... ============");
        final ExecutionResult execResult = graphQLService.graphQL.execute(queryString, operationName, variables /* this is our custom context = variables! */, variables);
        if (!execResult.getErrors().isEmpty()) {
            logger.error("============ GraphQL Errors: ============");
            for (final GraphQLError error: execResult.getErrors()) {
                logger.error(String.format("message [%s] errorType [%s] locations [%s]", error.getMessage(), error.getErrorType(), error.getLocations()));
            }
        }
        logger.error("============ GraphQL Data: ============");
        final Map<String, Object> data = (Map<String, Object>) execResult.getData(); // graphQL.execute("{hello}").getData();
        logger.error(Prettyfier.prettyString(data, 0, true));
        
        return restUtil.graphQLResultRepresentation(data, execResult.getErrors());
    }
}
