package ua.com.fielden.platform.mcp;

import com.fasterxml.jackson.databind.json.JsonMapper;
import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.json.jackson2.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpStatelessServerFeatures.SyncResourceSpecification;
import io.modelcontextprotocol.server.McpStatelessSyncServer;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.mcp.web.McpResource;
import ua.com.fielden.platform.mcp.web.McpResourceFactory;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.web_api.IWebApi;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static ua.com.fielden.platform.utils.MiscUtilities.readResource;

/// MCP server that exposes a TG system to 3rd-party AI systems.
///
/// Provides:
/// - `tg://query-guide` resource — GraphQL query syntax reference.
/// - `execute_query` tool — executes GraphQL queries against the TG system.
///
/// Uses stateless HTTP transport.
///
/// Requires a running TG instance with a GraphQL endpoint.
///
public class TgMcpServer {

    public static final String QUERY_GUIDE_RESOURCE_URI = "tg://query-guide";

    private static final String SERVER_NAME = "tg-mcp-server";
    private static final String SERVER_VERSION = "0.1.0";

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
    protected TgMcpServer(final IWebApi webApi, final McpResourceFactory mcpResourceFactory, final IUserProvider userProvider) {
        this.webApi = webApi;
        this.userProvider = userProvider;

        // After the builder is finished, the server will start.
        server = McpServer.sync(mcpResourceFactory)
                .serverInfo(SERVER_NAME, SERVER_VERSION)
                .instructions("""
                              TG MCP Server provides access to a TG system's data via GraphQL.
                              Read the tg://query-guide resource first to understand the query syntax, then use the execute_query tool to run queries.
                              """)
                .capabilities(McpSchema.ServerCapabilities.builder()
                                      .tools(false)
                                      .resources(false, false)
                                      .build())
                .resources(queryGuideResource())
                .toolCall(executeQueryTool(), this::handleExecuteQuery)
                .build();
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
    /// Only a malformed tool input and an exception yield an unsuccessful result (`isError` is `true`), which carries a diagnostic message instead of a GraphQL response document.
    ///
    /// A successful result is returned as structured content, and unstructured content is deliberately omitted.
    /// The MCP SDK derives a JSON text block from structured content whenever unstructured content is absent, which guarantees that both representations agree.
    ///
    private CallToolResult handleExecuteQuery(final McpTransportContext context, final CallToolRequest request) {
        final var arguments = request.arguments();
        if (!(arguments.get("query") instanceof String query)) {
            return malformedInput("Argument [query] is required and must be a string.");
        }
        if (!(arguments.getOrDefault("variables", Map.of()) instanceof Map<?, ?> variables)) {
            return malformedInput("Argument [variables] must be an object.");
        }

        setCurrentUser(context);

        try {
            final var result = webApi.execute(Map.of("query", query, "variables", variables));
            // Unstructured content is deliberately omitted — it is derived from structured content by the MCP SDK.
            return CallToolResult.builder()
                    .structuredContent(result)
                    .build();
        } catch (final Exception e) {
            LOGGER.error(() -> """
                               Error executing query:
                               %s
                               Variables: %s""".formatted(query, variables),
                         e);
            return CallToolResult.builder()
                    .addTextContent("Error executing query: " + e.getMessage())
                    .isError(true)
                    .build();
        }
    }

    /// Creates an unsuccessful result that reports a malformed tool input.
    ///
    private static CallToolResult malformedInput(final String message) {
        return CallToolResult.builder()
                .addTextContent(message)
                .isError(true)
                .build();
    }

}
