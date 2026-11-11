package ua.com.fielden.platform.web_api;

import org.junit.After;
import org.junit.Test;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.persistent.TgVehicleModel_CanReadModel_Token;
import ua.com.fielden.platform.security.tokens.persistent.TgVehicleModel_CanRead_Token;
import ua.com.fielden.platform.security.tokens.persistent.TgVehicle_CanRead_model_Token;
import ua.com.fielden.platform.security.tokens.persistent.TgVehicle_CanRead_price_Token;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.web_api.IWebApi.Enhancement;
import ua.com.fielden.platform.web_api.IWebApi.PropertyDefinition;
import ua.com.fielden.platform.web_api.exceptions.WebApiException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ua.com.fielden.platform.utils.CollectionUtil.append;
import static ua.com.fielden.platform.utils.CollectionUtil.concatList;
import static ua.com.fielden.platform.web_api.EntityTypeEnhancer.*;
import static ua.com.fielden.platform.web_api.WebApiUtils.*;

/// Tests for enhanced entity types, defined with [IWebApi#enhanceEntityType].
///
/// Each test that expects a request to be rejected also asserts that the rejected request changed nothing.
///
public class WebApiEnhancedEntityTypeTest extends AbstractDaoTestCase {

    private static final String OTHER_USER = "OTHER_USER";

    private final IWebApi webApi = getInstance(IWebApi.class);

    @After
    public void discardEnhancedEntityTypes() {
        webApi.discardEnhancedEntityTypes();
        runAs(OTHER_USER, webApi::discardEnhancedEntityTypes);
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Creating and enhancing
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @Test
    public void a_new_enhanced_entity_type_has_all_fields_of_its_base_type_and_the_added_properties() {
        final var enhancement = enhance("MyVehicle", "TgVehicle", props("myNumber", "2 * 3"));

        assertThat(enhancement.warnings()).isEmpty();
        assertThat(enhancement.entityType().name()).isEqualTo("MyVehicle");
        assertThat(enhancement.entityType().baseType()).isEqualTo(TgVehicle.class);
        assertThat(enhancement.entityType().properties()).isEqualTo(Map.of("myNumber", "2 * 3"));
        assertThat(webApi.findEnhancedEntityType("MyVehicle")).contains(enhancement.entityType());

        assertThat(fieldNames("MyVehicle")).isEqualTo(append(fieldNames("TgVehicle"), "myNumber"));
        assertThat(inputFieldNames("MyVehicle_Cond_cond")).isEqualTo(append(inputFieldNames("TgVehicle_Cond_cond"), "myNumber"));
        assertThat(fieldNames("MyVehicle_Agg")).isEqualTo(fieldNames("TgVehicle_Agg"));
        assertThat(inputFieldNames("MyVehicle_Cond")).isEqualTo(inputFieldNames("TgVehicle_Cond"));
    }

    @Test
    public void the_base_type_of_an_enhanced_entity_type_is_unaffected() {
        final var tgVehicleFields = fieldNames("TgVehicle");
        enhance("MyVehicle", "TgVehicle", props("myNumber", "2 * 3"));

        assertThat(fieldNames("TgVehicle")).isEqualTo(tgVehicleFields).doesNotContain("myNumber");
        assertThat(inputFieldNames("TgVehicle_Cond_cond")).doesNotContain("myNumber");
        assertValidationError("{tgVehicle {myNumber}}", "Field 'myNumber' in type 'TgVehicle' is undefined");
    }

    @Test
    public void an_enhanced_entity_type_can_be_queried_through_its_root_field() {
        enhance("MyVehicle", "TgVehicle", props("myNumber", "2 * 3", "doublePrice", "2 * price.amount"));

        assertThat(execute("{myVehicle {key(order: ASC_1) myNumber doublePrice}}"))
                .isEqualTo(result(Map.of("myVehicle", List.of(
                        Map.of("key", "CAR1", "myNumber", 6, "doublePrice", new BigDecimal("1000.00")),
                        Map.of("key", "CAR2", "myNumber", 6, "doublePrice", new BigDecimal("900.00"))))));
    }

    @Test
    public void added_properties_of_an_enhanced_entity_type_can_be_used_in_conditions() {
        enhance("MyVehicle", "TgVehicle", props("doublePrice", "2 * price.amount"));

        assertThat(execute("{myVehicle(where: {cond: {doublePrice: {gt: 950}}}) {key}}"))
                .isEqualTo(result(Map.of("myVehicle", List.of(Map.of("key", "CAR1")))));
        assertThat(execute("{myVehicle_agg(where: {cond: {doublePrice: {lt: 950}}}) {count}}"))
                .isEqualTo(result(Map.of("myVehicle_agg", List.of(Map.of("count", 1)))));
    }

    @Test
    public void added_properties_of_an_enhanced_entity_type_can_be_aggregated() {
        enhance("MyVehicle", "TgVehicle", props("doublePrice", "2 * price.amount"));

        assertThat(execute("{myVehicle_agg {count sum {doublePrice} max {doublePrice} min {doublePrice}}}"))
                .isEqualTo(result(Map.of("myVehicle_agg", List.of(
                        Map.of("count", 2,
                               "sum", Map.of("doublePrice", new BigDecimal("1900.00")),
                               "max", Map.of("doublePrice", new BigDecimal("1000.00")),
                               "min", Map.of("doublePrice", new BigDecimal("900.00")))))));
    }

    @Test
    public void an_enhanced_entity_type_is_described_by_the_domain_meta_schema_as_its_base_type_is_with_the_added_properties() {
        enhance("MyVehicle", "TgVehicle", props("myNumber", "2 * 3"));

        final var result = execute("{_entityType(like: \"MyVehicle,TgVehicle\") {name rootField baseType title desc kind keyType keyMembers hasDesc properties {name}}}");
        assertThat(errors(result)).isEmpty();
        final List<Map<String, Object>> entityTypes = (List<Map<String, Object>>) data(result).get("_entityType");
        assertThat(entityTypes).extracting(entityType -> entityType.get("name")).containsExactlyInAnyOrder("MyVehicle", "TgVehicle");
        final var myVehicle = entityTypes.stream().filter(entityType -> "MyVehicle".equals(entityType.get("name"))).findFirst().orElseThrow();
        final var tgVehicle = entityTypes.stream().filter(entityType -> "TgVehicle".equals(entityType.get("name"))).findFirst().orElseThrow();

        assertThat(myVehicle).containsEntry("rootField", "myVehicle").containsEntry("baseType", "TgVehicle");
        for (final var field : List.of("title", "desc", "kind", "keyType", "keyMembers", "hasDesc")) {
            assertThat(myVehicle.get(field)).as(field).isEqualTo(tgVehicle.get(field));
        }
        assertThat(propertyNames(myVehicle)).isEqualTo(append(propertyNames(tgVehicle), "myNumber"));
    }

    @Test
    public void properties_can_be_added_to_and_removed_from_an_existing_enhanced_entity_type() {
        enhance("MyVehicle", "TgVehicle", props("myNumber", "2 * 3", "otherNumber", "4"));
        final var enhancement = enhance("MyVehicle", null, props("thirdNumber", "5"), List.of("myNumber"));

        assertThat(enhancement.warnings()).isEmpty();
        assertThat(enhancement.entityType().properties()).isEqualTo(Map.of("otherNumber", "4", "thirdNumber", "5"));
        assertThat(fieldNames("MyVehicle")).isEqualTo(concatList(fieldNames("TgVehicle"), List.of("otherNumber", "thirdNumber")));
        assertThat(inputFieldNames("MyVehicle_Cond_cond")).isEqualTo(concatList(inputFieldNames("TgVehicle_Cond_cond"), List.of("otherNumber", "thirdNumber")));
        assertValidationError("{myVehicle {myNumber}}", "Field 'myNumber' in type 'MyVehicle' is undefined");
        assertThat(execute("{myVehicle {key(order: ASC_1) otherNumber thirdNumber}}"))
                .isEqualTo(result(Map.of("myVehicle", List.of(
                        Map.of("key", "CAR1", "otherNumber", 4, "thirdNumber", 5),
                        Map.of("key", "CAR2", "otherNumber", 4, "thirdNumber", 5)))));
    }

    @Test
    public void a_property_can_be_replaced_by_removing_it_and_then_adding_it() {
        enhance("MyVehicle", "TgVehicle", props("myNumber", "2 * 3"));
        enhance("MyVehicle", null, props(), List.of("myNumber"));
        enhance("MyVehicle", null, props("myNumber", "3 * 3"));

        assertThat(execute("{myVehicle {key(order: ASC_1) myNumber}}"))
                .isEqualTo(result(Map.of("myVehicle", List.of(Map.of("key", "CAR1", "myNumber", 9), Map.of("key", "CAR2", "myNumber", 9)))));
    }

    @Test
    public void an_added_property_may_refer_to_properties_added_before_it() {
        enhance("MyVehicle", "TgVehicle", props("a", "2 * 3", "b", "a + 1"));
        enhance("MyVehicle", null, props("c", "b * 10"));

        assertThat(execute("{myVehicle {key(order: ASC_1) a b c}}"))
                .isEqualTo(result(Map.of("myVehicle", List.of(
                        Map.of("key", "CAR1", "a", 6, "b", 7, "c", 70),
                        Map.of("key", "CAR2", "a", 6, "b", 7, "c", 70)))));
    }

    @Test
    public void an_enhanced_entity_type_without_added_properties_is_a_copy_of_its_base_type() {
        final var enhancement = enhance("MyVehicle", "TgVehicle", props());

        assertThat(enhancement.entityType().properties()).isEmpty();
        assertThat(fieldNames("MyVehicle")).isEqualTo(fieldNames("TgVehicle"));
        assertThat(execute("{myVehicle {key(order: ASC_1)}}"))
                .isEqualTo(result(Map.of("myVehicle", List.of(Map.of("key", "CAR1"), Map.of("key", "CAR2")))));
    }

    @Test
    public void removing_a_property_that_the_enhanced_entity_type_does_not_have_results_in_a_warning_and_the_rest_of_the_request_takes_effect() {
        enhance("MyVehicle", "TgVehicle", props("myNumber", "2 * 3"));
        final var enhancement = enhance("MyVehicle", null, props("otherNumber", "4"), List.of("noSuchProp", "myNumber", "otherNoSuchProp"));

        assertThat(enhancement.warnings()).containsExactly(WARN_NO_SUCH_PROPERTIES_TO_REMOVE.formatted("MyVehicle", "noSuchProp, otherNoSuchProp"));
        assertThat(enhancement.entityType().properties()).isEqualTo(Map.of("otherNumber", "4"));
    }

    @Test
    public void removing_properties_while_creating_an_enhanced_entity_type_results_in_a_warning() {
        final var enhancement = enhance("MyVehicle", "TgVehicle", props("myNumber", "2 * 3"), List.of("noSuchProp"));

        assertThat(enhancement.warnings()).containsExactly(WARN_NO_SUCH_PROPERTIES_TO_REMOVE.formatted("MyVehicle", "noSuchProp"));
        assertThat(enhancement.entityType().properties()).isEqualTo(Map.of("myNumber", "2 * 3"));
    }

    @Test
    public void a_user_can_have_several_enhanced_entity_types_with_the_same_base_type() {
        enhance("MyVehicle", "TgVehicle", props("myNumber", "2 * 3"));
        enhance("YourVehicle", "TgVehicle", props("yourNumber", "4"));

        assertThat(fieldNames("MyVehicle")).contains("myNumber").doesNotContain("yourNumber");
        assertThat(fieldNames("YourVehicle")).contains("yourNumber").doesNotContain("myNumber");
    }

    @Test
    public void discarding_enhanced_entity_types_removes_them_from_the_schema() {
        enhance("MyVehicle", "TgVehicle", props("myNumber", "2 * 3"));
        webApi.discardEnhancedEntityTypes();

        assertThat(webApi.findEnhancedEntityType("MyVehicle")).isEmpty();
        assertTypeIsAbsent("MyVehicle");
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Users
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @Test
    public void enhanced_entity_types_of_a_user_are_invisible_to_other_users() {
        enhance("MyVehicle", "TgVehicle", props("myNumber", "2 * 3"));

        runAs(OTHER_USER, () -> {
            assertThat(webApi.findEnhancedEntityType("MyVehicle")).isEmpty();
            assertTypeIsAbsent("MyVehicle");
            assertValidationError("{myVehicle {key}}", "Field 'myVehicle' in type 'Query' is undefined");
        });
        assertThat(fieldNames("MyVehicle")).contains("myNumber");
    }

    @Test
    public void users_can_define_enhanced_entity_types_with_the_same_name_independently() {
        enhance("MyType", "TgVehicle", props("myNumber", "2 * 3"));
        runAs(OTHER_USER, () -> enhance("MyType", "TgVehicleModel", props("otherNumber", "4")));

        assertThat(execute("{myType {key(order: ASC_1) myNumber}}"))
                .isEqualTo(result(Map.of("myType", List.of(Map.of("key", "CAR1", "myNumber", 6), Map.of("key", "CAR2", "myNumber", 6)))));
        runAs(OTHER_USER, () -> assertThat(execute("{myType {key(order: ASC_1) otherNumber}}"))
                .isEqualTo(result(Map.of("myType", List.of(Map.of("key", "005", "otherNumber", 4), Map.of("key", "316", "otherNumber", 4))))));
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Rejected requests
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @Test
    public void names_of_enhanced_entity_types_must_be_valid_Java_identifiers_and_GraphQL_names() {
        for (final var name : List.of("1Vehicle", "class", "My$Vehicle", "Véhicule", "__Vehicle", "My Vehicle", "")) {
            assertRejected(() -> enhance(name, "TgVehicle", props("myNumber", "2 * 3")), ERR_INVALID_NAME.formatted(name));
            assertThat(webApi.findEnhancedEntityType(name)).isEmpty();
        }
        assertRejected(() -> enhance(null, "TgVehicle", props("myNumber", "2 * 3")), ERR_INVALID_NAME.formatted((Object) null));
    }

    @Test
    public void names_of_added_properties_must_be_valid_Java_identifiers_and_GraphQL_names() {
        for (final var name : List.of("1prop", "new", "my$prop", "propé", "__prop")) {
            assertRejected(() -> enhance("MyVehicle", "TgVehicle", props(name, "2 * 3")), ERR_INVALID_NAME.formatted(name));
            assertThat(webApi.findEnhancedEntityType("MyVehicle")).isEmpty();
        }
    }

    @Test
    public void a_new_enhanced_entity_type_requires_a_base_type() {
        assertRejected(() -> enhance("MyVehicle", null, props("myNumber", "2 * 3")), ERR_BASE_TYPE_REQUIRED.formatted("MyVehicle"));
        assertNotDefined("MyVehicle");
    }

    @Test
    public void the_base_type_of_an_existing_enhanced_entity_type_cannot_be_specified() {
        final var existing = enhance("MyVehicle", "TgVehicle", props("myNumber", "2 * 3")).entityType();

        assertRejected(() -> enhance("MyVehicle", "TgVehicle", props("otherNumber", "4")), ERR_BASE_TYPE_OF_EXISTING_TYPE.formatted("MyVehicle"));
        assertThat(webApi.findEnhancedEntityType("MyVehicle")).contains(existing);
    }

    @Test
    public void a_base_type_must_be_a_queryable_domain_entity_type() {
        enhance("MyVehicle", "TgVehicle", props("myNumber", "2 * 3"));

        for (final var baseType : List.of("NoSuchType", "TgUnion", "MyVehicle", "TgVehicle_Agg", "String")) {
            assertRejected(() -> enhance("OtherVehicle", baseType, props()), ERR_NO_SUCH_BASE_TYPE.formatted(baseType));
            assertNotDefined("OtherVehicle");
        }
    }

    @Test
    public void a_base_type_must_be_a_type_whose_model_the_user_can_read() {
        runWithoutToken(TgVehicleModel_CanReadModel_Token.class, () -> {
            assertRejected(() -> enhance("MyModel", "TgVehicleModel", props()), ERR_NO_SUCH_BASE_TYPE.formatted("TgVehicleModel"));
            assertNotDefined("MyModel");
        });
        // A positive control: the same request succeeds when the model can be read.
        enhance("MyModel", "TgVehicleModel", props());
    }

    @Test
    public void the_name_of_a_new_enhanced_entity_type_cannot_be_that_of_an_existing_type() {
        for (final var name : List.of("TgVehicleModel", "TgVehicle_Agg", "TgVehicle_Cond", "_EntityType", "Money", "Query")) {
            assertRejected(() -> enhance(name, "TgVehicle", props()), ERR_NAME_TAKEN_BY_TYPE.formatted(name, name));
        }
    }

    @Test
    public void the_name_of_a_new_enhanced_entity_type_cannot_produce_names_of_existing_types() {
        enhance("X_Agg", "TgVehicle", props());

        assertRejected(() -> enhance("X", "TgVehicle", props()), ERR_NAME_TAKEN_BY_TYPE.formatted("X", "X_Agg"));
        assertNotDefined("X");
    }

    @Test
    public void the_name_of_a_new_enhanced_entity_type_cannot_produce_names_of_existing_root_fields() {
        assertRejected(() -> enhance("_entityType", "TgVehicle", props()), ERR_NAME_TAKEN_BY_ROOT_FIELD.formatted("_entityType", "_entityType"));
        assertNotDefined("_entityType");
    }

    @Test
    public void added_properties_must_have_distinct_names() {
        assertRejected(() -> enhance("MyVehicle", "TgVehicle", props("myNumber", "2 * 3", "myNumber", "4")), ERR_DUPLICATE_PROPERTY.formatted("myNumber"));
        assertNotDefined("MyVehicle");
    }

    @Test
    public void a_property_of_the_base_type_cannot_be_added() {
        assertRejected(() -> enhance("MyVehicle", "TgVehicle", props("price", "2 * 3")), ERR_ADDED_PROPERTY_OF_BASE_TYPE.formatted("price", "TgVehicle"));
        assertNotDefined("MyVehicle");

        final var existing = enhance("MyVehicle", "TgVehicle", props()).entityType();
        assertRejected(() -> enhance("MyVehicle", null, props("key", "2 * 3")), ERR_ADDED_PROPERTY_OF_BASE_TYPE.formatted("key", "TgVehicle"));
        assertThat(webApi.findEnhancedEntityType("MyVehicle")).contains(existing);
    }

    @Test
    public void a_property_of_an_existing_enhanced_entity_type_cannot_be_added() {
        final var existing = enhance("MyVehicle", "TgVehicle", props("myNumber", "2 * 3")).entityType();

        assertRejected(() -> enhance("MyVehicle", null, props("myNumber", "4")), ERR_ADDED_PROPERTY_EXISTS.formatted("myNumber", "MyVehicle"));
        assertThat(webApi.findEnhancedEntityType("MyVehicle")).contains(existing);
    }

    @Test
    public void a_property_of_the_base_type_cannot_be_removed() {
        final var existing = enhance("MyVehicle", "TgVehicle", props("myNumber", "2 * 3")).entityType();

        assertRejected(() -> enhance("MyVehicle", null, props(), List.of("price")), ERR_REMOVED_PROPERTY_OF_BASE_TYPE.formatted("price", "TgVehicle"));
        assertThat(webApi.findEnhancedEntityType("MyVehicle")).contains(existing);
        assertThat(fieldNames("MyVehicle")).contains("price");
    }

    @Test
    public void a_property_cannot_be_both_added_and_removed() {
        final var existing = enhance("MyVehicle", "TgVehicle", props("myNumber", "2 * 3")).entityType();

        assertRejected(() -> enhance("MyVehicle", null, props("otherNumber", "4"), List.of("otherNumber")), ERR_PROPERTY_ADDED_AND_REMOVED.formatted("otherNumber"));
        assertThat(webApi.findEnhancedEntityType("MyVehicle")).contains(existing);
    }

    @Test
    public void a_property_with_an_invalid_expression_cannot_be_added_and_the_request_takes_no_effect() {
        assertThatThrownBy(() -> enhance("MyVehicle", "TgVehicle", props("myNumber", "2 * 3", "badNumber", "2 * noSuchProp")))
                .hasMessageStartingWith("Property [badNumber] cannot be added.");
        assertNotDefined("MyVehicle");

        final var existing = enhance("MyVehicle", "TgVehicle", props("myNumber", "2 * 3")).entityType();
        assertThatThrownBy(() -> enhance("MyVehicle", null, props("otherNumber", "4", "badNumber", "2 *"), List.of("myNumber")))
                .hasMessageStartingWith("Property [badNumber] cannot be added.");
        assertThat(webApi.findEnhancedEntityType("MyVehicle")).contains(existing);
        assertThat(fieldNames("MyVehicle")).contains("myNumber").doesNotContain("otherNumber");
    }

    @Test
    public void a_property_that_another_added_property_refers_to_cannot_be_removed() {
        final var existing = enhance("MyVehicle", "TgVehicle", props("a", "2 * 3", "b", "a + 1")).entityType();

        assertThatThrownBy(() -> enhance("MyVehicle", null, props(), List.of("a")))
                .isInstanceOf(WebApiException.class)
                .hasMessageStartingWith("Property [b] cannot be added.");
        assertThat(webApi.findEnhancedEntityType("MyVehicle")).contains(existing);
        // Removing both properties at once is permitted.
        assertThat(enhance("MyVehicle", null, props(), List.of("a", "b")).entityType().properties()).isEmpty();
    }

    @Test
    public void reading_an_enhanced_entity_type_is_authorised_as_reading_its_base_type() {
        enhance("MyModel", "TgVehicleModel", props("myNumber", "2 * 3"));
        final var query = "{myModel {key(order: ASC_1)}}";
        final var aggQuery = "{myModel_agg {count}}";

        runWithoutToken(TgVehicleModel_CanRead_Token.class, () -> {
            assertThat(errors(execute(query))).singleElement().asString()
                    .contains("Exception while fetching data (/myModel) : Permission denied due to token [Tg Vehicle Model Can Read] restriction.");
            assertThat(errors(execute(aggQuery))).singleElement().asString()
                    .contains("Exception while fetching data (/myModel_agg) : Permission denied due to token [Tg Vehicle Model Can Read] restriction.");
        });
        // A positive control: the same queries succeed when the token is held.
        assertThat(execute(query)).isEqualTo(result(Map.of("myModel", List.of(Map.of("key", "005"), Map.of("key", "316")))));
        assertThat(execute(aggQuery)).isEqualTo(result(Map.of("myModel_agg", List.of(Map.of("count", 2)))));
    }

    @Test
    public void the_model_of_an_enhanced_entity_type_is_visible_only_if_the_model_of_its_base_type_is() {
        enhance("MyModel", "TgVehicleModel", props("myNumber", "2 * 3"));
        final var entityTypeQuery = "{_entityType(eq: \"MyModel\") {name}}";

        runWithoutToken(TgVehicleModel_CanReadModel_Token.class, () -> {
            assertThat(fieldNames("MyModel")).containsExactly("id");
            assertThat(inputFieldNames("MyModel_Cond_cond")).containsExactly("id");
            assertThat(execute(entityTypeQuery)).isEqualTo(result(Map.of("_entityType", List.of())));
        });
        // A positive control: the model is visible when the token is held.
        assertThat(fieldNames("MyModel")).contains("key", "make", "myNumber");
        assertThat(inputFieldNames("MyModel_Cond_cond")).contains("key", "make", "myNumber");
        assertThat(execute(entityTypeQuery)).isEqualTo(result(Map.of("_entityType", List.of(Map.of("name", "MyModel")))));
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Authorisation of expressions
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @Test
    public void an_expression_may_refer_to_properties_that_the_user_can_read() {
        for (final var expression : List.of("2 * price.amount", "2 * replacedBy.price.amount", "model", "model.make", "replacedBy.model.make",
                                             "model.makeModelsCount + price.amount")) {
            enhance("MyVehicle", "TgVehicle", props("myProp", expression));
            webApi.discardEnhancedEntityTypes();
        }
    }

    @Test
    public void an_expression_cannot_refer_to_properties_that_the_user_cannot_read() {
        assertUnauthorised("2 * price.amount", List.of(TgVehicle_CanRead_price_Token.class), "price");
        assertUnauthorised("2 * replacedBy.price.amount", List.of(TgVehicle_CanRead_price_Token.class), "replacedBy.price");
        assertUnauthorised("2 * replacedBy.price.amount + price.amount", List.of(TgVehicle_CanRead_price_Token.class), "price, replacedBy.price");
        assertUnauthorised("model", List.of(TgVehicle_CanRead_model_Token.class), "model");
        assertUnauthorised("model.make", List.of(TgVehicle_CanRead_model_Token.class), "model");
        assertUnauthorised("replacedBy.model.make", List.of(TgVehicle_CanRead_model_Token.class), "replacedBy.model");
        assertUnauthorised("model.makeModelsCount + price.amount", List.of(TgVehicle_CanRead_model_Token.class, TgVehicle_CanRead_price_Token.class), "model, price");
    }

    @Test
    public void an_expression_cannot_refer_to_properties_of_a_type_whose_model_the_user_cannot_read() {
        assertUnauthorised("model.make", List.of(TgVehicleModel_CanReadModel_Token.class), "model.make");
        assertUnauthorised("replacedBy.model.make", List.of(TgVehicleModel_CanReadModel_Token.class), "replacedBy.model.make");
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Utilities
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    private Enhancement enhance(final String name, final String baseType, final List<PropertyDefinition> addProperties, final List<String> removeProperties) {
        return webApi.enhanceEntityType(name, baseType, addProperties, removeProperties);
    }

    private Enhancement enhance(final String name, final String baseType, final List<PropertyDefinition> addProperties) {
        return enhance(name, baseType, addProperties, List.of());
    }

    /// Property definitions from alternating names and expressions.
    ///
    private static List<PropertyDefinition> props(final String... namesAndExpressions) {
        final var props = new ArrayList<PropertyDefinition>();
        for (int i = 0; i < namesAndExpressions.length; i += 2) {
            props.add(new PropertyDefinition(namesAndExpressions[i], namesAndExpressions[i + 1]));
        }
        return props;
    }

    private Map<String, Object> execute(final String query) {
        return webApi.execute(input(query));
    }

    private void assertRejected(final Runnable request, final String message) {
        assertThatThrownBy(request::run).isInstanceOf(WebApiException.class).hasMessage(message);
    }

    /// Asserts that a request to add calculated property `myProp` with `expression` to a new enhanced entity type is rejected
    /// while `tokens` are withdrawn, and accepted otherwise.
    ///
    private void assertUnauthorised(
            final String expression,
            final List<Class<? extends ISecurityToken>> tokens,
            final String unauthorisedPaths)
    {
        runWithoutTokens(tokens, () -> {
            assertRejected(() -> enhance("MyVehicle", "TgVehicle", props("myProp", expression)),
                           ERR_UNAUTHORISED_ACCESS.formatted("myProp", unauthorisedPaths));
            assertNotDefined("MyVehicle");
        });
        // A positive control: the same request succeeds when the tokens are held.
        enhance("MyVehicle", "TgVehicle", props("myProp", expression));
        webApi.discardEnhancedEntityTypes();
    }

    private void assertValidationError(final String query, final String message) {
        assertThat(errors(execute(query))).singleElement().asString().contains("Validation error").contains(message);
    }

    private void assertNotDefined(final String name) {
        assertThat(webApi.findEnhancedEntityType(name)).isEmpty();
        assertTypeIsAbsent(name);
    }

    private void assertTypeIsAbsent(final String typeName) {
        final var result = execute("{__type(name: \"%s\") {name}}".formatted(typeName));
        assertThat(errors(result)).isEmpty();
        assertThat(data(result)).containsEntry("__type", null);
    }

    private List<String> fieldNames(final String typeName) {
        return namesOf(typeName, "fields");
    }

    private List<String> inputFieldNames(final String typeName) {
        return namesOf(typeName, "inputFields");
    }

    private List<String> namesOf(final String typeName, final String fieldsKind) {
        final var result = execute("{__type(name: \"%s\") {%s {name}}}".formatted(typeName, fieldsKind));
        assertThat(errors(result)).isEmpty();
        final var type = (Map<String, Object>) data(result).get("__type");
        assertThat(type).as("Type [%s] exists.", typeName).isNotNull();
        return ((List<Map<String, Object>>) type.get(fieldsKind)).stream().map(field -> (String) field.get("name")).toList();
    }

    private static List<String> propertyNames(final Map<String, Object> entityType) {
        return ((List<Map<String, Object>>) entityType.get("properties")).stream().map(property -> (String) property.get("name")).toList();
    }

    private void runAs(final String username, final Runnable runnable) {
        final var userProvider = getInstance(IUserProvider.class);
        final var currentUser = userProvider.getUser();
        userProvider.setUsername(username, co(User.class));
        try {
            runnable.run();
        } finally {
            userProvider.setUser(currentUser);
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

        final var otherUser = save(new_(User.class, OTHER_USER).setBase(true).setEmail(OTHER_USER + "@unit-test.software").setActive(true));
        save(new_composite(UserAndRoleAssociation.class, otherUser, co(UserRole.class).findByKey(UNIT_TEST_ROLE)));

        final var merc = save(new_(TgVehicleMake.class, "MERC", "Mercedes"));
        final var audi = save(new_(TgVehicleMake.class, "AUDI", "Audi"));

        final var merc316 = save(new_(TgVehicleModel.class, "316", "316").setMake(merc));
        final var audi005 = save(new_(TgVehicleModel.class, "005", "005").setMake(audi));

        save(new_(TgVehicle.class, "CAR1", "CAR1 DESC").setModel(audi005).setPrice(new Money("500")).setActive(true));
        save(new_(TgVehicle.class, "CAR2", "CAR2 DESC").setModel(merc316).setPrice(new Money("450")).setActive(true));
    }

}
