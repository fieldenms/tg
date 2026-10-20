package ua.com.fielden.platform.mcp;

import com.fasterxml.jackson.databind.json.JsonMapper;
import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.json.jackson2.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpStatelessServerFeatures.SyncResourceSpecification;
import io.modelcontextprotocol.server.McpStatelessSyncServer;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.domaintree.ICalculatedProperty;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.mcp.web.McpResource;
import ua.com.fielden.platform.mcp.web.McpResourceFactory;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.web_api.IWebApi;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import static java.lang.String.format;
import static ua.com.fielden.platform.utils.MiscUtilities.readResource;
import static ua.com.fielden.platform.web_api.IWebApi.WEB_API_DOMAIN_TREE_ENHANCER;

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
            final @Named(WEB_API_DOMAIN_TREE_ENHANCER) IDomainTreeEnhancer domainTreeEnhancer)
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
                .resources(queryGuideResource())
                .toolCall(executeQueryTool(), this::handleExecuteQuery)
                .toolCall(addCalculatedPropertyTool(), new AddCalculatedProperty(appDomainProvider, domainTreeEnhancer))
                .toolCall(removeCalculatedPropertyTool(), new RemoveCalculatedProperty(appDomainProvider, domainTreeEnhancer))
                .build();
        // After the builder is finished, the server will start.
    }

    // -- Resources --

    private SyncResourceSpecification queryGuideResource() {
        return new SyncResourceSpecification(
                McpSchema.Resource.builder()
                        .uri(QUERY_GUIDE_RESOURCE_URI)
                        .name("GraphQL Query Guide")
                        .description("""
                                     Reference document describing the GraphQL capabilities supported by TG.""")
                        .mimeType("text/markdown")
                        .build(),
                (exchange, request) -> new ReadResourceResult(List.of(new TextResourceContents(request.uri(), "text/markdown", loadQueryGuide()))));
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
        return McpSchema.Tool.builder()
                .name("execute_query")
                .description("""
                             Executes a GraphQL query against the TG system and returns a GraphQL response document.
                             Use for both preliminary lookups (e.g., resolving reference entity keys) and final data queries.
                             Batch multiple lookups into a single query by requesting multiple root fields.

                             Values are returned in "data", failures in "errors", and both can be present at once — a query can produce partial data.
                             A query that could not be parsed, validated or executed is reported in "errors", and is a successful tool call.
                             A tool error means only that this request was malformed or that the system failed to process it.""")
                .inputSchema(new McpSchema.JsonSchema(
                        "object",
                        Map.of("query", Map.of("type", "string",
                                               "description", "The GraphQL query string"),
                               "variables", Map.of("type", "object",
                                                   "description", "Optional GraphQL variables")),
                        List.of("query"),
                        null, null, null))
                .outputSchema(new JacksonMcpJsonMapper(new JsonMapper()),
                              readResource(TgMcpServer.class, EXECUTE_QUERY_OUTPUT_SCHEMA_RESOURCE_PATH, StandardCharsets.UTF_8))
                .build();
    }

    private static McpSchema.Tool addCalculatedPropertyTool() {
        return McpSchema.Tool.builder()
                .name(TOOL_ADD_CALCULATED_PROPERTY)
                .description("""
                             Adds a calculated property.
                             """)
                .inputSchema(new McpSchema.JsonSchema(
                        "object",
                        Map.of("type", Map.of("type", "string",
                                              "description", "Entity type to add the property to."),
                               "name", Map.of("type", "string",
                                              "description", "Property name."),
                               "expression", Map.of("type", "string",
                                                    "description", "Expression defining the calculated property.")),
                        List.of("type", "name", "expression"),
                        null, null, null))
                .build();
    }

    private static McpSchema.Tool removeCalculatedPropertyTool() {
        return McpSchema.Tool.builder()
                .name(TOOL_REMOVE_CALCULATED_PROPERTY)
                .description("""
                             Removes the specified calculated property.
                             """)
                .inputSchema(new McpSchema.JsonSchema(
                        "object",
                        Map.of("type", Map.of("type", "string",
                                              "description", "Entity type to remove the property from."),
                               "name", Map.of("type", "string", "description", "Property name.")),
                        List.of("type", "name"),
                        null, null, null))
                .build();
    }

    /// Sets the current user on this thread from [McpTransportContext].
    ///
    private void setCurrentUser(final McpTransportContext context) {
        final var user = (User) context.get(McpResource.USER_KEY);
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
        private final IDomainTreeEnhancer domainTreeEnhancer;

        AddCalculatedProperty(
                final IApplicationDomainProvider appDomainProvider,
                final IDomainTreeEnhancer domainTreeEnhancer)
        {
            this.appDomainProvider = appDomainProvider;
            this.domainTreeEnhancer = domainTreeEnhancer;
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
                // Managed type must be accessed only after the calculated property is added.
                final var managedType = (Class<? extends AbstractEntity<?>>) domainTreeEnhancer.getManagedType(entityType);
                webApi.propertiesAdded(managedType, Set.of(name));
            } catch (final Exception e) {
                try {
                    domainTreeEnhancer.findCalculatedProperty(entityType, name)
                            .ifPresent(_ -> domainTreeEnhancer.removeCalculatedProperty(entityType, name));
                    final var managedType = (Class<? extends AbstractEntity<?>>) domainTreeEnhancer.getManagedType(entityType);
                    webApi.propertiesRemoved(managedType, Set.of(name));
                } catch (final Exception cleanupEx) {
                    LOGGER.error(() -> format("Failed to undo the effect of adding calculated property [%s.%s]. This failure will be suppressed.",
                                              entityType.getSimpleName(), name),
                                 cleanupEx);
                    e.addSuppressed(cleanupEx);
                }

                final var msg = """
                                Error adding calculated property [%s.%s] with expression:
                                %s
                                """.formatted(type, name, expression);
                LOGGER.error(() -> msg, e);
                return errorResult(msg + "\n" + e.getMessage());
            }

            return CallToolResult.builder()
                    .addTextContent("Ok")
                    .build();
        }

    }

    class RemoveCalculatedProperty implements BiFunction<McpTransportContext, CallToolRequest, CallToolResult> {

        private final IApplicationDomainProvider appDomainProvider;
        private final IDomainTreeEnhancer domainTreeEnhancer;

        RemoveCalculatedProperty(
                final IApplicationDomainProvider appDomainProvider,
                final IDomainTreeEnhancer domainTreeEnhancer)
        {
            this.appDomainProvider = appDomainProvider;
            this.domainTreeEnhancer = domainTreeEnhancer;
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
