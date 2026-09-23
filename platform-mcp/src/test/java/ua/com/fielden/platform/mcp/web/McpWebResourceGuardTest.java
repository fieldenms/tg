package ua.com.fielden.platform.mcp.web;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import ua.com.fielden.platform.mcp.test_config.AbstractTgMcpServerTestCase;
import ua.com.fielden.platform.mcp.test_config.McpTestWebApp;
import ua.com.fielden.platform.web.test.TestWebApplication;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static ua.com.fielden.platform.mcp.McpConfig.WEB_API_KEY_MCP_HTTP_HEADER;
import static ua.com.fielden.platform.mcp.test_config.TgMcpServerTestCaseConfiguration.PATH_MCP;
import static ua.com.fielden.platform.mcp.test_config.TgMcpServerTestCaseConfiguration.TEST_WEB_API_KEY_MCP;

/// Tests for [McpWebResourceGuard], which guards the MCP web resource with an API key and establishes the MCP user for a request.
///
public class McpWebResourceGuardTest extends AbstractTgMcpServerTestCase {

    private static final String BASE_URI = "http://localhost:%s".formatted(TestWebApplication.PORT);
    private static final String PREFIX = "/test";
    private static final String FULL_URI = BASE_URI + PREFIX + PATH_MCP;

    private final McpTestWebApp webApp = getInstance(McpTestWebApp.class);
    private final Client client = new Client(new Context(), Protocol.HTTP);

    @Before
    public void startUp() {
        TestWebApplication.attachWebApplication(PREFIX, webApp);
    }

    @After
    public void tearDown() {
        TestWebApplication.detachWebApplication(webApp);
    }

    @Test
    public void a_request_without_an_api_key_is_forbidden() {
        final var request = new Request(Method.POST, FULL_URI);
        final var response = client.handle(request);
        assertThat(response.getStatus()).isEqualTo(Status.CLIENT_ERROR_FORBIDDEN);
    }

    @Test
    public void a_request_with_an_invalid_api_key_is_forbidden() throws Exception {
        final var request = new Request(Method.POST, FULL_URI);
        request.getHeaders().set(WEB_API_KEY_MCP_HTTP_HEADER, TEST_WEB_API_KEY_MCP + "-invalid");
        final var response = client.handle(request);
        assertThat(response.getStatus()).isEqualTo(Status.CLIENT_ERROR_FORBIDDEN);
    }

    /// A valid API key passes the guard, which then assigns the MCP user.
    /// Executing a tool establishes both: the request reaches the MCP resource, and it is processed under a user that is authorised.
    ///
    @Test
    public void a_request_with_a_valid_api_key_is_processed_as_the_mcp_user() {
        final var mcpClient = McpClient.sync(
                        HttpClientStreamableHttpTransport.builder(BASE_URI)
                                .endpoint(PREFIX + PATH_MCP)
                                .customizeRequest(request -> request.header(WEB_API_KEY_MCP_HTTP_HEADER, TEST_WEB_API_KEY_MCP))
                                .build())
                .requestTimeout(Duration.ofMinutes(1))
                .build();

        final var result = mcpClient.callTool(new McpSchema.CallToolRequest("execute_query", Map.of("query", "{ tgVehicle { key } }")));
        assertThat(result.isError()).isFalse();
    }

    @Override
    public boolean saveDataPopulationScriptToFile() {
        return false;
    }

    @Override
    public boolean useSavedDataPopulationScript() {
        return false;
    }

}
