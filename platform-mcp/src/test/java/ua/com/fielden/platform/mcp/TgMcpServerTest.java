package ua.com.fielden.platform.mcp;

import com.fasterxml.jackson.databind.json.JsonMapper;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.*;
import ua.com.fielden.platform.domaintree.IDomainManager;
import ua.com.fielden.platform.domaintree.impl.CalculatedProperty;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.mcp.test_config.AbstractTgMcpServerTestCase;
import ua.com.fielden.platform.mcp.test_config.McpTestWebApp;
import ua.com.fielden.platform.mcp.test_config.TgMcpServerTestCaseConfiguration;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.persistent.TgVehicle_CanRead_model_Token;
import ua.com.fielden.platform.security.tokens.persistent.TgVehicle_CanRead_price_Token;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.CollectionUtil;
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
import static ua.com.fielden.platform.mcp.test_config.TgMcpServerTestCaseConfiguration.TEST_WEB_API_KEY_MCP;
import static ua.com.fielden.platform.test_utils.TestUtils.assertInstanceOf;

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

    private final IDomainMetadata domainMetadata = getInstance(IDomainMetadata.class);
    private final IWebApi webApi = getInstance(IWebApi.class);
    private final IDomainManager domainManager = getInstance(IDomainManager.class);

    @Before
    public void startWebApp() {
        webApplication.attachWebApplication(PREFIX, webApp);
    }

    @After
    public void stopWebApp() {
        webApplication.detachWebApplication(webApp);
    }

    @After
    public void restoreDomain() {
        final var domainTreeEnhancer = domainManager.domainTreeEnhancer(getUser());
        domainTreeEnhancer.calculatedProperties().forEach((type, props) -> {
            webApi.propertiesRemoved((Class<? extends AbstractEntity<?>>) type, props.stream().map(CalculatedProperty::pathAndName).collect(toSet()));
        });
        domainTreeEnhancer.restoreAll();
        domainTreeEnhancer.apply();
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

    @Test
    public void tool_add_calculated_property_adds_a_calculated_property_that_can_be_read_with_execute_query__top_level_access() {
        addCalculatedProperty(TgVehicle.class, "myNumber", "2*3");

        final var result = executeQuery("{tgVehicle {myNumber} }");
        final Map<String, Object> structuredContent = assertInstanceOf(Map.class, result.structuredContent());
        assertThat(structuredContent).containsOnlyKeys("data");
        final Map<String, Object> data = assertInstanceOf(Map.class, structuredContent.get("data"));
        assertThat(data).containsOnlyKeys("tgVehicle");
        final List<Object> vehicles = assertInstanceOf(List.class, data.get("tgVehicle"));
        assertThat(vehicles).allSatisfy(vehicle -> assertEquals(Map.of("myNumber", 6), vehicle));
    }

    @Test
    public void tool_add_calculated_property_adds_a_calculated_property_that_can_be_used_in_conditions_with_execute_query__top_level_access() {
        addCalculatedProperty(TgVehicle.class, "myNumber", "2*3");

        assertThat(executeQuery("{tgVehicle(order: ASC_1, where: {cond: {myNumber: {eq: 6}}}) {key} }").structuredContent())
                .isEqualTo(Map.of("data", Map.of("tgVehicle", List.of(Map.of("key", "CAR1"), Map.of("key", "CAR2")))));
        assertThat(executeQuery("{tgVehicle(where: {cond: {myNumber: {ne: 6}}}) {key} }").structuredContent())
                .isEqualTo(Map.of("data", Map.of("tgVehicle", List.of())));
        assertThat(executeQuery("{tgVehicle_agg(where: {cond: {myNumber: {eq: 6}}}) {count} }").structuredContent())
                .isEqualTo(Map.of("data", Map.of("tgVehicle_agg", List.of(Map.of("count", 2)))));
        assertThat(executeQuery("{tgVehicle_agg(where: {cond: {myNumber: {ne: 6}}}) {count} }").structuredContent())
                .isEqualTo(Map.of("data", Map.of("tgVehicle_agg", List.of(Map.of("count", 0)))));
    }

    @Test
    public void tool_add_calculated_property_adds_a_calculated_property_that_can_be_aggregated_with_execute_query__top_level_access() {
        addCalculatedProperty(TgVehicle.class, "myNumber", "2*3");

        final var result = executeQuery("{tgVehicle_agg {groupBy {myNumber} count sum {myNumber} avg {myNumber} max {myNumber} min {myNumber}} }");
        assertThat(result.structuredContent()).isEqualTo(Map.of("data", Map.of("tgVehicle_agg", List.of(
                Map.of("groupBy", Map.of("myNumber", 6),
                       "count", 2,
                       "sum", Map.of("myNumber", 12),
                       "avg", Map.of("myNumber", 6),
                       "max", Map.of("myNumber", 6),
                       "min", Map.of("myNumber", 6))))));
    }

    @Test
    public void calculated_properties_added_via_MCP_cannot_be_read_via_nested_access__result_fields_of_data_queries() {
        addCalculatedProperty(TgVehicle.class, "myNumber", "2*3");

        assertExecutionError("{tgVehicle {replacedBy {myNumber}} }", "tgVehicle",
                             "The property [replacedBy.myNumber] does not exist in entity type [TgVehicle$$TgEntity_");
    }

    @Test
    public void calculated_properties_added_via_MCP_cannot_be_read_via_nested_access__conditions_of_data_queries() {
        addCalculatedProperty(TgVehicle.class, "myNumber", "2*3");

        assertExecutionError("{tgVehicle(where: {cond: {replacedBy: {cond: {myNumber: {eq: 6}}}}}) {key} }", "tgVehicle",
                             "Error during entity retrieval.");
    }

    @Test
    public void calculated_properties_added_via_MCP_cannot_be_read_via_nested_access__result_fields_of_aggregation_queries() {
        addCalculatedProperty(TgVehicle.class, "myNumber", "2*3");

        List.of("groupBy", "sum", "avg", "max", "min").forEach(field -> assertExecutionError(
                "{tgVehicle_agg {%s {replacedBy {myNumber}}} }".formatted(field), "tgVehicle_agg",
                "Error during entity retrieval."));
    }

    @Test
    public void calculated_properties_added_via_MCP_cannot_be_read_via_nested_access__conditions_of_aggregation_queries() {
        addCalculatedProperty(TgVehicle.class, "myNumber", "2*3");

        assertExecutionError("{tgVehicle_agg(where: {cond: {replacedBy: {cond: {myNumber: {eq: 6}}}}}) {count} }", "tgVehicle_agg",
                             "Error during entity retrieval.");
    }

    @Test
    public void tool_add_calculated_property_returns_an_error_if_the_property_being_added_has_the_same_name_as_the_one_added_previously() {
        addCalculatedProperty(TgVehicle.class, "myNumber", "2*3");

        try (final var client = client()) {
            final var result = client.callTool(McpSchema.CallToolRequest.builder(TOOL_ADD_CALCULATED_PROPERTY)
                                                       .arguments(Map.of("type", TgVehicle.class.getSimpleName(),
                                                                         "name", "myNumber",
                                                                         "expression", "5*4"))
                                                       .build());
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
        try (final var client = client()) {
            final var result = client.callTool(McpSchema.CallToolRequest.builder(TOOL_ADD_CALCULATED_PROPERTY)
                                                       .arguments(Map.of("type", TgVehicle.class.getSimpleName(),
                                                                         "name", "model",
                                                                         "expression", "2*3"))
                                                       .build());
            assertTrue(result.isError());
            assertThat(result.content()).hasSize(1);
            assertThat(assertInstanceOf(McpSchema.TextContent.class, result.content().getFirst()).text()).contains("""
                Error adding calculated property [TgVehicle.model] with expression:
                2*3
                
                Property [TgVehicle.model] already exists in the original domain. Please try another name.""");
        }
    }

    @Test
    public void tool_remove_calculated_property_removes_a_previously_added_calculated_property_which_then_cannot_be_read_with_execute_query() {
        addCalculatedProperty(TgVehicle.class, "myNumber", "2*3");

        try (final var client = client()) {
            final var result = client.callTool(McpSchema.CallToolRequest.builder(TOOL_REMOVE_CALCULATED_PROPERTY)
                                                       .arguments(Map.of("type", TgVehicle.class.getSimpleName(),
                                                                         "name", "myNumber"))
                                                       .build());
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
        try (final var client = client()) {
            final var result = client.callTool(McpSchema.CallToolRequest.builder(TOOL_REMOVE_CALCULATED_PROPERTY)
                                                       .arguments(Map.of("type", TgVehicle.class.getSimpleName(),
                                                                         "name", "myNumber"))
                                                       .build());
            assertTrue(result.isError());
            assertThat(result.content()).hasSize(1);
            assertThat(assertInstanceOf(McpSchema.TextContent.class, result.content().getFirst()).text()).contains(
                    "Error removing calculated property [TgVehicle.myNumber]. Calculated property [TgVehicle.myNumber] does not exist.");
        }
    }

    @Test
    public void tool_remove_calculated_property_returns_an_error_if_the_property_being_removed_is_an_original_persistent_property() {
        assertThat(domainMetadata.forPropertyOpt(TgVehicle.class, "model")).isPresent();
        try (final var client = client()) {
            final var result = client.callTool(McpSchema.CallToolRequest.builder(TOOL_REMOVE_CALCULATED_PROPERTY)
                                                       .arguments(Map.of("type", TgVehicle.class.getSimpleName(),
                                                                         "name", "model"))
                                                       .build());
            assertTrue(result.isError());
            assertThat(result.content()).hasSize(1);
            assertThat(assertInstanceOf(McpSchema.TextContent.class, result.content().getFirst()).text()).contains(
                    "Error removing calculated property [TgVehicle.model]. Property [TgVehicle.model] is part of the original domain and cannot be modified.");
        }
    }

    @Test
    public void tool_remove_calculated_property_returns_an_error_if_the_property_being_removed_is_an_original_calculated_property() {
        assertThat(domainMetadata.forPropertyOpt(TgVehicle.class, "lastFuelUsage")).isPresent();
        try (final var client = client()) {
            final var result = client.callTool(McpSchema.CallToolRequest.builder(TOOL_REMOVE_CALCULATED_PROPERTY)
                                                       .arguments(Map.of("type", TgVehicle.class.getSimpleName(),
                                                                         "name", "lastFuelUsage"))
                                                       .build());
            assertTrue(result.isError());
            assertThat(result.content()).hasSize(1);
            assertThat(assertInstanceOf(McpSchema.TextContent.class, result.content().getFirst()).text()).contains(
                    "Error removing calculated property [TgVehicle.lastFuelUsage]. Property [TgVehicle.lastFuelUsage] is part of the original domain and cannot be modified.");
        }
    }

    @Test
    public void tool_add_calculated_property_returns_an_error_if_the_specified_type_does_not_exist() {
        try (final var client = client()) {
            final var result = client.callTool(McpSchema.CallToolRequest.builder(TOOL_ADD_CALCULATED_PROPERTY)
                                                       .arguments(Map.of("type", "Nothing",
                                                                         "name", "property",
                                                                         "expression", "2*3"))
                                                       .build());
            assertTrue(result.isError());
            assertThat(result.content()).hasSize(1);
            assertThat(assertInstanceOf(McpSchema.TextContent.class, result.content().getFirst()).text()).contains(
                    "Invalid argument [type]. No such type: Nothing.");
        }
    }

    @Test
    public void tool_remove_calculated_property_returns_an_error_if_the_specified_type_does_not_exist() {
        try (final var client = client()) {
            final var result = client.callTool(McpSchema.CallToolRequest.builder(TOOL_REMOVE_CALCULATED_PROPERTY)
                                                       .arguments(Map.of("type", "Nothing",
                                                                         "name", "property"))
                                                       .build());
            assertTrue(result.isError());
            assertThat(result.content()).hasSize(1);
            assertThat(assertInstanceOf(McpSchema.TextContent.class, result.content().getFirst()).text()).contains(
                    "Invalid argument [type]. No such type: Nothing.");
        }
    }

    @Test
    public void tool_add_calculated_property_returns_an_error_if_the_specified_expression_is_invalid() {
        try (final var client = client()) {
            final var result = client.callTool(McpSchema.CallToolRequest.builder(TOOL_ADD_CALCULATED_PROPERTY)
                                                       .arguments(Map.of("type", TgVehicle.class.getSimpleName(),
                                                                         "name", "myNumber",
                                                                         "expression", "2*a"))
                                                       .build());
            assertTrue(result.isError());
            assertThat(result.content()).hasSize(1);
            assertThat(assertInstanceOf(McpSchema.TextContent.class, result.content().getFirst()).text()).contains("""
               Error adding calculated property [TgVehicle.myNumber] with expression:
               2*a

               Invalid expression for calculated property [TgVehicle.myNumber]: 2*a""");
        }
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Authorisation of property access in expressions of calculated properties
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @Test
    public void tool_add_calculated_property_returns_an_error_if_the_expression_performs_unathorised_access() {
        interface Test {
            void run(final Class<?> type,
                     final String propName,
                     final String expression,
                     final Iterable<? extends Class<? extends ISecurityToken>> restrictedTokens,
                     final List<? extends CharSequence> unathorisedProps);
        }
        final Test test = (type, propName, expression, restrictedTokens, unathorisedProps) -> {
            runWithoutTokens(restrictedTokens, () -> {
                try (final var client = client()) {
                    final var result = client.callTool(McpSchema.CallToolRequest.builder(TOOL_ADD_CALCULATED_PROPERTY)
                                                               .arguments(Map.of("type", type.getSimpleName(),
                                                                                 "name", propName,
                                                                                 "expression", expression))
                                                               .build());
                    assertTrue(result.isError());
                    assertThat(result.content()).hasSize(1);
                    assertThat(assertInstanceOf(McpSchema.TextContent.class, result.content().getFirst()).text()).contains("""
               Error adding calculated property [%s.%s] with expression:
               %s
               Reason: Unauthorised access to the following properties: [%s]."""
               .formatted(type.getSimpleName(), propName, expression, CollectionUtil.toString(unathorisedProps, ", ")));
                }
            });

            assertThat(domainManager.domainTreeEnhancer(getUser()).findCalculatedProperty(type, propName)).isEmpty();
            assertGraphQlTypeHasNoField(webApi, type.getSimpleName(), propName);
        };

        test.run(TgVehicle.class, "myProp", "2 * price", List.of(TgVehicle_CanRead_price_Token.class), List.of("price"));
        test.run(TgVehicle.class, "myProp", "2 * replacedBy.price",
                 List.of(TgVehicle_CanRead_price_Token.class),
                 List.of("replacedBy.price"));
        test.run(TgVehicle.class, "myProp", "2 * replacedBy.price + price",
                 List.of(TgVehicle_CanRead_price_Token.class),
                 List.of("price", "replacedBy.price"));
        test.run(TgVehicle.class, "myProp", "model", List.of(TgVehicle_CanRead_model_Token.class), List.of("model"));
        test.run(TgVehicle.class, "myProp", "model.make", List.of(TgVehicle_CanRead_model_Token.class), List.of("model"));
        test.run(TgVehicle.class, "myProp", "replacedBy.model.make",
                 List.of(TgVehicle_CanRead_model_Token.class),
                 List.of("replacedBy.model"));
        test.run(TgVehicle.class, "myProp", "model.makeModelsCount + price",
                 List.of(TgVehicle_CanRead_model_Token.class, TgVehicle_CanRead_price_Token.class),
                 List.of("model", "price"));
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

    private void addCalculatedProperty(final Class<? extends AbstractEntity<?>> type, final String name, final String expression) {
        try (final var client = client()) {
            final var result = client.callTool(McpSchema.CallToolRequest.builder(TOOL_ADD_CALCULATED_PROPERTY)
                                                       .arguments(Map.of("type", type.getSimpleName(),
                                                                         "name", name,
                                                                         "expression", expression))
                                                       .build());
            assertFalse(result.isError());
        }
    }

    /// Asserts that `query` results in a single GraphQL error, raised while fetching data for root field `rootField`, with a message that contains `message`.
    ///
    private void assertExecutionError(final String query, final String rootField, final String message) {
        final var result = executeQuery(query);
        assertFalse(result.isError()); // Not an MCP tool error, but a GraphQL error.
        final Map<String, Object> structuredContent = assertInstanceOf(Map.class, result.structuredContent());
        assertThat(structuredContent).containsKey("errors");
        final List<Map<String, Object>> errors = assertInstanceOf(List.class, structuredContent.get("errors"));
        assertThat(errors).hasSize(1);
        assertThat(errors.getFirst()).extractingByKey("message", STRING).contains(
                "Exception while fetching data (/%s) : %s".formatted(rootField, message));
    }

    private void assertGraphQlTypeHasField(final IWebApi webApi, final CharSequence typeName, final CharSequence fieldName) {
        assertThat(graphQlTypeFields(webApi, typeName)).contains(fieldName.toString());
    }

    private void assertGraphQlTypeHasNoField(final IWebApi webApi, final CharSequence typeName, final CharSequence fieldName) {
        assertThat(graphQlTypeFields(webApi, typeName)).doesNotContain(fieldName.toString());
    }

    private List<String> graphQlTypeFields(final IWebApi webApi, final CharSequence typeName) {
        final var query = """
                          query {
                              __type(name: "%s") { fields { name } }
                          }
                          """.formatted(typeName);
        final var result = webApi.execute(Map.of("query", query));
        assertThat(result).doesNotContainKey("error");
        assertThat(result).containsKey("data");
        final Map<String, Object> data = assertInstanceOf(Map.class, result.get("data"));
        assertThat(data).containsKey("__type");
        final Map<String, Object> __type = assertInstanceOf(Map.class, data.get("__type"));
        assertThat(__type).containsKey("fields");
        final List<Map<String, Object>> fields = assertInstanceOf(List.class, __type.get("fields"));
        return fields.stream().map(fld -> (String) fld.get("name")).toList();
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