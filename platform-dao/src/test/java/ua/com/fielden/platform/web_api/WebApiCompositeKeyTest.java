package ua.com.fielden.platform.web_api;

import org.junit.Test;
import ua.com.fielden.platform.sample.domain.TgAuthor;
import ua.com.fielden.platform.sample.domain.TgAuthorship;
import ua.com.fielden.platform.sample.domain.TgPersonName;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static ua.com.fielden.platform.web_api.WebApiUtils.*;

/// Test for selection of a composite key through the GraphQL Web API.
///
/// A composite key reaches the schema as a single `String`-typed field, so it admits the same `eq`, `like` and `order` arguments
/// as any other string, and those are exercised here both on a root type and on an entity-typed property.
///
public class WebApiCompositeKeyTest extends AbstractDaoTestCase {

    /// `JOHN SMITH` has no patronymic, whereas `JANE DOE X` has one, so that the concatenation of an optional key member is covered.
    ///
    private static final String
            JOHN = "JOHN SMITH",
            JANE = "JANE DOE X",
            AUTHORSHIP = "JOHN SMITH First Book";

    private final IWebApi webApi = getInstance(IWebApi.class);

    // : Selection

    @Test
    public void a_composite_key_is_selectable_as_a_string() {
        assertThat(keysOf("{tgAuthor{key}}", "tgAuthor")).containsExactlyInAnyOrder(JOHN, JANE);
    }

    @Test
    public void a_composite_key_is_selectable_on_an_entity_typed_property() {
        final var rows = rowsOf("{tgAuthorship{key author{key}}}", "tgAuthorship");

        assertThat(rows).hasSize(1);
        assertThat(rows.getFirst().get("key")).isEqualTo(AUTHORSHIP);
        assertThat(((Map<String, Object>) rows.getFirst().get("author")).get("key")).isEqualTo(JOHN);
    }

    // : Argument eq

    @Test
    public void eq_matches_a_composite_key_in_full() {
        assertThat(keysOf("{tgAuthor{key(eq:\"%s\")}}".formatted(JOHN), "tgAuthor")).containsExactly(JOHN);
    }

    @Test
    public void eq_on_a_composite_key_is_case_insensitive() {
        // A composite key is exposed as a string, so it is matched as one -- which for `eq` means case-insensitively, without wildcards.
        assertThat(keysOf("{tgAuthor{key(eq:\"john smith\")}}", "tgAuthor")).containsExactly(JOHN);
    }

    @Test
    public void eq_matches_a_composite_key_of_an_entity_typed_property() {
        assertThat(keysOf("{tgAuthorship{key author(eq:\"%s\"){key}}}".formatted(JOHN), "tgAuthorship")).containsExactly(AUTHORSHIP);
        assertThat(keysOf("{tgAuthorship{key author{key(eq:\"%s\")}}}".formatted(JOHN), "tgAuthorship")).containsExactly(AUTHORSHIP);
    }

    @Test
    public void eq_that_matches_no_composite_key_yields_nothing() {
        assertThat(keysOf("{tgAuthor{key(eq:\"JOHN\")}}", "tgAuthor")).isEmpty();
    }

    // : Argument like

    @Test
    public void like_matches_a_composite_key_by_wildcard() {
        assertThat(keysOf("{tgAuthor{key(like:\"*SMITH*\")}}", "tgAuthor")).containsExactly(JOHN);
    }

    @Test
    public void like_matches_a_composite_key_in_full() {
        assertThat(keysOf("{tgAuthor{key(like:\"%s\")}}".formatted(JOHN), "tgAuthor")).containsExactly(JOHN);
    }

    @Test
    public void like_matches_a_composite_key_against_comma_separated_values() {
        assertThat(keysOf("{tgAuthor{key(like:\"%s,%s\")}}".formatted(JOHN, JANE), "tgAuthor")).containsExactlyInAnyOrder(JOHN, JANE);
    }

    @Test
    public void like_matches_a_composite_key_of_an_entity_typed_property() {
        assertThat(keysOf("{tgAuthorship{key author{key(like:\"%s\")}}}".formatted(JOHN), "tgAuthorship")).containsExactly(AUTHORSHIP);
        assertThat(keysOf("{tgAuthorship{key author{key(like:\"*SMITH*\")}}}", "tgAuthorship")).containsExactly(AUTHORSHIP);
    }

    @Test
    public void like_on_a_composite_key_of_an_entity_typed_property_matches_that_property_and_not_the_root_type() {
        // The key of the only `TgAuthorship` contains `First Book`, the key of its author does not.
        // Were the condition applied to the root type, this would match.
        assertThat(keysOf("{tgAuthorship{key author{key(like:\"*First Book*\")}}}", "tgAuthorship")).isEmpty();
    }

    // : Argument order

    @Test
    public void a_composite_key_is_orderable() {
        assertThat(keysOf("{tgAuthor{key(order:ASC_1)}}", "tgAuthor")).containsExactly(JANE, JOHN);
        assertThat(keysOf("{tgAuthor{key(order:DESC_1)}}", "tgAuthor")).containsExactly(JOHN, JANE);
    }

    // : Agreement with the schema

    @Test
    public void a_composite_key_type_exposes_key_as_its_first_field_alongside_its_key_members() {
        assertThat(schemaFieldsOf("TgAuthor"))
                .startsWith("key")
                .contains("name", "surname", "patronymic");
    }

    @Test
    public void a_simple_key_type_exposes_key_exactly_once() {
        // `key` is a key member of a simple-key type, so it must not be contributed a second time as a composite key would be.
        assertThat(schemaFieldsOf("TgVehicleModel")).containsOnlyOnce("key").startsWith("key");
    }

    // : Utilities

    /// Returns the rows of `rootField`, asserting that the query produced no errors.
    ///
    private List<Map<String, Object>> rowsOf(final String query, final String rootField) {
        final var result = webApi.execute(input(query));
        assertThat(errors(result)).as("Errors for [%s].".formatted(query)).isEmpty();
        return (List<Map<String, Object>>) data(result).get(rootField);
    }

    /// Returns the `key` of every row of `rootField`, in the order the query produced them.
    ///
    private List<String> keysOf(final String query, final String rootField) {
        return rowsOf(query, rootField).stream().map(row -> (String) row.get("key")).toList();
    }

    /// Returns the names of the fields that the schema declares for `typeName`, in the order it declares them.
    ///
    private List<String> schemaFieldsOf(final String typeName) {
        final var result = webApi.execute(input("{__type(name:\"%s\"){fields{name}}}".formatted(typeName)));
        assertThat(errors(result)).as("Errors for type [%s].".formatted(typeName)).isEmpty();
        final var type = (Map<String, Object>) data(result).get("__type");
        assertThat(type).as("Type [%s] is not present in the schema.".formatted(typeName)).isNotNull();
        return ((List<Map<String, Object>>) type.get("fields")).stream().map(field -> (String) field.get("name")).toList();
    }

    @Override
    protected void populateDomain() {
        super.populateDomain();

        if (useSavedDataPopulationScript()) {
            return;
        }

        final var john = save(new_(TgPersonName.class, "JOHN"));
        final var jane = save(new_(TgPersonName.class, "JANE"));
        final var smith = save(new_composite(TgAuthor.class, john, "SMITH", null));
        save(new_composite(TgAuthor.class, jane, "DOE", "X"));
        save(new_composite(TgAuthorship.class, smith, "First Book"));
    }

}
