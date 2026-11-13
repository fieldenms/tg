package ua.com.fielden.platform.mcp;

import com.fasterxml.jackson.databind.json.JsonMapper;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.*;
import ua.com.fielden.platform.mcp.test_config.AbstractTgMcpServerTestCase;
import ua.com.fielden.platform.mcp.test_config.McpTestWebApp;
import ua.com.fielden.platform.mcp.test_config.TgMcpServerTestCaseConfiguration;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.security.tokens.persistent.TgVehicle_CanRead_price_Token;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.web.test.TestWebApplication;
import ua.com.fielden.platform.web_api.IWebApi;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.mcp.McpConfig.WEB_API_KEY_MCP_HTTP_HEADER;
import static ua.com.fielden.platform.mcp.TgMcpServer.QUERY_GUIDE_RESOURCE_URI;
import static ua.com.fielden.platform.mcp.TgMcpServer.TOOL_ENHANCE_ENTITY_TYPE;
import static ua.com.fielden.platform.mcp.test_config.TgMcpServerTestCaseConfiguration.TEST_WEB_API_KEY_MCP;
import static ua.com.fielden.platform.test_utils.TestUtils.assertInstanceOf;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.web_api.EntityTypeEnhancer.ERR_BASE_TYPE_REQUIRED;
import static ua.com.fielden.platform.web_api.EntityTypeEnhancer.ERR_UNAUTHORISED_ACCESS;

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
    private final IWebApi webApi = getInstance(IWebApi.class);

    @Before
    public void startWebApp() {
        webApplication.attachWebApplication(PREFIX, webApp);
    }

    @After
    public void stopWebApp() {
        webApplication.detachWebApplication(webApp);
    }

    /// MCP requests run as `mcp.user`, which is the current user of tests, so the enhanced entity types that they define are those of the current user.
    ///
    @After
    public void evictUserEnhancedEntityTypes() {
        webApi.evictUserEnhancedEntityTypes();
    }

    @Test
    public void resource_query_guide() {
        try (final var client = client()) {
            final var queryGuideResource = client.readResource(McpSchema.ReadResourceRequest.builder(QUERY_GUIDE_RESOURCE_URI).build());
            assertFalse(queryGuideResource.contents().isEmpty());
            final var content = assertInstanceOf(McpSchema.TextResourceContents.class, queryGuideResource.contents().getFirst());
            assertEquals("text/markdown", content.mimeType());
            assertEquals(QUERY_GUIDE_RESOURCE_URI, content.uri());
            assertThat(content.text()).contains("This document describes how to query data from a TG system using GraphQL");
        }
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
        try (final var client = client()) {
            final var result = client.callTool(McpSchema.CallToolRequest.builder("execute_query").arguments(Map.of()).build());
            assertTrue(result.isError());
            assertThat(assertInstanceOf(McpSchema.TextContent.class, result.content().getFirst()).text())
                    .isEqualTo("Argument [query] is required and must be a string.");
        }
    }

    @Test
    public void tool_execute_query_with_malformed_variables_argument() {
        try (final var client = client()) {
            final var result = client.callTool(McpSchema.CallToolRequest.builder("execute_query")
                                                       .arguments(Map.of("query", "{ tgVehicle { key } }", "variables", "not an object"))
                                                       .build());
            assertTrue(result.isError());
            assertThat(assertInstanceOf(McpSchema.TextContent.class, result.content().getFirst()).text())
                    .isEqualTo("Argument [variables] must be an object.");
        }
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Tool enhance_entity_type
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @Test
    public void tool_enhance_entity_type_creates_an_entity_type_that_can_be_queried_with_execute_query() {
        final var result = enhanceEntityType(Map.of("name", "MyVehicle",
                                                    "baseType", "TgVehicle",
                                                    "addProperties", List.of(Map.of("name", "myNumber", "expression", "2 * 3"))));
        assertFalse(result.isError());
        assertThat(text(result)).isEqualTo(
                "Created entity type [MyVehicle], which extends [TgVehicle] with properties [myNumber]. Query it with root fields [myVehicle] and [myVehicle_agg].");

        assertThat(executeQuery("{myVehicle {key(order: ASC_1) myNumber}}").structuredContent())
                .isEqualTo(Map.of("data", Map.of("myVehicle", List.of(Map.of("key", "CAR1", "myNumber", 6), Map.of("key", "CAR2", "myNumber", 6)))));
        assertThat(executeQuery("{myVehicle_agg(where: {cond: {myNumber: {eq: 6}}}) {count}}").structuredContent())
                .isEqualTo(Map.of("data", Map.of("myVehicle_agg", List.of(Map.of("count", 2)))));
    }

    @Test
    public void tool_enhance_entity_type_changes_an_existing_entity_type() {
        enhanceEntityType(Map.of("name", "MyVehicle",
                                 "baseType", "TgVehicle",
                                 "addProperties", List.of(Map.of("name", "myNumber", "expression", "2 * 3"))));
        final var result = enhanceEntityType(Map.of("name", "MyVehicle",
                                                    "addProperties", List.of(Map.of("name", "otherNumber", "expression", "myNumber + 1")),
                                                    "removeProperties", List.of("noSuchProp")));
        assertFalse(result.isError());
        assertThat(text(result)).isEqualTo("""
                Changed entity type [MyVehicle], which extends [TgVehicle] with properties [myNumber, otherNumber]. Query it with root fields [myVehicle] and [myVehicle_agg].
                Warning: Entity type [MyVehicle] has no properties [noSuchProp], so they were not removed.""");

        assertThat(executeQuery("{myVehicle {key(order: ASC_1) myNumber otherNumber}}").structuredContent())
                .isEqualTo(Map.of("data", Map.of("myVehicle", List.of(Map.of("key", "CAR1", "myNumber", 6, "otherNumber", 7),
                                                                       Map.of("key", "CAR2", "myNumber", 6, "otherNumber", 7)))));
    }

    @Test
    public void tool_enhance_entity_type_returns_an_error_if_the_request_is_rejected() {
        final var result = enhanceEntityType(Map.of("name", "MyVehicle",
                                                    "addProperties", List.of(Map.of("name", "myNumber", "expression", "2 * 3"))));
        assertTrue(result.isError());
        assertThat(text(result)).isEqualTo(ERR_BASE_TYPE_REQUIRED.formatted("MyVehicle"));
        assertThat(webApi.findEnhancedEntityType("MyVehicle")).isEmpty();
    }

    @Test
    public void tool_enhance_entity_type_returns_an_error_if_an_expression_is_invalid() {
        final var result = enhanceEntityType(Map.of("name", "MyVehicle",
                                                    "baseType", "TgVehicle",
                                                    "addProperties", List.of(Map.of("name", "myNumber", "expression", "2 * noSuchProp"))));
        assertTrue(result.isError());
        assertThat(text(result)).startsWith("Property [myNumber] cannot be added.");
        assertThat(webApi.findEnhancedEntityType("MyVehicle")).isEmpty();
    }

    @Test
    public void tool_enhance_entity_type_returns_an_error_if_an_expression_refers_to_properties_that_cannot_be_read() {
        final Map<String, Object> arguments = Map.of("name", "MyVehicle",
                                                     "baseType", "TgVehicle",
                                                     "addProperties", List.of(Map.of("name", "myPrice", "expression", "2 * price")));
        runWithoutToken(TgVehicle_CanRead_price_Token.class, () -> {
            final var result = enhanceEntityType(arguments);
            assertTrue(result.isError());
            assertThat(text(result)).isEqualTo(ERR_UNAUTHORISED_ACCESS.formatted("myPrice", "price"));
            assertThat(webApi.findEnhancedEntityType("MyVehicle")).isEmpty();
        });
        // A positive control: the same request succeeds when the token is held.
        assertFalse(enhanceEntityType(arguments).isError());
    }

    @Test
    public void tool_enhance_entity_type_returns_an_error_if_arguments_are_malformed() {
        final var invalidAddProperties = "Argument [addProperties] must be an array of objects, each with string properties [name] and [expression].";
        final var invalidRemoveProperties = "Argument [removeProperties] must be an array of strings.";
        final List<T2<Map<String, Object>, String>> cases = List.of(
                t2(Map.of("baseType", "TgVehicle"), "Argument [name] is required and must be a string."),
                t2(Map.of("name", 1), "Argument [name] is required and must be a string."),
                t2(Map.of("name", "MyVehicle", "baseType", 1), "Argument [baseType] must be a string."),
                t2(Map.of("name", "MyVehicle", "addProperties", "myNumber"), invalidAddProperties),
                t2(Map.of("name", "MyVehicle", "addProperties", List.of("myNumber")), invalidAddProperties),
                t2(Map.of("name", "MyVehicle", "addProperties", List.of(Map.of("name", "myNumber"))), invalidAddProperties),
                t2(Map.of("name", "MyVehicle", "addProperties", List.of(Map.of("name", "myNumber", "expression", 6))), invalidAddProperties),
                t2(Map.of("name", "MyVehicle", "removeProperties", "myNumber"), invalidRemoveProperties),
                t2(Map.of("name", "MyVehicle", "removeProperties", List.of(1)), invalidRemoveProperties));
        for (final var testCase : cases) {
            testCase.run((args, error) -> {
                final var result = enhanceEntityType(args);
                assertTrue(result.isError());
                assertThat(text(result)).as("Arguments %s", args).isEqualTo(error);
            });
        }
        assertThat(webApi.findEnhancedEntityType("MyVehicle")).isEmpty();
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Invalid requests
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @Test
    public void non_existing_resource() {
        try (final var client = client()) {
            assertThatThrownBy(() -> client.readResource(McpSchema.ReadResourceRequest.builder("tg://abcd").build()))
                    .isInstanceOf(McpError.class);
        }
    }

    @Test
    public void non_existing_tool() {
        try (final var client = client()) {
            assertThatThrownBy(() -> client.callTool(McpSchema.CallToolRequest.builder("magic").build()))
                    .isInstanceOf(McpError.class);
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
        try (final var client = client()) {
            return client.callTool(McpSchema.CallToolRequest.builder("execute_query")
                                           .arguments(Map.of("query", query, "variables", variables))
                                           .build());
        }
    }

    private McpSchema.CallToolResult executeQuery(final String query) {
        return executeQuery(query, Map.of());
    }

    private McpSchema.CallToolResult enhanceEntityType(final Map<String, Object> arguments) {
        try (final var client = client()) {
            return client.callTool(McpSchema.CallToolRequest.builder(TOOL_ENHANCE_ENTITY_TYPE).arguments(arguments).build());
        }
    }

    /// The text of a tool result that consists of a single text content.
    ///
    private static String text(final McpSchema.CallToolResult result) {
        assertThat(result.content()).hasSize(1);
        return assertInstanceOf(McpSchema.TextContent.class, result.content().getFirst()).text();
    }

    private McpSyncClient client() {
        final var transport = HttpClientStreamableHttpTransport.builder(URI)
                .endpoint(PREFIX + TgMcpServerTestCaseConfiguration.PATH_MCP)
                .addHttpRequestCustomizer((builder, _, _, _, _) -> builder.header(WEB_API_KEY_MCP_HTTP_HEADER, TEST_WEB_API_KEY_MCP))
                .build();
        return McpClient.sync(transport)
                .requestTimeout(Duration.ofHours(999))
                .build();
    }

}