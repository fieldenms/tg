package ua.com.fielden.platform.mcp;

import com.fasterxml.jackson.databind.json.JsonMapper;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.junit.*;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.domaintree.impl.CalculatedProperty;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.mcp.test_config.AbstractTgMcpServerTestCase;
import ua.com.fielden.platform.mcp.test_config.McpTestWebApp;
import ua.com.fielden.platform.mcp.test_config.TgMcpServerTestCaseConfiguration;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.web.test.TestWebApplication;
import ua.com.fielden.platform.web_api.IWebApi;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.mcp.McpConfig.WEB_API_KEY_MCP_HTTP_HEADER;
import static ua.com.fielden.platform.mcp.TgMcpServer.*;
import static ua.com.fielden.platform.test_utils.TestUtils.assertInstanceOf;
import static ua.com.fielden.platform.web_api.IWebApi.WEB_API_DOMAIN_TREE_ENHANCER;

public class TgMcpServerTest extends AbstractTgMcpServerTestCase {

    private static final int PORT = 9045;
    private static final String URI = "http://localhost:%s".formatted(PORT);
    private static final String PREFIX = "/test";

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
    private final McpSyncClient mcpClient = McpClient.sync(
            HttpClientStreamableHttpTransport.builder(URI)
                    .endpoint(PREFIX + TgMcpServerTestCaseConfiguration.PATH_MCP)
                    .customizeRequest(request -> request.header(WEB_API_KEY_MCP_HTTP_HEADER, TgMcpServerTestCaseConfiguration.TEST_WEB_API_KEY_MCP))
                    .build())
            .requestTimeout(Duration.ofHours(999))
            .build();

    private final IDomainMetadata domainMetadata = getInstance(IDomainMetadata.class);
    private final IWebApi webApi = getInstance(IWebApi.class);
    @Inject
    @Named(WEB_API_DOMAIN_TREE_ENHANCER)
    private IDomainTreeEnhancer domainTreeEnhancer;

    @Before
    public void startWebApp() {
        webApplication.attachWebApplication(PREFIX, webApp);
        setUser(TgMcpServerTestCaseConfiguration.USER_MCP_TEST);
    }

    @After
    public void stopWebApp() {
        webApplication.detachWebApplication(webApp);
    }

    @After
    public void restoreDomain() {
        domainTreeEnhancer.calculatedProperties().forEach((type, props) -> {
            webApi.propertiesRemoved((Class<? extends AbstractEntity<?>>) type, props.stream().map(CalculatedProperty::pathAndName).collect(toSet()));
        });
        domainTreeEnhancer.restoreAll();
        domainTreeEnhancer.apply();
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
    public void tool_execute_query() throws Exception {
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

        // There were no errors, so "data" is the only member of the response document.
        final Map<String, Object> structuredContent = assertInstanceOf(Map.class, result.structuredContent());
        assertThat(structuredContent).containsOnlyKeys("data");

        // The shape of "data" follows the shape of the selection set.
        final Map<String, Object> data = assertInstanceOf(Map.class, structuredContent.get("data"));
        assertThat(data).containsOnlyKeys("tgVehicle");
        final List<Object> vehicles = assertInstanceOf(List.class, data.get("tgVehicle"));
        // The query specifies no ordering, so the order of vehicles is not asserted.
        // Values are typed as they arrive from JSON: Money is coerced to a number, and thus arrives as Double.
        assertThat(vehicles).containsExactlyInAnyOrder(
                Map.of("price", 500.0,
                       "active", true,
                       "model", Map.of("make", Map.of("key", "AUDI"),
                                       "makeModelsCount", 1)),
                Map.of("price", 450.0,
                       "active", true,
                       "model", Map.of("make", Map.of("key", "MERC"),
                                       "makeModelsCount", 1)));

        // Unstructured content is derived from structured content, so both must agree.
        assertThat(result.content()).hasSize(1);
        final var textContent = assertInstanceOf(McpSchema.TextContent.class, result.content().getFirst());
        assertEquals(structuredContent, new JsonMapper().readValue(textContent.text(), Map.class));
    }

    /// A query that could not be validated is reported in `errors`, and is a successful tool call.
    ///
    @Test
    public void tool_execute_query_with_invalid_query() {
        final var result = executeQuery("{ thereIsNoSuchEntity { key } }");
        assertFalse(result.isError());

        final Map<String, Object> structuredContent = assertInstanceOf(Map.class, result.structuredContent());
        assertThat(assertInstanceOf(List.class, structuredContent.get("errors"))).isNotEmpty();
        // GraphQL prescribes the absence of "data" if a request fails before execution begins.
        assertThat(structuredContent).doesNotContainKey("data");
    }

    @Test
    public void tool_execute_query_without_query_argument() {
        final var result = mcpClient.callTool(new McpSchema.CallToolRequest("execute_query", Map.of()));
        assertTrue(result.isError());
        assertThat(assertInstanceOf(McpSchema.TextContent.class, result.content().getFirst()).text()).contains("[query]");
    }

    @Test
    public void tool_execute_query_with_malformed_variables_argument() {
        final var result = mcpClient.callTool(new McpSchema.CallToolRequest(
                "execute_query",
                Map.of("query", "{ tgVehicle { key } }", "variables", "not an object")));
        assertTrue(result.isError());
        assertThat(assertInstanceOf(McpSchema.TextContent.class, result.content().getFirst()).text()).contains("[variables]");
    }

    @Test
    public void tool_add_calculated_property_adds_a_calculated_property_that_can_be_read_with_execute_query__top_level_access() {
        {
            final var result = mcpClient.callTool(new McpSchema.CallToolRequest(
                    TOOL_ADD_CALCULATED_PROPERTY,
                    Map.of("type", TgVehicle.class.getSimpleName(),
                           "name", "myNumber",
                           "expression", "2*3")));
            assertFalse(result.isError());
        }

        final var result = executeQuery("{tgVehicle {myNumber} }");
        final Map<String, Object> structuredContent = assertInstanceOf(Map.class, result.structuredContent());
        assertThat(structuredContent).containsOnlyKeys("data");
        final Map<String, Object> data = assertInstanceOf(Map.class, structuredContent.get("data"));
        assertThat(data).containsOnlyKeys("tgVehicle");
        final List<Object> vehicles = assertInstanceOf(List.class, data.get("tgVehicle"));
        assertThat(vehicles).allSatisfy(vehicle -> assertEquals(Map.of("myNumber", 6), vehicle));
    }

    @Test
    public void tool_add_calculated_property_adds_a_calculated_property_that_can_be_read_with_execute_query__nested_access() {
        {
            final var result = mcpClient.callTool(new McpSchema.CallToolRequest(
                    TOOL_ADD_CALCULATED_PROPERTY,
                    Map.of("type", TgVehicle.class.getSimpleName(),
                           "name", "myNumber",
                           "expression", "2*3")));
            assertFalse(result.isError());
        }

        final var result = executeQuery("{tgVehicle {replacedBy {myNumber}} }");
        final Map<String, Object> structuredContent = assertInstanceOf(Map.class, result.structuredContent());
        assertThat(structuredContent).containsOnlyKeys("data");
        final Map<String, Object> data = assertInstanceOf(Map.class, structuredContent.get("data"));
        assertThat(data).containsOnlyKeys("tgVehicle");
        final List<Object> vehicles = assertInstanceOf(List.class, data.get("tgVehicle"));
        assertThat(vehicles).allSatisfy(vehicle -> assertEquals(Map.of("replacedBy", Map.of("myNumber", 6)), vehicle));
    }

    @Test
    public void tool_add_calculated_property_returns_an_error_if_the_property_being_added_has_the_same_name_as_the_one_added_previously() {
        {
            final var result = mcpClient.callTool(new McpSchema.CallToolRequest(
                    TOOL_ADD_CALCULATED_PROPERTY,
                    Map.of("type", TgVehicle.class.getSimpleName(),
                           "name", "myNumber",
                           "expression", "2*3")));
            assertFalse(result.isError());
        }

        {
            final var result = mcpClient.callTool(new McpSchema.CallToolRequest(
                    TOOL_ADD_CALCULATED_PROPERTY,
                    Map.of("type", TgVehicle.class.getSimpleName(),
                           "name", "myNumber",
                           "expression", "5*4")));
            assertTrue(result.isError());
            assertThat(result.content()).hasSize(1);
            assertThat(assertInstanceOf(McpSchema.TextContent.class, result.content().getFirst()).text()).contains("""
                Error adding calculated property [TgVehicle.myNumber] with expression:
                5*4

                Calculated property [TgVehicle.myNumber] already exists.""");
        }
    }

    @Test
    public void tool_add_calculated_property_returns_an_error_if_the_property_being_added_has_the_same_name_as_an_existing_one() {
        assertThat(domainMetadata.forPropertyOpt(TgVehicle.class, "model")).isPresent();
        final var result = mcpClient.callTool(new McpSchema.CallToolRequest(
                TOOL_ADD_CALCULATED_PROPERTY,
                Map.of("type", TgVehicle.class.getSimpleName(),
                       "name", "model",
                       "expression", "2*3")));
        assertTrue(result.isError());
        assertThat(result.content()).hasSize(1);
        assertThat(assertInstanceOf(McpSchema.TextContent.class, result.content().getFirst()).text()).contains("""
                Error adding calculated property [TgVehicle.model] with expression:
                2*3
                
                Property [TgVehicle.model] already exists in the original domain. Please try another name.""");
    }

    @Test
    public void tool_remove_calculated_property_removes_a_previously_added_calculated_property_which_then_cannot_be_read_with_execute_query() {
        {
            final var result = mcpClient.callTool(new McpSchema.CallToolRequest(
                    TOOL_ADD_CALCULATED_PROPERTY,
                    Map.of("type", TgVehicle.class.getSimpleName(),
                           "name", "myNumber",
                           "expression", "2*3")));
            assertFalse(result.isError());
        }

        {
            final var result = mcpClient.callTool(new McpSchema.CallToolRequest(
                    TOOL_REMOVE_CALCULATED_PROPERTY,
                    Map.of("type", TgVehicle.class.getSimpleName(),
                           "name", "myNumber")));
            assertFalse(result.isError());
        }

        final var result = executeQuery("{tgVehicle {myNumber} }");
        assertFalse(result.isError());
        final Map<String, Object> structuredContent = assertInstanceOf(Map.class, result.structuredContent());
        final List<Map<String, Object>> errors = assertInstanceOf(List.class, structuredContent.get("errors"));
        assertThat(errors).anySatisfy(err -> assertThat(err).extractingByKey("message", STRING).contains("Field 'myNumber' in type 'TgVehicle' is undefined"));
    }

    @Test
    public void tool_remove_calculated_property_returns_an_error_if_the_property_being_removed_does_not_exist() {
        assertThat(domainMetadata.forPropertyOpt(TgVehicle.class, "myNumber")).isEmpty();
        final var result = mcpClient.callTool(new McpSchema.CallToolRequest(
                TOOL_REMOVE_CALCULATED_PROPERTY,
                Map.of("type", TgVehicle.class.getSimpleName(),
                       "name", "myNumber")));
        assertTrue(result.isError());
        assertThat(result.content()).hasSize(1);
        assertThat(assertInstanceOf(McpSchema.TextContent.class, result.content().getFirst()).text()).contains(
                "Error removing calculated property [TgVehicle.myNumber]. Calculated property [TgVehicle.myNumber] does not exist.");
    }

    @Test
    public void tool_remove_calculated_property_returns_an_error_if_the_property_being_removed_is_an_original_persistent_property() {
        assertThat(domainMetadata.forPropertyOpt(TgVehicle.class, "model")).isPresent();
        final var result = mcpClient.callTool(new McpSchema.CallToolRequest(
                TOOL_REMOVE_CALCULATED_PROPERTY,
                Map.of("type", TgVehicle.class.getSimpleName(),
                       "name", "model")));
        assertTrue(result.isError());
        assertThat(result.content()).hasSize(1);
        assertThat(assertInstanceOf(McpSchema.TextContent.class, result.content().getFirst()).text()).contains(
                "Error removing calculated property [TgVehicle.model]. Property [TgVehicle.model] is part of the original domain and cannot be modified.");
    }

    @Test
    public void tool_remove_calculated_property_returns_an_error_if_the_property_being_removed_is_an_original_calculated_property() {
        assertThat(domainMetadata.forPropertyOpt(TgVehicle.class, "lastFuelUsage")).isPresent();
        final var result = mcpClient.callTool(new McpSchema.CallToolRequest(
                TOOL_REMOVE_CALCULATED_PROPERTY,
                Map.of("type", TgVehicle.class.getSimpleName(),
                       "name", "lastFuelUsage")));
        assertTrue(result.isError());
        assertThat(result.content()).hasSize(1);
        assertThat(assertInstanceOf(McpSchema.TextContent.class, result.content().getFirst()).text()).contains(
                "Error removing calculated property [TgVehicle.lastFuelUsage]. Property [TgVehicle.lastFuelUsage] is part of the original domain and cannot be modified.");
    }

    @Test
    public void tool_add_calculated_property_returns_an_error_if_the_specified_type_does_not_exist() {
        final var result = mcpClient.callTool(new McpSchema.CallToolRequest(
                TOOL_ADD_CALCULATED_PROPERTY,
                Map.of("type", "Nothing",
                       "name", "property",
                       "expression", "2*3")));
        assertTrue(result.isError());
        assertThat(result.content()).hasSize(1);
        assertThat(assertInstanceOf(McpSchema.TextContent.class, result.content().getFirst()).text()).contains(
                "Invalid argument [type]. No such type: Nothing.");
    }

    @Test
    public void tool_remove_calculated_property_returns_an_error_if_the_specified_type_does_not_exist() {
        final var result = mcpClient.callTool(new McpSchema.CallToolRequest(
                TOOL_REMOVE_CALCULATED_PROPERTY,
                Map.of("type", "Nothing",
                       "name", "property")));
        assertTrue(result.isError());
        assertThat(result.content()).hasSize(1);
        assertThat(assertInstanceOf(McpSchema.TextContent.class, result.content().getFirst()).text()).contains(
                "Invalid argument [type]. No such type: Nothing.");
    }

    @Test
    public void tool_add_calculated_property_returns_an_error_if_the_specified_expression_is_invalid() {
        final var result = mcpClient.callTool(new McpSchema.CallToolRequest(
                TOOL_ADD_CALCULATED_PROPERTY,
                Map.of("type", TgVehicle.class.getSimpleName(),
                       "name", "myNumber",
                       "expression", "2*a")));
        assertTrue(result.isError());
        assertThat(result.content()).hasSize(1);
        assertThat(assertInstanceOf(McpSchema.TextContent.class, result.content().getFirst()).text()).contains("""
               Error adding calculated property [TgVehicle.myNumber] with expression:
               2*a
               
               Invalid expression for calculated property [TgVehicle.myNumber]: 2*a""");
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