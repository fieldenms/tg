package ua.com.fielden.platform.mcp;

import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpStatelessServerFeatures.SyncResourceSpecification;
import io.modelcontextprotocol.server.McpStatelessSyncServer;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.domaintree.ICalculatedProperty;
import ua.com.fielden.platform.domaintree.IDomainManager;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.exceptions.InvalidStateException;
import ua.com.fielden.platform.entity.query.QueryProcessingModel;
import ua.com.fielden.platform.eql.meta.query.AbstractQuerySourceItem;
import ua.com.fielden.platform.eql.retrieval.EqlQueryTransformer;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.mcp.web.McpResource;
import ua.com.fielden.platform.mcp.web.McpResourceFactory;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.security.IAuthorisationModel;
import ua.com.fielden.platform.security.provider.ISecurityTokenProvider;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.types.either.Left;
import ua.com.fielden.platform.types.either.Right;
import ua.com.fielden.platform.utils.CollectionUtil;
import ua.com.fielden.platform.web_api.IWebApi;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiFunction;

import static java.lang.String.format;
import static java.util.function.Predicate.not;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.eql.retrieval.EqlQueryTransformer.Stage.stage2;
import static ua.com.fielden.platform.error.Result.failuref;
import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.security.tokens.TokenUtils.authorisePropertyReading;
import static ua.com.fielden.platform.types.try_wrapper.TryWrapper.Try;
import static ua.com.fielden.platform.utils.MiscUtilities.readResource;

/// MCP server that exposes a TG system to 3rd-party AI systems.
///
/// Provides:
/// - `tg://query-guide` resource — GraphQL query syntax reference.
/// - `execute_query` tool — executes GraphQL queries against the TG system.
/// - [#TOOL_ADD_CALCULATED_PROPERTY] — adds a calculated property to an entity type.
/// - [#TOOL_REMOVE_CALCULATED_PROPERTY] — removes a calculated property from an entity type.
///
/// Uses stateless HTTP transport.
///
/// Requires a running TG instance with a GraphQL endpoint.
///
/// ### Error handling
///
/// There are 2 kinds of errors:
///
/// 1. Tool execution errors, represented as [CallToolResult] with [CallToolResult#isError()] set to true.
///    Errors that can be corrected by clients, or that should be visible to clients, should be represented in this way.
///    For example, invalid tool call arguments, violated business rules, and other application-level errors.
///    In practice this means that tool handlers should catch exceptions and encode them in [CallToolResult].
///
/// 2. Protocol errors, represented as [McpError], which itself represents a JSON-RPC error.
///    Internal server errors that should not necessarily be visible to clients should be represented in this way.
///    For example, network failures, malformed JSON-RPC requests, and other system-level errors.
///    In practice this means that an exception thrown by a tool handler is processed by the MCP SDK and returned to the
///    client as a generic JSON-RPC error.
///    Furthermore, MCP clients are not required to expose such errors to LLMs, unlike tool execution errors.
///
/// For more details, see [Error Handling](https://modelcontextprotocol.io/specification/2025-06-18/server/tools#error-handling).
///
public class TgMcpServer {

    public static final String QUERY_GUIDE_RESOURCE_URI = "tg://query-guide";

    private static final String SERVER_NAME = "tg-mcp-server";
    private static final String SERVER_VERSION = "0.1.0";

    public static final String
            TOOL_ADD_CALCULATED_PROPERTY = "add_calculated_property",
            TOOL_REMOVE_CALCULATED_PROPERTY = "remove_calculated_property";

    /// Path to the Java resource containing the contents of MCP resource [#QUERY_GUIDE_RESOURCE_URI].
    /// This Java resource is resolved via [Class#getResource(String)], which means it should be present on the classpath.
    private static final String GRAPHQL_QUERY_GUIDE_RESOURCE_PATH = "/mcp/graphql-query-guide.md";

    /// Path to the Java resource containing the output schema of tool `execute_query`.
    /// This Java resource is resolved via [Class#getResource(String)], which means it should be present on the classpath.
    ///
    /// That schema describes a GraphQL response document, as defined by the GraphQL specification.
    /// Its property `data` is deliberately open: the shape of `data` is determined by the selection set of an executed query,
    /// which is composed by a requestor.
    /// The shape of the enclosing document, on the other hand, is invariant, which enables a requestor to parse any
    /// successful result with a standard GraphQL client.
    ///
    private static final String EXECUTE_QUERY_OUTPUT_SCHEMA_RESOURCE_PATH = "/mcp/execute-query-output-schema.json";

    private static final Logger LOGGER = LogManager.getLogger();

    private final IWebApi webApi;
    private final IUserProvider userProvider;
    /// Retained reference to the running server for observability.
    private final McpStatelessSyncServer server;

    @Inject
    protected TgMcpServer(
            final IWebApi webApi,
            final McpResourceFactory mcpResourceFactory,
            final IUserProvider userProvider,
            final IApplicationDomainProvider appDomainProvider,
            final IDomainManager domainManager,
            final EqlQueryTransformer eqlTransformer,
            final IAuthorisationModel authorisationModel,
            final ISecurityTokenProvider securityTokenProvider,
            final IDomainMetadata domainMetadata)
    {
        this.webApi = webApi;
        this.userProvider = userProvider;

        server = McpServer.sync(mcpResourceFactory)
                .serverInfo(SERVER_NAME, SERVER_VERSION)
                .instructions("""
                              TG MCP Server provides access to a TG system's data via GraphQL.
                              Read the tg://query-guide resource first to understand the query syntax, then use provided tools.
                              """)
                .capabilities(McpSchema.ServerCapabilities.builder()
                                      .tools(false)
                                      .resources(false, false)
                                      .build())
                // Prefer our own validation with our own messages.
                .validateToolInputs(false)
                .resources(queryGuideResource())
                .toolCall(executeQueryTool(), this::handleExecuteQuery)
                .toolCall(addCalculatedPropertyTool(), new AddCalculatedProperty(appDomainProvider, domainManager, eqlTransformer, userProvider, authorisationModel, securityTokenProvider, domainMetadata))
                .toolCall(removeCalculatedPropertyTool(), new RemoveCalculatedProperty(appDomainProvider, domainManager))
                .build();
        // After the builder is finished, the server will start.
    }

    // -- Resources --

    private SyncResourceSpecification queryGuideResource() {
        return new SyncResourceSpecification(
                McpSchema.Resource.builder(QUERY_GUIDE_RESOURCE_URI, "GraphQL Query Guide")
                        .description("""
                                     Reference document describing the GraphQL capabilities supported by TG.""")
                        .mimeType("text/markdown")
                        .build(),
                (_, request) -> {
                    final var contents = List.<McpSchema.ResourceContents>of(
                            TextResourceContents.builder(request.uri(), loadQueryGuide())
                                    .mimeType("text/markdown")
                                    .build()
                    );
                    return ReadResourceResult.builder(contents).build();
                });
    }

    private static String loadQueryGuide() {
        try (final var is = TgMcpServer.class.getResourceAsStream(GRAPHQL_QUERY_GUIDE_RESOURCE_PATH)) {
            if (is != null) {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (final IOException e) {
            LOGGER.warn("GraphQL query guide not available.", e);
        }
        return "GraphQL query guide not available.";
    }

    // -- Tools --

    private static McpSchema.Tool executeQueryTool() {
        final var inputSchema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "query", Map.of("type", "string",
                                        "description", "The GraphQL query string"),
                        "variables", Map.of("type", "object",
                                            "description", "Optional GraphQL variables")),
                "required", List.of("query"));
        return McpSchema.Tool.builder("execute_query", inputSchema)
                .description("""
                             Executes a GraphQL query against the TG system and returns a GraphQL response document.
                             Use for both preliminary lookups (e.g., resolving reference entity keys) and final data queries.
                             Batch multiple lookups into a single query by requesting multiple root fields.

                             Values are returned in "data", failures in "errors", and both can be present at once — a query can produce partial data.
                             A query that could not be parsed, validated or executed is reported in "errors", and is a successful tool call.
                             A tool error means only that this request was malformed or that the system failed to process it.""")
                .outputSchema(McpJsonDefaults.getMapper(),
                              readResource(TgMcpServer.class, EXECUTE_QUERY_OUTPUT_SCHEMA_RESOURCE_PATH, StandardCharsets.UTF_8))
                .build();
    }

    private static McpSchema.Tool addCalculatedPropertyTool() {
        final var inputSchema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "type", Map.of("type", "string",
                                       "description", "Entity type to add the property to."),
                        "name", Map.of("type", "string",
                                       "description", "Property name."),
                        "expression", Map.of("type", "string",
                                             "description", "Expression defining the calculated property.")),
                "required", List.of("type", "name", "expression"));
        return McpSchema.Tool.builder(TOOL_ADD_CALCULATED_PROPERTY, inputSchema)
                .description("""
                             Adds a calculated property.
                             
                             **Authorisation**
                             An expression may access only those properties that the client is authorised to access.
                             If an unauthorised access is detected, the calculated property will be rejected.
                             """)
                .build();
    }

    private static McpSchema.Tool removeCalculatedPropertyTool() {
        final var inputSchema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "type", Map.of("type", "string",
                                       "description", "Entity type to remove the property from."),
                        "name", Map.of("type", "string",
                                       "description", "Property name.")),
                "required", List.of("type", "name"));
        return McpSchema.Tool.builder(TOOL_REMOVE_CALCULATED_PROPERTY, inputSchema)
                .description("""
                             Removes the specified calculated property.
                             """)
                .build();
    }

    private @Nullable User getUser(final McpTransportContext context) {
        return (User) context.get(McpResource.USER_KEY);
    }

    /// Sets the current user on this thread from [McpTransportContext].
    ///
    private void setCurrentUser(final McpTransportContext context) {
        final var user = getUser(context);
        if (user != null) {
            userProvider.setUser(user);
        }
    }

    /// Executes a GraphQL query, as per tool `execute_query`.
    ///
    /// A successful result is always a GraphQL response document, conforming to the schema at [#EXECUTE_QUERY_OUTPUT_SCHEMA_RESOURCE_PATH].
    /// This includes a query that could not be parsed, validated or executed: such failures are reported in `errors`, as prescribed by the GraphQL specification.
    /// Tool execution errors yield an unsuccessful result (`isError` is `true`), which carries a diagnostic message instead of a GraphQL response document.
    ///
    /// A successful result is returned as structured content, and unstructured content is deliberately omitted.
    /// The MCP SDK derives a JSON text block from structured content whenever unstructured content is absent, which guarantees that both representations agree.
    ///
    private CallToolResult handleExecuteQuery(final McpTransportContext context, final CallToolRequest request) {
        final var arguments = request.arguments();
        if (!(arguments.get("query") instanceof String query)) {
            return errorResult("Argument [query] is required and must be a string.");
        }
        if (!(arguments.getOrDefault("variables", Map.of()) instanceof Map<?, ?> variables)) {
            return errorResult("Argument [variables] must be an object.");
        }

        setCurrentUser(context);

        final Map<String, Object> result;
        try {
            result = webApi.execute(Map.of("query", query, "variables", variables));
        } catch (final Exception e) {
            LOGGER.error(() -> """
                               Error executing query:
                               %s
                               Variables: %s""".formatted(query, variables),
                         e);
            return errorResult("Error executing query: " + e.getMessage());
        }
        // Unstructured content is deliberately omitted — it is derived from structured content by the MCP SDK.
        return CallToolResult.builder()
                .structuredContent(result)
                .build();
    }

    class AddCalculatedProperty implements BiFunction<McpTransportContext, CallToolRequest, CallToolResult> {

        private final IApplicationDomainProvider appDomainProvider;
        private final IDomainManager domainManager;
        private final EqlQueryTransformer eqlTransformer;
        private final IUserProvider userProvider;
        private final IAuthorisationModel authorisationModel;
        private final ISecurityTokenProvider securityTokenProvider;
        private final IDomainMetadata domainMetadata;

        AddCalculatedProperty(
                final IApplicationDomainProvider appDomainProvider,
                final IDomainManager domainManager,
                final EqlQueryTransformer eqlTransformer,
                final IUserProvider userProvider,
                final IAuthorisationModel authorisationModel,
                final ISecurityTokenProvider securityTokenProvider,
                final IDomainMetadata domainMetadata)
        {
            this.appDomainProvider = appDomainProvider;
            this.domainManager = domainManager;
            this.eqlTransformer = eqlTransformer;
            this.userProvider = userProvider;
            this.authorisationModel = authorisationModel;
            this.securityTokenProvider = securityTokenProvider;
            this.domainMetadata = domainMetadata;
        }

        @Override
        public CallToolResult apply(final McpTransportContext context, final CallToolRequest request) {
            final var arguments = request.arguments();
            if (!(arguments.get("type") instanceof String type)) {
                return errorResult("Argument [type] is required and must be a string.");
            }
            if (!(arguments.get("name") instanceof String name)) {
                return errorResult("Argument [name] is required and must be a string.");
            }
            if (!(arguments.get("expression") instanceof String expression)) {
                return errorResult("Argument [expression] is required and must be a string.");
            }

            setCurrentUser(context);

            final var user = getUser(context);
            if (user == null) {
                LOGGER.error("Current user is missing.");
                return errorResult("Current user is missing.");
            }
            final var domainTreeEnhancer = domainManager.domainTreeEnhancer(user);

            final var maybeEntityType = appDomainProvider.entityTypes().stream()
                    .filter(ty -> ty.getSimpleName().equals(type))
                    .findFirst();
            if (maybeEntityType.isEmpty()) {
                return invalidArgument("type", "No such type: %s.".formatted(type));
            }
            final var entityType = maybeEntityType.get();

            try {
                domainTreeEnhancer.addCalculatedProperty(
                        entityType,
                        "",
                        name,
                        expression,
                        "Title",
                        "Description",
                        ICalculatedProperty.CalculatedPropertyAttribute.NO_ATTR,
                        null,
                        null,
                        null);
                domainTreeEnhancer.apply();
            } catch (final Exception ex) {
                final var msg = """
                                Error adding calculated property [%s.%s] with expression:
                                %s
                                """.formatted(type, name, expression);
                LOGGER.error(() -> msg, ex);
                return errorResult(msg + "\n" + ex.getMessage());
            }

            final var managedType = (Class<? extends AbstractEntity<?>>) domainTreeEnhancer.getManagedType(entityType);

            // The domain has been enhanced, but the rest of the operation could still fail, which would require a rollback
            // (removal of the added calculated property).

            final Result validationResult;
            try {
                validationResult = validateExpression(managedType, name);
            } catch (final Throwable ex) {
                LOGGER.error(() -> format("Expression validation failed while adding calculated property [%s.%s].", entityType.getSimpleName(), name), ex);
                removeAddedProperty(domainTreeEnhancer, entityType, managedType, name).ifPresent(ex::addSuppressed);
                final var msg = """
                                Error adding calculated property [%s.%s] with expression:
                                %s
                                """.formatted(type, name, expression);
                return errorResult(msg + "\n" + ex.getMessage());
            }

            if (!validationResult.isSuccessful()) {
                removeAddedProperty(domainTreeEnhancer, entityType, managedType, name);
                return errorResult("""
                                   Error adding calculated property [%s.%s] with expression:
                                   %s
                                   Reason: %s
                                   """.formatted(type, name, expression, validationResult));
            }

            try {
                webApi.propertiesAdded(managedType, Set.of(name));
            } catch (final Throwable ex) {
                LOGGER.error(() -> format("Web API failed while adding calculated property [%s.%s].", entityType.getSimpleName(), name), ex);
                removeAddedProperty(domainTreeEnhancer, entityType, managedType, name).ifPresentOrElse(
                        ex::addSuppressed,
                        () -> LOGGER.info(() -> format("Rollback successful: removed calculated property [%s.%s] from the Web API.", entityType.getSimpleName(), name)));
                final var msg = """
                                Error adding calculated property [%s.%s] with expression:
                                %s
                                """.formatted(type, name, expression);
                return errorResult(msg + "\n" + ex.getMessage());
            }

            return CallToolResult.builder()
                    .addTextContent("Ok")
                    .build();
        }

        private Optional<Exception> removeAddedProperty(
                final IDomainTreeEnhancer domainTreeEnhancer,
                final Class<? extends AbstractEntity<?>> entityType,
                final Class<? extends AbstractEntity<?>> managedType,
                final CharSequence property)
        {
            return switch (Try(() -> {
                domainTreeEnhancer.findCalculatedProperty(entityType, property.toString()).ifPresent(_ -> {
                    domainTreeEnhancer.removeCalculatedProperty(entityType, property.toString());
                    domainTreeEnhancer.apply();
                });
                webApi.propertiesRemoved(managedType, Set.of(property));
                return true;
            })) {
                case Right _ -> Optional.empty();
                case Left (var cleanupEx) -> {
                    LOGGER.error(() -> format("Failed to remove calculated property [%s.%s] from the Web API. This failure will be suppressed.",
                                              entityType.getSimpleName(), property),
                                 cleanupEx);
                    yield Optional.of(cleanupEx);
                }
            };
        }

        private Result validateExpression(final Class<? extends AbstractEntity<?>> entityType, final CharSequence property) {
            final var expressionModel = domainMetadata.forProperty(entityType, property).asCalculated()
                    .orElseThrow(() -> new InvalidStateException(format("[%s.%s] must be calculated.", entityType.getTypeName(), property)))
                    .data().expressionModel();
            final var query = select(entityType).yield().expr(expressionModel).as("expr").modelAsAggregate();
            final var qpm = new QueryProcessingModel<>(query, null, null, Map.of(), true);
            // Stage 2 contains yet unexpanded calculated properties which may themselves reference other properties,
            // but it is not necessary to check them.
            // If such a calculated property is defined in the original domain, its authorisation rules will guard access.
            // If it is an added calculated property, it must have already been validated.
            final var resultQuery2 = eqlTransformer.transformToStage(qpm, userProvider.getUsername(), stage2);
            final var unauthorisedPaths = resultQuery2.collectProps().stream()
                    .map(this::findUnauthorised)
                    .filter(Objects::nonNull)
                    .map(path -> CollectionUtil.toString(path, it -> it.name, "."))
                    .sorted()
                    .toList();
            if (!unauthorisedPaths.isEmpty()) {
                return failuref("Unauthorised access to the following properties: [%s].", CollectionUtil.toString(unauthorisedPaths, ", "));
            }
            return successful();
        }

        private @Nullable List<AbstractQuerySourceItem<?>> findUnauthorised(final Prop2 prop) {
            try {
                Class<?> type = prop.source.sourceType();
                final var path = prop.getPath();
                for (int i = 0; i < path.size(); i++) {
                    final var item = path.get(i);
                    final var result = authorisePropertyReading(type, item.name, authorisationModel, securityTokenProvider);
                    if (result.filter(not(Result::isSuccessful)).isPresent()) {
                        return path.subList(0, i + 1);
                    }
                    type = domainMetadata.forProperty(type, item.name).type().javaType();
                }
                return null;
            } catch (final Exception ex) {
                LOGGER.warn(() -> format("Suppressed error while checking authorisation for [%s.%s]. Assuming unauthorised.",
                                         prop.source.sourceType().getTypeName(), prop.propPath),
                            ex);
                return prop.getPath();
            }
        }

    }

    class RemoveCalculatedProperty implements BiFunction<McpTransportContext, CallToolRequest, CallToolResult> {

        private final IApplicationDomainProvider appDomainProvider;
        private final IDomainManager domainManager;

        RemoveCalculatedProperty(
                final IApplicationDomainProvider appDomainProvider,
                final IDomainManager domainManager)
        {
            this.appDomainProvider = appDomainProvider;
            this.domainManager = domainManager;
        }

        @Override
        public CallToolResult apply(final McpTransportContext context, final CallToolRequest request) {
            final var arguments = request.arguments();
            if (!(arguments.get("type") instanceof String type)) {
                return errorResult("Argument [type] is required and must be a string.");
            }
            if (!(arguments.get("name") instanceof String name)) {
                return errorResult("Argument [name] is required and must be a string.");
            }

            setCurrentUser(context);

            final var user = getUser(context);
            if (user == null) {
                LOGGER.error("Current user is missing.");
                return errorResult("Current user is missing.");
            }
            final var domainTreeEnhancer = domainManager.domainTreeEnhancer(user);

            final var maybeEntityType = appDomainProvider.entityTypes().stream()
                    .filter(ty -> ty.getSimpleName().equals(type))
                    .findFirst();
            if (maybeEntityType.isEmpty()) {
                return invalidArgument("type", "No such type: %s.".formatted(type));
            }
            final var entityType = maybeEntityType.get();

            try {
                domainTreeEnhancer.removeCalculatedProperty(entityType, name);
                domainTreeEnhancer.apply();
                webApi.propertiesRemoved((Class<? extends AbstractEntity<?>>) domainTreeEnhancer.getManagedType(entityType), Set.of(name));
            } catch (final Exception e) {
                final var msg = "Error removing calculated property [%s.%s].".formatted(type, name);
                LOGGER.error(() -> msg, e);
                return errorResult(msg + " " + e.getMessage());
            }

            return CallToolResult.builder()
                    .addTextContent("Ok")
                    .build();
        }

    }

    /// Creates a tool result representing an error.
    ///
    private static CallToolResult errorResult(final String message) {
        return CallToolResult.builder()
                .addTextContent(message)
                .isError(true)
                .build();
    }

    /// Creates a tool result representing an invalid argument error.
    ///
    private static CallToolResult invalidArgument(final String argumentName, final String message) {
        return CallToolResult.builder()
                .addTextContent(format("Invalid argument [%s]. %s", argumentName, message))
                .isError(true)
                .build();
    }

}
