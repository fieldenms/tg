package ua.com.fielden.platform.web_api;

import org.junit.Test;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.web_api.EntityTypeIntrospection.ENTITY_TYPE_ROOT_FIELD_NAME;
import static ua.com.fielden.platform.web_api.RootEntityUtils.ERR_EQ_AND_LIKE_ARE_MUTUALLY_EXCLUSIVE;
import static ua.com.fielden.platform.web_api.RootEntityUtils.ERR_EQ_DOES_NOT_PERMIT_WILDCARDS;
import static ua.com.fielden.platform.web_api.WebApiUtils.*;

/// Test for the domain meta-schema, which [EntityTypeIntrospection] exposes through the `_entityType` root field.
///
public class WebApiEntityTypeIntrospectionTest extends AbstractDaoTestCase {

    /// Selects every field of `_EntityType` and of `_Property`.
    /// The placeholder takes the arguments of `_entityType`, if any.
    ///
    private static final String ALL_FIELDS_TEMPLATE =
            "{_entityType%s{name rootField title kind keyType keyMembers keySeparator hasDesc " +
            "properties{name type typeKind collectional arguments required}}}";

    private static final String ALL_FIELDS = ALL_FIELDS_TEMPLATE.formatted("");

    private final IWebApi webApi = getInstance(IWebApi.class);

    @Test
    public void a_persistent_type_is_described_with_its_root_field_and_key_shape() {
        final var entityType = getType(queryAllTypes(), "TgWebApiEntity");

        assertEquals("tgWebApiEntity", entityType.get("rootField"));
        assertEquals("PERSISTENT", entityType.get("kind"));
        assertEquals("SIMPLE", entityType.get("keyType"));
        assertEquals(List.of("key"), entityType.get("keyMembers"));
    }

    @Test
    public void an_entity_typed_property_is_described_with_its_type_and_arguments() {
        final var model = getProperty(getType(queryAllTypes(), "TgWebApiEntity"), "model");

        assertEquals("TgVehicleModel", model.get("type"));
        assertEquals("ENTITY", model.get("typeKind"));
        assertEquals(false, model.get("collectional"));
        assertEquals(List.of("eq", "like", "order"), model.get("arguments"));
    }

    @Test
    public void a_value_typed_property_is_described_with_the_arguments_of_its_type() {
        final var properties = getType(queryAllTypes(), "TgWebApiEntity");

        final var key = getProperty(properties, "key");
        assertEquals("String", key.get("type"));
        assertEquals("VALUE", key.get("typeKind"));
        assertEquals(List.of("eq", "like", "order"), key.get("arguments"));
        // Property `key` is required by definition.
        assertEquals(true, key.get("required"));

        final var id = getProperty(properties, "id");
        assertEquals("VALUE", id.get("typeKind"));
        assertEquals(List.of("from", "to", "order"), id.get("arguments"));
    }

    @Test
    public void a_boolean_property_is_required() {
        final var booleanProperties = queryAllTypes().stream()
                .flatMap(ty -> properties(ty).stream())
                .filter(prop -> "Boolean".equals(prop.get("type")))
                .toList();

        assertFalse("The test domain is expected to contain boolean properties.", booleanProperties.isEmpty());
        booleanProperties.forEach(prop -> assertEquals("Boolean property [%s] must be required.".formatted(prop.get("name")),
                                                       true, prop.get("required")));
    }

    @Test
    public void a_collectional_property_is_identified_and_accepts_no_arguments() {
        final var activeRoles = getProperty(getType(queryAllTypes(), "User"), "activeRoles");

        assertEquals(true, activeRoles.get("collectional"));
        assertEquals(List.of(), activeRoles.get("arguments"));
        // For a collectional property, `type` is the type of its elements, which is the type to query as a root field when filtering by the contents of the collection.
        assertEquals("ENTITY", activeRoles.get("typeKind"));
        assertNotNull(activeRoles.get("type"));
    }

    @Test
    public void union_types_are_described_but_are_not_queryable() {
        final var unions = queryAllTypes().stream()
                .filter(ty -> "UNION".equals(ty.get("kind")))
                .toList();

        assertFalse("Union types are reachable as property types and must be described.", unions.isEmpty());
        unions.forEach(union -> {
            assertNull("Union [%s] must not be queryable, so it has no root field.".formatted(union.get("name")), union.get("rootField"));
        });
    }

    @Test
    public void a_type_without_a_key_reports_neither_key_members_nor_a_separator() {
        final var noKeyTypes = queryAllTypes().stream()
                .filter(ty -> "NO_KEY".equals(ty.get("keyType")))
                .toList();

        assertFalse("The test domain is expected to contain types without a key.", noKeyTypes.isEmpty());
        noKeyTypes.forEach(entityType -> {
            final var name = (String) entityType.get("name");
            assertNull("Type [%s] without a key must not report key members.".formatted(name), entityType.get("keyMembers"));
            assertNull("Type [%s] without a key must not report a separator.".formatted(name), entityType.get("keySeparator"));
        });
    }

    @Test
    public void a_composite_key_is_described_through_its_members_rather_than_key() {
        final var composites = queryAllTypes().stream()
                .filter(ty -> "COMPOSITE".equals(ty.get("keyType")))
                .toList();

        assertFalse("The test domain is expected to contain composite-key types.", composites.isEmpty());
        composites.forEach(entityType -> {
            final var name = (String) entityType.get("name");
            @SuppressWarnings("unchecked")
            final var keyMembers = (List<String>) entityType.get("keyMembers");
            assertFalse("Composite key of [%s] must list its members.".formatted(name), keyMembers.isEmpty());
            assertNotNull("Composite key of [%s] must report a separator.".formatted(name), entityType.get("keySeparator"));
            assertNotEquals("Composite key of [%s] must not be reported as a single `key` member.".formatted(name),
                            List.of("key"), keyMembers);
        });
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Argument `eq`
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @Test
    public void eq_matches_a_type_name_exactly() {
        assertThat(queryTypeNames("""
          { _entityType(eq: "TgWebApiEntity") { name } }
          """))
                .containsExactly("TgWebApiEntity");
    }

    @Test
    public void eq_matches_a_type_name_as_variable() {
        final var result = webApi.execute(input("""
          query($val: String) { _entityType(eq: $val) { name } }
          """, Map.of("val", "TgWebApiEntity")));

        assertNoErrors(result);
        assertThat(namesOf(result)).containsExactly("TgWebApiEntity");
    }

    @Test
    public void eq_is_case_sensitive() {
        assertThat(queryTypeNames("""
          { _entityType(eq: "tgwebapientity") { name } }
          """))
                .isEmpty();
    }

    /// The value of `eq` is matched against `name`, which is what a property's `type` refers to, and not against `rootField`.
    ///
    @Test
    public void eq_does_not_match_a_root_field_name() {
        assertThat(queryTypeNames("""
          { _entityType(eq: "tgWebApiEntity") { name } }
          """))
                .isEmpty();
    }

    @Test
    public void eq_treats_surrounding_whitespace_as_part_of_the_value() {
        assertThat(queryTypeNames("""
          { _entityType(eq: " TgWebApiEntity ") { name } }
          """))
                .isEmpty();
    }

    /// A value for `eq` is a single name, so a comma in it is matched literally and therefore matches nothing.
    ///
    @Test
    public void eq_does_not_support_comma_separated_values() {
        assertThat(queryTypeNames("""
          { _entityType(eq: "TgWebApiEntity,TgVehicleModel") { name } }
          """))
                .isEmpty();
    }

    @Test
    public void eq_does_not_permit_wildcards_as_literal() {
        assertSingleError("""
          { _entityType(eq: "TgWebApi*") { name } }
          """, ERR_EQ_DOES_NOT_PERMIT_WILDCARDS);
    }

    @Test
    public void eq_does_not_permit_wildcards_as_variable() {
        final var result = webApi.execute(input("""
          query($val: String) { _entityType(eq: $val) { name } }
          """, Map.of("val", "TgWebApi*")));

        assertSingleError(result, ERR_EQ_DOES_NOT_PERMIT_WILDCARDS);
    }

    @Test
    public void eq_matching_no_type_returns_an_empty_list() {
        assertThat(queryTypeNames("""
          { _entityType(eq: "ThereIsNoSuchEntityType") { name } }
          """))
                .isEmpty();
    }

    @Test
    public void eq_with_a_null_value_imposes_no_restriction() {
        assertThat(queryTypeNames("""
          { _entityType(eq: null) { name } }
          """))
                .isEqualTo(queryAllTypeNames());
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Argument `like`
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @Test
    public void like_matches_exactly_when_no_wildcard_is_used() {
        assertThat(queryTypeNames("""
          { _entityType(like: "TgWebApiEntity") { name } }
          """))
                .containsExactly("TgWebApiEntity");
    }

    @Test
    public void like_supports_a_trailing_wildcard() {
        assertThat(queryTypeNames("""
          { _entityType(like: "TgWebApi*") { name } }
          """))
                .containsExactlyInAnyOrder("TgWebApiEntity", "TgWebApiEntitySyntheticMulti", "TgWebApiEntitySyntheticSingle");
    }

    @Test
    public void like_supports_a_leading_wildcard() {
        assertThat(queryTypeNames("""
          { _entityType(like: "*WebApiEntity") { name } }
          """))
                .containsExactly("TgWebApiEntity");
    }

    @Test
    public void like_supports_a_wildcard_in_the_middle() {
        assertThat(queryTypeNames("""
          { _entityType(like: "TgWebApi*Multi") { name } }
          """))
                .containsExactly("TgWebApiEntitySyntheticMulti");
    }

    @Test
    public void like_supports_comma_separated_values() {
        assertThat(queryTypeNames("""
          { _entityType(like: "TgWebApiEntity,TgVehicleModel") { name } }
          """))
                .containsExactlyInAnyOrder("TgWebApiEntity", "TgVehicleModel");
    }

    @Test
    public void like_supports_comma_separated_values_with_wildcards() {
        assertThat(queryTypeNames("""
          { _entityType(like: "TgWebApiEntitySynthetic*,TgVehicleMake") { name } }
          """))
                .containsExactlyInAnyOrder("TgWebApiEntitySyntheticMulti", "TgWebApiEntitySyntheticSingle", "TgVehicleMake");
    }

    @Test
    public void like_supports_a_value_as_variable() {
        final var result = webApi.execute(input("""
          query($val: String) { _entityType(like: $val) { name } }
          """, Map.of("val", "TgWebApi*")));

        assertNoErrors(result);
        assertThat(namesOf(result))
                .containsExactlyInAnyOrder("TgWebApiEntity", "TgWebApiEntitySyntheticMulti", "TgWebApiEntitySyntheticSingle");
    }

    @Test
    public void like_with_a_lone_wildcard_matches_every_type() {
        assertThat(queryTypeNames("""
          { _entityType(like: "*") { name } }
          """))
                .isEqualTo(queryAllTypeNames());
    }

    @Test
    public void like_with_a_null_value_imposes_no_restriction() {
        assertThat(queryTypeNames("""
          { _entityType(like: null) { name } }
          """))
                .isEqualTo(queryAllTypeNames());
    }

    @Test
    public void like_matching_no_type_returns_an_empty_list() {
        assertThat(queryTypeNames("""
          { _entityType(like: "ThereIsNoSuch*EntityType") { name } }
          """))
                .isEmpty();
    }

    @Test
    public void like_is_case_sensitive_for_an_exact_value() {
        assertThat(queryTypeNames("""
          { _entityType(like: "tgwebapientity") { name } }
          """))
                .isEmpty();
    }

    @Test
    public void like_is_case_sensitive_for_a_wildcard_value() {
        assertThat(queryTypeNames("""
          { _entityType(like: "tgwebapi*") { name } }
          """))
                .isEmpty();
    }

    /// Whitespace is part of a value, so a comma-separated list must be written without spaces after the commas.
    ///
    @Test
    public void like_treats_whitespace_around_a_value_as_part_of_it() {
        assertThat(queryTypeNames("""
          { _entityType(like: "TgWebApiEntity, TgVehicleModel") { name } }
          """))
                .containsExactly("TgWebApiEntity");
    }

    /// A lone wildcard matches everything, so it subsumes any value it is listed with.
    ///
    @Test
    public void like_with_a_lone_wildcard_among_other_values_still_matches_every_type() {
        assertThat(queryTypeNames("""
          { _entityType(like: "*,TgWebApiEntity") { name } }
          """))
                .isEqualTo(queryAllTypeNames());
    }

    /// Only `*` is a wildcard.
    /// Everything else in a `like` value stands for itself, including characters that are meaningful in a regular expression.
    ///
    @Test
    public void like_treats_everything_but_the_wildcard_literally() {
        // `.` matches only a full stop, and no type name contains one.
        assertThat(queryTypeNames("""
          { _entityType(like: "*.ebApiEntity") { name } }
          """))
                .isEmpty();
        // An unbalanced bracket is not an error, it simply matches nothing.
        assertThat(queryTypeNames("""
          { _entityType(like: "TgWebApi*(") { name } }
          """))
                .isEmpty();
        // `%` is an SQL wildcard, but not one here.
        assertThat(queryTypeNames("""
          { _entityType(like: "%WebApiEntity") { name } }
          """))
                .isEmpty();
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Arguments `eq` and `like` together
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @Test
    public void eq_and_like_are_mutually_exclusive_as_literals() {
        assertSingleError("""
          { _entityType(eq: "TgWebApiEntity", like: "TgVehicleModel") { name } }
          """, ERR_EQ_AND_LIKE_ARE_MUTUALLY_EXCLUSIVE);
    }

    @Test
    public void eq_and_like_are_mutually_exclusive_as_variables() {
        final var result = webApi.execute(input("""
          query($eq: String, $like: String) { _entityType(eq: $eq, like: $like) { name } }
          """, Map.of("eq", "TgWebApiEntity", "like", "TgVehicleModel")));

        assertSingleError(result, ERR_EQ_AND_LIKE_ARE_MUTUALLY_EXCLUSIVE);
    }

    /// Arguments `eq` and `like` select types, they do not affect how a selected type is described.
    ///
    @Test
    public void filtering_does_not_change_how_a_type_is_described() {
        final var filtered = queryEntityType(ALL_FIELDS_TEMPLATE.formatted("(eq: \"TgWebApiEntity\")"));

        assertThat(filtered).containsExactly(getType(queryAllTypes(), "TgWebApiEntity"));
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Utilities
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    /// Names of all types described by the meta-schema, in the order in which they are reported.
    ///
    private List<String> queryAllTypeNames() {
        return queryTypeNames("{_entityType{name}}");
    }

    private List<String> queryTypeNames(final String query) {
        return queryEntityType(query).stream().map(ty -> (String) ty.get("name")).toList();
    }

    private List<Map<String, Object>> queryEntityType(final String query) {
        final var result = webApi.execute(input(query));
        assertNoErrors(result);
        return typesOf(result);
    }

    private List<Map<String, Object>> queryAllTypes() {
        return queryEntityType(ALL_FIELDS);
    }

    private void assertSingleError(final String query, final String expectedError) {
        assertSingleError(webApi.execute(input(query)), expectedError);
    }

    private static void assertSingleError(final Map<String, Object> result, final String expectedError) {
        final var errors = errors(result);
        assertThat(errors).hasSize(1);
        assertThat(errors.getFirst().toString()).contains(expectedError);
    }

    private static void assertNoErrors(final Map<String, Object> result) {
        final var errors = errors(result);
        assertTrue(errors.toString(), errors.isEmpty());
    }

    private static List<String> namesOf(final Map<String, Object> result) {
        return typesOf(result).stream().map(ty -> (String) ty.get("name")).toList();
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> typesOf(final Map<String, Object> result) {
        return (List<Map<String, Object>>) data(result).get(ENTITY_TYPE_ROOT_FIELD_NAME);
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> properties(final Map<String, Object> entityType) {
        return (List<Map<String, Object>>) entityType.get("properties");
    }

    private static Map<String, Object> getType(final List<Map<String, Object>> types, final String name) {
        return types.stream()
                .filter(ty -> name.equals(ty.get("name")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Type [%s] is not described.".formatted(name)));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> getProperty(final Map<String, Object> entityType, final String name) {
        return ((List<Map<String, Object>>) entityType.get("properties")).stream()
                .filter(prop -> name.equals(prop.get("name")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Property [%s] of [%s] is not described.".formatted(name, entityType.get("name"))));
    }

}
