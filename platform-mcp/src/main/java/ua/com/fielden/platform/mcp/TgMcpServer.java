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
import ua.com.fielden.platform.mcp.web.McpResource;
import ua.com.fielden.platform.mcp.web.McpResourceFactory;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.web_api.GraphQLCommon;
import ua.com.fielden.platform.web_api.IWebApi;
import ua.com.fielden.platform.web_api.IWebApi.Enhancement;
import ua.com.fielden.platform.web_api.IWebApi.PropertyDefinition;
import ua.com.fielden.platform.web_api.exceptions.WebApiException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static ua.com.fielden.platform.utils.MiscUtilities.readResource;

/// MCP server that exposes a TG system to 3rd-party AI systems.
///
/// Provides:
/// - `tg://query-guide` resource — GraphQL query syntax reference.
/// - `execute_query` tool — executes GraphQL queries against the TG system.
/// - [#TOOL_ENHANCE_ENTITY_TYPE] tool — creates or changes an entity type that extends a domain type with calculated properties.
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

    public static final String TOOL_ENHANCE_ENTITY_TYPE = "enhance_entity_type";

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
    private final GraphQLCommon common;
    /// Retained reference to the running server for observability.
    private final McpStatelessSyncServer server;

    @Inject
    protected TgMcpServer(
            final IWebApi webApi,
            final McpResourceFactory mcpResourceFactory,
            final IUserProvider userProvider,
            final GraphQLCommon common)
    {
        this.webApi = webApi;
        this.userProvider = userProvider;
        this.common = common;

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
                .toolCall(enhanceEntityTypeTool(), this::handleEnhanceEntityType)
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

    private static McpSchema.Tool enhanceEntityTypeTool() {
        final var propertySchema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "name", Map.of("type", "string",
                                       "description", "Property name."),
                        "expression", Map.of("type", "string",
                                             "description", "Expression in TG's expression language over the properties of the entity type.")),
                "required", List.of("name", "expression"));
        final var inputSchema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "name", Map.of("type", "string",
                                       "description", "Name of the entity type to create or change."),
                        "baseType", Map.of("type", "string",
                                           "description", "Name of the queryable entity type to extend. Required to create an entity type, and must be omitted to change one."),
                        "addProperties", Map.of("type", "array",
                                                "items", propertySchema,
                                                "description", "Calculated properties to add, in order."),
                        "removeProperties", Map.of("type", "array",
                                                   "items", Map.of("type", "string"),
                                                   "description", "Names of previously added properties to remove.")),
                "required", List.of("name"));
        return McpSchema.Tool.builder(TOOL_ENHANCE_ENTITY_TYPE, inputSchema)
                .description("""
                             Creates an enhanced entity type that extends a queryable entity type with calculated properties, or changes such an entity type.
                             Use it where a request needs a value that no property holds, e.g., to compare two properties, or to filter, group or aggregate by a derived value.

                             If no entity type named `name` was created with this tool, one is created that extends `baseType` with `addProperties`.
                             Otherwise, `removeProperties` are removed from it and then `addProperties` are added to it; `baseType` must be omitted.
                             Names consist of Latin letters, digits and underscores, do not start with a digit, and are not Java keywords.
                             The name of a new entity type must not be that of an existing GraphQL type.

                             Each added property is defined by an expression in TG's expression language over the properties of the entity type, such as `2 * price` or `actualCost - estimatedCost`.
                             See section "Enhanced Entity Types" of resource %s.
                             An expression may refer to properties added before it, and only to properties that the current user may read.
                             Properties of the base type can be neither added nor removed.
                             To change the expression of a property, remove the property in one call and add it in another.

                             The enhanced entity type can be queried with tool `execute_query` through its root fields, which are included in the result of this tool.
                             The enhanced entity type has every property of its base type and the added ones, and `_entityType` describes it.

                             A call either takes full effect or none."""
                             .formatted(QUERY_GUIDE_RESOURCE_URI))
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

    /// Creates or changes an enhanced entity type, as per tool [#TOOL_ENHANCE_ENTITY_TYPE].
    ///
    /// A successful result is a text message that names the entity type and its root fields, followed by warnings, if any.
    ///
    private CallToolResult handleEnhanceEntityType(final McpTransportContext context, final CallToolRequest request) {
        final var arguments = request.arguments();
        if (!(arguments.get("name") instanceof String name)) {
            return errorResult("Argument [name] is required and must be a string.");
        }
        final var baseTypeArgument = arguments.get("baseType");
        if (baseTypeArgument != null && !(baseTypeArgument instanceof String)) {
            return errorResult("Argument [baseType] must be a string.");
        }
        final var addProperties = propertyDefinitions(arguments.getOrDefault("addProperties", List.of()));
        if (addProperties.isEmpty()) {
            return errorResult("Argument [addProperties] must be an array of objects, each with string properties [name] and [expression].");
        }
        if (!(arguments.getOrDefault("removeProperties", List.of()) instanceof List<?> removeProperties)
            || !removeProperties.stream().allMatch(String.class::isInstance))
        {
            return errorResult("Argument [removeProperties] must be an array of strings.");
        }
        final var baseType = (String) baseTypeArgument;

        setCurrentUser(context);

        final Enhancement enhancement;
        try {
            enhancement = webApi.enhanceEntityType(name, baseType, addProperties.get(), removeProperties.stream().map(String.class::cast).toList());
        } catch (final WebApiException ex) {
            return errorResult(ex.getMessage());
        } catch (final Exception ex) {
            LOGGER.error(() -> "Error enhancing entity type [%s].".formatted(name), ex);
            return errorResult("Error enhancing entity type [%s]: %s".formatted(name, ex.getMessage()));
        }

        final var entityType = enhancement.entityType();
        final var message = Stream.concat(
                Stream.of("%s entity type [%s], which extends [%s] with properties [%s]. Query it with root fields [%s] and [%s].".formatted(
                        baseType == null ? "Changed" : "Created",
                        entityType.name(),
                        entityType.baseType().getSimpleName(),
                        String.join(", ", entityType.properties().keySet()),
                        common.rootFieldName(entityType.webApiEntityType()),
                        common.rootFieldNameForAggEntity(entityType.webApiEntityType()))),
                enhancement.warnings().stream().map(warning -> "Warning: " + warning))
                .collect(joining("\n"));
        return CallToolResult.builder()
                .addTextContent(message)
                .build();
    }

    /// Converts the value of argument `addProperties` into property definitions.
    /// Returns an empty optional if the value is malformed.
    ///
    private static Optional<List<PropertyDefinition>> propertyDefinitions(final Object argument) {
        if (!(argument instanceof List<?> elements)) {
            return Optional.empty();
        }
        final var definitions = new ArrayList<PropertyDefinition>();
        for (final var element : elements) {
            if (!(element instanceof Map<?, ?> map && map.get("name") instanceof String name && map.get("expression") instanceof String expression)) {
                return Optional.empty();
            }
            definitions.add(new PropertyDefinition(name, expression));
        }
        return Optional.of(definitions);
    }

    /// Creates a tool result representing an error.
    ///
    private static CallToolResult errorResult(final String message) {
        return CallToolResult.builder()
                .addTextContent(message)
                .isError(true)
                .build();
    }

}
