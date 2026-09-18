package ua.com.fielden.platform.web_api;

import org.junit.Test;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static ua.com.fielden.platform.web_api.WebApiUtils.*;

/// Test for the domain meta-schema, which [EntityTypeIntrospection] exposes through the `_entityType` root field.
///
public class WebApiEntityTypeIntrospectionTest extends AbstractDaoTestCase {

    private static final String ALL_FIELDS =
            "{_entityType{name rootField title kind keyType keyMembers keySeparator hasDesc " +
            "properties{name type typeKind collectional arguments required}}}";

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
    // : Utilities
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> queryAllTypes() {
        final var result = webApi.execute(input(ALL_FIELDS));
        assertTrue(errors(result).toString(), errors(result).isEmpty());
        return (List<Map<String, Object>>) data(result).get(EntityTypeIntrospection.ENTITY_TYPE_ROOT_FIELD_NAME);
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
