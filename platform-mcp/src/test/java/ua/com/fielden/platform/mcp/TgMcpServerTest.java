package ua.com.fielden.platform.mcp;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ua.com.fielden.platform.mcp.test_config.AbstractTgMcpServerTestCase;
import ua.com.fielden.platform.mcp.test_config.McpTestWebApp;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.web.test.TestWebApplication;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static ua.com.fielden.platform.mcp.TgMcpServer.QUERY_GUIDE_RESOURCE_URI;
import static ua.com.fielden.platform.test_utils.TestUtils.assertInstanceOf;

public class TgMcpServerTest extends AbstractTgMcpServerTestCase {

    private static final String URI = "http://localhost:%s".formatted(TestWebApplication.PORT);
    private static final String PREFIX = "/test";

    private final McpTestWebApp webApp = getInstance(McpTestWebApp.class);
    private final McpSyncClient mcpClient = McpClient.sync(
            HttpClientStreamableHttpTransport.builder(URI)
                    .endpoint(PREFIX + McpTestWebApp.PATH_MCP)
                    .build())
            .requestTimeout(Duration.ofHours(999))
            .build();

    @Before
    public void startUp() {
        TestWebApplication.attachWebApplication(PREFIX, webApp);
        setUser(USER_MCP_TEST);
    }

    @After
    public void tearDown() {
        TestWebApplication.detachWebApplication(webApp);
    }

    @Test
    public void resource_query_guide() {
        final var queryGuideResource = mcpClient.readResource(new McpSchema.ReadResourceRequest(QUERY_GUIDE_RESOURCE_URI));
        assertFalse(queryGuideResource.contents().isEmpty());
        final var content = assertInstanceOf(McpSchema.TextResourceContents.class, queryGuideResource.contents().getFirst());
        assertEquals("text/markdown", content.mimeType());
        assertEquals(QUERY_GUIDE_RESOURCE_URI, content.uri());
        assertThat(content.text()).contains("This document describes how to query data from a TG system using GraphQL");
    }

    @Test
    public void tool_execute_query() {
        final var result = executeQuery(
                """
                {
                  tgVehicle {
                    price
                    active
                    model {
                      make {
                        key
                      }
                      makeModelsCount
                    }
                  }
                }
                """);
        assertFalse(result.isError());
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().getFirst()).isInstanceOf(McpSchema.TextContent.class);
        assertThat(((McpSchema.TextContent) result.content().getFirst()).text()).isNotEmpty();
    }

    @Test
    public void non_existing_resource() {
        assertThatThrownBy(() -> mcpClient.readResource(new McpSchema.ReadResourceRequest("tg://abcd")))
                .isInstanceOf(McpError.class);
    }

    @Test
    public void non_existing_tool() {
        assertThatThrownBy(() -> mcpClient.callTool(new McpSchema.CallToolRequest("magic", Map.of())))
                .isInstanceOf(McpError.class);
    }

    @Override
    public boolean saveDataPopulationScriptToFile() {
        return false;
    }

    @Override
    public boolean useSavedDataPopulationScript() {
        return false;
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();

        if (useSavedDataPopulationScript()) {
            return;
        }

        final var merc = save(new_(TgVehicleMake.class, "MERC", "Mercedes"));
        final var audi = save(new_(TgVehicleMake.class, "AUDI", "Audi"));

        final var merc316 = save(new_(TgVehicleModel.class, "316", "316").setMake(merc));
        final var audi005 = save(new_(TgVehicleModel.class, "005", "005").setMake(audi));

        final var car1 = save(new_(TgVehicle.class, "CAR1", "CAR1 DESC")
                                      .setModel(audi005)
                                      .setPrice(new Money("500"))
                                      .setActive(true));
        final var car2 = save(new_(TgVehicle.class, "CAR2", "CAR2 DESC")
                                      .setModel(merc316)
                                      .setPrice(new Money("450"))
                                      .setActive(true));
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Utilities
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    private McpSchema.CallToolResult executeQuery(final String query, final Map<String, Object> variables) {
        return mcpClient.callTool(new McpSchema.CallToolRequest("execute_query", Map.of("query", query, "variables", variables)));
    }

    private McpSchema.CallToolResult executeQuery(final String query) {
        return executeQuery(query, Map.of());
    }

    private void setUser(final String username) {
        final IUser coUser = co(User.class);
        getInstance(IUserProvider.class).setUsername(username, coUser);
    }

}