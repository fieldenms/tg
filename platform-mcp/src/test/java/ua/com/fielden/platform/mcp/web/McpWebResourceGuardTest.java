package ua.com.fielden.platform.mcp.web;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.*;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import ua.com.fielden.platform.mcp.test_config.AbstractTgMcpServerTestCase;
import ua.com.fielden.platform.mcp.test_config.McpTestWebApp;
import ua.com.fielden.platform.mcp.test_config.TestMcpAuthenticationModel;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.web.test.TestWebApplication;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static ua.com.fielden.platform.mcp.test_config.TgMcpServerTestCaseConfiguration.PATH_MCP;

/// Tests for [McpWebResourceGuard], which authenticates requests to the MCP web resource and establishes the authenticated user for a request.
///
public class McpWebResourceGuardTest extends AbstractTgMcpServerTestCase {

    private static final int PORT = 9043;
    private static final String BASE_URI = "http://localhost:%s".formatted(PORT);
    private static final String PREFIX = "/test";
    private static final String FULL_URI = BASE_URI + PREFIX + PATH_MCP;

    private static final TestWebApplication webApplication = new TestWebApplication();

    @BeforeClass
    public static void beforeClass() {
        webApplication.start(PORT);
    }

    @AfterClass
    public static void afterClass() {
        webApplication.stop();
    }

    private final McpTestWebApp webApp = getInstance(McpTestWebApp.class);
    private final TestMcpAuthenticationModel authModel = (TestMcpAuthenticationModel) getInstance(IMcpAuthenticationModel.class);
    private final Client client = new Client(new Context(), Protocol.HTTP);

    @Before
    public void startUp() {
        webApplication.attachWebApplication(PREFIX, webApp);
    }

    @After
    public void tearDown() {
        webApplication.detachWebApplication(webApp);
    }

    /// Once the restriction is lifted, the same request passes the guard and reaches the MCP resource, which rejects it
    /// because it is empty, which means the request got past the guard.
    ///
    @Test
    public void a_request_is_denied_access_while_access_is_restricted() {
        final var restrictedResponse = authModel.withRestricted(() -> client.handle(new Request(Method.POST, FULL_URI)));
        assertThat(restrictedResponse.getStatus()).isEqualTo(Status.CLIENT_ERROR_FORBIDDEN);

        final var response = client.handle(new Request(Method.POST, FULL_URI));
        assertThat(response.getStatus()).isEqualTo(Status.CLIENT_ERROR_BAD_REQUEST);
    }

    /// A request authenticated by the authentication model passes the guard, which then assigns the authenticated user.
    /// Executing a tool establishes both: the request reaches the MCP resource, and it is processed under a user that is authorised.
    /// The current user is cleared beforehand, so that only the user assigned by the guard can authorise the request.
    ///
    @Test
    public void a_request_authenticated_by_the_authentication_model_is_processed_as_the_authenticated_user() {
        getInstance(IUserProvider.class).clearUser();
        try (final var mcpClient = McpClient.sync(
                        HttpClientStreamableHttpTransport.builder(BASE_URI)
                                .endpoint(PREFIX + PATH_MCP)
                                .build())
                .requestTimeout(Duration.ofMinutes(1))
                .build())
        {
            final var result = mcpClient.callTool(McpSchema.CallToolRequest.builder("execute_query")
                                                          .arguments(Map.of("query", "{ tgVehicle { key } }"))
                                                          .build());
            assertThat(result.isError()).isFalse();
        }
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
