package ua.com.fielden.platform.web_api;

import org.junit.Test;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.sample.domain.TgWebApiEntity;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static ua.com.fielden.platform.web_api.WebApiUtils.*;

/// Test for aliases in data queries (root field `e` for each queryable entity type `E`) and aggregation queries (root field `e_agg`).
///
/// A root field can be selected several times under different aliases, each time with its own arguments and sub-selection.
/// In sub-selections, a field can be renamed with an alias, but selecting the same field more than once, with or without aliases, is an error.
///
/// The test data comprises four instances of [TgWebApiEntity]:
///
/// | key  | desc      | model | make | intProp | longProp | active |
/// |------|-----------|-------|------|---------|----------|--------|
/// | VEH1 | veh1 desc | 316   | MERC | 1       | 10       | true   |
/// | VEH2 | veh2 desc | 316   | MERC | 2       | 20       | false  |
/// | VEH3 | veh3 desc | A4    | AUDI | 5       | 50       | true   |
/// | VEH4 | veh4 desc |       |      |         |          | false  |
///
public class WebApiAliasesTest extends AbstractDaoTestCase {

    private static final String
            ROOT = "tgWebApiEntity",
            AGG_ROOT = "tgWebApiEntity_agg",
            WHERE_VEH1 = "{cond: {key: {eq: \"VEH1\"}}}",
            WHERE_MERC = "{cond: {model: {cond: {make: {cond: {key: {eq: \"MERC\"}}}}}}}",
            WHERE_AUDI = "{cond: {model: {cond: {make: {cond: {key: {eq: \"AUDI\"}}}}}}}";

    private final IWebApi webApi = getInstance(IWebApi.class);

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Data queries
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @Test
    public void aliased_root_field_is_returned_under_its_alias() {
        final var data = query("{ vehicles: %s { key } }".formatted(ROOT));

        assertThat(data).containsOnlyKeys("vehicles");
        assertThat(listAt(data, "vehicles")).extracting(row -> row.get("key"))
                .containsExactlyInAnyOrder("VEH1", "VEH2", "VEH3", "VEH4");
    }

    @Test
    public void aliased_root_fields_with_different_conditions_are_computed_independently() {
        final var data = query("""
          { merc: %1$s(where: %2$s) { key }
            audi: %1$s(where: %3$s) { key } }
          """.formatted(ROOT, WHERE_MERC, WHERE_AUDI));

        assertThat(listAt(data, "merc")).extracting(row -> row.get("key")).containsExactlyInAnyOrder("VEH1", "VEH2");
        assertThat(listAt(data, "audi")).extracting(row -> row.get("key")).containsExactly("VEH3");
    }

    @Test
    public void aliased_root_fields_with_different_ordering_and_pagination_are_computed_independently() {
        final var data = query("""
          { first: %1$s(order: ASC_1, pageCapacity: 1) { key }
            last: %1$s(order: DESC_1, pageCapacity: 1) { key }
            all: %1$s(order: ASC_1) { key } }
          """.formatted(ROOT));

        assertThat(listAt(data, "first")).extracting(row -> row.get("key")).containsExactly("VEH1");
        assertThat(listAt(data, "last")).extracting(row -> row.get("key")).containsExactly("VEH4");
        assertThat(listAt(data, "all")).extracting(row -> row.get("key")).containsExactly("VEH1", "VEH2", "VEH3", "VEH4");
    }

    @Test
    public void aliased_root_fields_with_different_selections_are_computed_independently() {
        final var data = query("""
          { keys: %1$s(where: %2$s) { key }
            models: %1$s(where: %2$s) { model { key make { key } } } }
          """.formatted(ROOT, WHERE_VEH1));

        assertThat(listAt(data, "keys")).containsExactly(Map.of("key", "VEH1"));
        assertThat(listAt(data, "models")).containsExactly(Map.of("model", Map.of("key", "316", "make", Map.of("key", "MERC"))));
    }

    @Test
    public void aliased_properties_are_returned_under_their_aliases_at_every_level() {
        final var row = single(query("{ %s(where: %s) { k: key m: model { mk: key mm: make { mmk: key } } } }".formatted(ROOT, WHERE_VEH1)));

        assertThat(row).containsOnlyKeys("k", "m");
        assertThat(row.get("k")).isEqualTo("VEH1");
        assertThat(at(row, "m.mk")).isEqualTo("316");
        assertThat(at(row, "m.mm.mmk")).isEqualTo("MERC");
    }

    @Test
    public void aliases_may_coincide_with_names_of_other_properties() {
        final var row = single(query("{ %s(where: %s) { key: desc desc: key } }".formatted(ROOT, WHERE_VEH1)));

        assertThat(row).containsOnly(entry("key", "veh1 desc"), entry("desc", "VEH1"));
    }

    @Test
    public void aliases_can_be_used_in_fragments() {
        final var row = single(query("""
          { %s(where: %s) { ...F ... on TgWebApiEntity { m: model { key } } } }
          fragment F on TgWebApiEntity { k: key }
          """.formatted(ROOT, WHERE_VEH1)));

        assertThat(row).containsOnly(entry("k", "VEH1"), entry("m", Map.of("key", "316")));
    }

    @Test
    public void the_same_property_selected_twice_under_different_aliases_is_an_error() {
        assertDuplicateFieldError("{ %s { a: key b: key } }".formatted(ROOT), "key");
    }

    @Test
    public void the_same_property_selected_twice_without_aliases_is_an_error() {
        assertDuplicateFieldError("{ %s { key key } }".formatted(ROOT), "key");
    }

    @Test
    public void the_same_property_selected_twice_with_different_orderings_is_an_error() {
        assertDuplicateFieldError("{ %s { a: key(order: ASC_1) b: key(order: DESC_1) } }".formatted(ROOT), "key");
    }

    @Test
    public void the_same_entity_typed_property_selected_twice_with_different_sub_selections_is_an_error() {
        assertDuplicateFieldError("{ %s { a: model { key } b: model { desc } } }".formatted(ROOT), "model");
    }

    @Test
    public void duplicate_fields_in_a_nested_sub_selection_are_an_error() {
        assertDuplicateFieldError("{ vehicles: %s { model { make { a: key b: key } } } }".formatted(ROOT), "key");
    }

    @Test
    public void the_same_property_selected_directly_and_through_a_fragment_is_an_error() {
        assertDuplicateFieldError("""
          { %s { key ...F } }
          fragment F on TgWebApiEntity { key }
          """.formatted(ROOT), "key");
    }

    @Test
    public void the_same_property_selected_directly_and_through_an_inline_fragment_is_an_error() {
        assertDuplicateFieldError("{ %s { model { key } ... on TgWebApiEntity { model { desc } } } }".formatted(ROOT), "model");
    }

    @Test
    public void error_for_duplicate_fields_names_their_aliases() {
        final var result = webApi.execute(input("{ %s { a: key b: key } }".formatted(ROOT)));

        assertThat(errorMaps(result)).singleElement()
                .satisfies(err -> assertThat((String) err.get("message")).contains("Duplicate field [key] under aliases: a, b."));
    }

    @Test
    public void duplicate_fields_fail_only_the_root_field_that_contains_them() {
        final var result = webApi.execute(input("{ valid: %1$s(where: %2$s) { key } invalid: %1$s { a: key b: key } }".formatted(ROOT, WHERE_VEH1)));

        assertThat(errorMaps(result)).singleElement()
                .satisfies(err -> assertThat(err.get("path")).isEqualTo(List.of("invalid")));
        assertThat(listAt(data(result), "valid")).containsExactly(Map.of("key", "VEH1"));
        assertThat(data(result).get("invalid")).isNull();
    }

    @Test
    public void duplicate_fields_are_permitted_outside_data_and_aggregation_queries() {
        final var data = query("{ __type(name: \"TgWebApiEntity\") { a: name b: name } }");

        assertThat(at(data, "__type.a")).isEqualTo("TgWebApiEntity");
        assertThat(at(data, "__type.b")).isEqualTo("TgWebApiEntity");
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Aggregation queries
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @Test
    public void aliased_aggregation_root_field_is_returned_under_its_alias() {
        final var data = query("{ stats: %s { count } }".formatted(AGG_ROOT));

        assertThat(data).containsOnlyKeys("stats");
        assertThat(listAt(data, "stats")).containsExactly(Map.of("count", 4));
    }

    @Test
    public void aliased_aggregation_root_fields_with_different_conditions_are_computed_independently() {
        final var data = query("""
          { merc: %1$s(where: %2$s) { count sum { intProp } }
            audi: %1$s(where: %3$s) { count sum { intProp } } }
          """.formatted(AGG_ROOT, WHERE_MERC, WHERE_AUDI));

        assertThat(single(data, "merc").get("count")).isEqualTo(2);
        assertThat(((Number) at(single(data, "merc"), "sum.intProp")).intValue()).isEqualTo(3);
        assertThat(single(data, "audi").get("count")).isEqualTo(1);
        assertThat(((Number) at(single(data, "audi"), "sum.intProp")).intValue()).isEqualTo(5);
    }

    @Test
    public void data_and_aggregation_root_fields_can_be_aliased_in_one_query() {
        final var data = query("""
          { vehicles: %s(where: %s) { key }
            stats: %s(where: %s) { count } }
          """.formatted(ROOT, WHERE_MERC, AGG_ROOT, WHERE_MERC));

        assertThat(listAt(data, "vehicles")).extracting(row -> row.get("key")).containsExactlyInAnyOrder("VEH1", "VEH2");
        assertThat(listAt(data, "stats")).containsExactly(Map.of("count", 2));
    }

    @Test
    public void aliased_groupBy_and_its_aliased_properties_are_returned_under_their_aliases() {
        final var rows = listAt(query("{ %s { g: groupBy { m: model { mk: key } } n: count } }".formatted(AGG_ROOT)), AGG_ROOT);

        assertThat(rows).allSatisfy(row -> assertThat(row).containsOnlyKeys("g", "n"));
        assertThat(rows).extracting(row -> at(row, "g.m.mk"), row -> row.get("n"))
                .containsExactlyInAnyOrder(tuple("316", 2), tuple("A4", 1), tuple(null, 1));
    }

    @Test
    public void aliased_aggregates_and_their_aliased_properties_are_returned_under_their_aliases() {
        final var row = single(query("""
          { %s { total: sum { i: intProp l: longProp }
                 average: avg { i: intProp }
                 lowest: min { i: intProp k: key }
                 highest: max { i: intProp k: key } } }
          """.formatted(AGG_ROOT)), AGG_ROOT);

        assertThat(row).containsOnlyKeys("total", "average", "lowest", "highest");
        assertThat(((Number) at(row, "total.i")).intValue()).isEqualTo(8);
        assertThat(((Number) at(row, "total.l")).longValue()).isEqualTo(80L);
        assertThat(at(row, "average.i")).isIn(2, 3);
        assertThat(at(row, "lowest.i")).isEqualTo(1);
        assertThat(at(row, "lowest.k")).isEqualTo("VEH1");
        assertThat(at(row, "highest.i")).isEqualTo(5);
        assertThat(at(row, "highest.k")).isEqualTo("VEH4");
    }

    @Test
    public void aliases_of_aggregates_may_coincide_with_names_of_other_aggregates() {
        final var row = single(query("{ %s { min: max { intProp } max: min { intProp } } }".formatted(AGG_ROOT)), AGG_ROOT);

        assertThat(at(row, "min.intProp")).isEqualTo(5);
        assertThat(at(row, "max.intProp")).isEqualTo(1);
    }

    @Test
    public void count_selected_twice_under_different_aliases_is_an_error() {
        assertDuplicateFieldError("{ %s { a: count b: count } }".formatted(AGG_ROOT), "count");
    }

    @Test
    public void the_same_aggregate_selected_twice_under_different_aliases_is_an_error() {
        assertDuplicateFieldError("{ %s { a: sum { intProp } b: sum { longProp } } }".formatted(AGG_ROOT), "sum");
    }

    @Test
    public void groupBy_selected_twice_under_different_aliases_is_an_error() {
        assertDuplicateFieldError("{ %s { a: groupBy { model { key } } b: groupBy { active } count } }".formatted(AGG_ROOT), "groupBy");
    }

    @Test
    public void the_same_property_aggregated_twice_under_different_aliases_is_an_error() {
        assertDuplicateFieldError("{ %s { sum { a: intProp b: intProp } } }".formatted(AGG_ROOT), "intProp");
    }

    @Test
    public void the_same_property_grouped_by_twice_under_different_aliases_is_an_error() {
        assertDuplicateFieldError("{ %s { groupBy { a: model { key } b: model { desc } } count } }".formatted(AGG_ROOT), "model");
    }

    @Test
    public void the_same_aggregate_selected_directly_and_through_a_fragment_is_an_error() {
        assertDuplicateFieldError("""
          { stats: %s { count ...F } }
          fragment F on TgWebApiEntity_Agg { count }
          """.formatted(AGG_ROOT), "count");
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
        final var m316 = save(new_(TgVehicleModel.class, "316", "316 desc").setMake(merc));
        final var a4 = save(new_(TgVehicleModel.class, "A4", "A4 desc").setMake(audi));

        save(new_(TgWebApiEntity.class, "VEH1", "veh1 desc").setModel(m316).setIntProp(1).setLongProp(10L).setActive(true));
        save(new_(TgWebApiEntity.class, "VEH2", "veh2 desc").setModel(m316).setIntProp(2).setLongProp(20L).setActive(false));
        save(new_(TgWebApiEntity.class, "VEH3", "veh3 desc").setModel(a4).setIntProp(5).setLongProp(50L).setActive(true));
        save(new_(TgWebApiEntity.class, "VEH4", "veh4 desc").setActive(false));
    }
    
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Utilities
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    private Map<String, Object> query(final String query) {
        final var result = webApi.execute(input(query));
        final var errors = errors(result);
        assertThat(errors).as(errors.toString()).isEmpty();
        return data(result);
    }

    /// Asserts that `query` results in a single error for the duplicate `field`.
    ///
    private void assertDuplicateFieldError(final String query, final String field) {
        final var result = webApi.execute(input(query));

        assertThat(errorMaps(result)).singleElement()
                .satisfies(err -> assertThat((String) err.get("message")).contains("Duplicate field [%s]".formatted(field)));
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> errorMaps(final Map<String, Object> result) {
        return errors(result).stream().map(err -> (Map<String, Object>) err).toList();
    }

    /// The only row under the data root field.
    ///
    private static Map<String, Object> single(final Map<String, Object> data) {
        return single(data, ROOT);
    }

    /// The only row under the root field with the specified response key.
    ///
    private static Map<String, Object> single(final Map<String, Object> data, final String responseKey) {
        final var rows = listAt(data, responseKey);
        assertThat(rows).hasSize(1);
        return rows.getFirst();
    }

    /// Follows a dot-notated `path` through nested maps, returning `null` if any value along the path is `null`.
    ///
    @SuppressWarnings("unchecked")
    private static Object at(final Map<String, Object> map, final String path) {
        Object current = map;
        for (final var name : path.split("\\.")) {
            if (current == null) {
                return null;
            }
            current = ((Map<String, Object>) current).get(name);
        }
        return current;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> listAt(final Map<String, Object> map, final String path) {
        return (List<Map<String, Object>>) at(map, path);
    }

}
