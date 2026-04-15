package ua.com.fielden.platform.mcp;

import com.fasterxml.jackson.databind.json.JsonMapper;
import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.json.jackson2.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpStatelessServerFeatures.SyncResourceSpecification;
import io.modelcontextprotocol.server.McpStatelessSyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.mcp.web.McpResourceFactory;
import ua.com.fielden.platform.web_api.IWebApi;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

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

    private static final Logger LOGGER = LogManager.getLogger();

    private final IWebApi webApi;
    /// Retained reference to the running server for observability.
    private final McpStatelessSyncServer server;

    @Inject
    protected TgMcpServer(final IWebApi webApi, final McpResourceFactory mcpResourceFactory) {
        this.webApi = webApi;

        final var transport = new StdioServerTransportProvider(new JacksonMcpJsonMapper(new JsonMapper()));

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
                             Executes a GraphQL query against the TG system and returns the result as JSON.
                             Use for both preliminary lookups (e.g., resolving reference entity keys) and final data queries.
                             Batch multiple lookups into a single query by requesting multiple root fields.""")
                .inputSchema(new McpSchema.JsonSchema(
                        "object",
                        Map.of("query", Map.of("type", "string",
                                               "description", "The GraphQL query string"),
                               "variables", Map.of("type", "object",
                                                   "description", "Optional GraphQL variables")),
                        List.of("query"),
                        null, null, null))
                .build();
    }

    @SuppressWarnings("unchecked")
    private CallToolResult handleExecuteQuery(final McpTransportContext context, final CallToolRequest request) {
        final var query = (String) request.arguments().get("query");
        if (query == null) {
            return CallToolResult.builder()
                    .addTextContent("Missing required argument [query].")
                    .isError(true)
                    .build();
        }
        final var variables = (Map<String, Object>) request.arguments().getOrDefault("variables", Map.of());

        try {
            final var result = webApi.execute(Map.of("query", query, "variables", variables));
            return CallToolResult.builder()
                    // TODO Structured content
                    .addTextContent(result.toString())
                    .build();
        } catch (final Exception e) {
            LOGGER.error("""
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

}
