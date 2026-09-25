package ua.com.fielden.platform.web_api;

import org.joda.time.DateTime;
import org.junit.Test;
import ua.com.fielden.platform.sample.domain.TgUnion;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.sample.domain.TgWebApiEntity;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.tokens.persistent.TgVehicleModel_CanReadModel_Token;
import ua.com.fielden.platform.security.tokens.persistent.TgVehicleModel_CanRead_Token;
import ua.com.fielden.platform.security.user.SecurityRoleAssociation;
import ua.com.fielden.platform.security.user.SecurityRoleAssociationCo;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.types.Money;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;
import static ua.com.fielden.platform.utils.CollectionUtil.mapOf;
import static ua.com.fielden.platform.web_api.GraphQLCommon.*;
import static ua.com.fielden.platform.web_api.GraphQLScalars.createDateRepr;
import static ua.com.fielden.platform.web_api.WebApiUtils.*;

/// Test for aggregation queries, which [EntityAggregation] exposes through a root field `e_agg` for each queryable entity type `E`.
///
/// The test data comprises four instances of [TgWebApiEntity]:
///
/// | key  | model | make | intProp | longProp | bigDecimalProp | moneyProp | dateProp   | active |
/// |------|-------|------|---------|----------|----------------|-----------|------------|--------|
/// | VEH1 | 316   | MERC | 1       | 10       | 1.5            | 10        | 2025-01-10 | true   |
/// | VEH2 | 316   | MERC | 2       | 20       | 2.5            | 20        | 2025-02-10 | false  |
/// | VEH3 | A4    | AUDI | 5       | 50       | 4.0            | 40        | 2025-03-10 | true   |
/// | VEH4 |       |      |         |          |                |           |            | false  |
///
public class WebApiAggregationTest extends AbstractDaoTestCase {

    private static final String ROOT = "tgWebApiEntity_agg";

    private static final Date
            DATE_1 = new DateTime(2025, 1, 10, 0, 0).toDate(),
            DATE_2 = new DateTime(2025, 2, 10, 0, 0).toDate(),
            DATE_3 = new DateTime(2025, 3, 10, 0, 0).toDate();

    private final IWebApi webApi = getInstance(IWebApi.class);

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Schema
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @Test
    public void aggregation_root_field_and_type_are_named_after_the_entity_type() {
        assertThat(rootFieldNameForAggEntity(TgWebApiEntity.class)).isEqualTo(ROOT);
        assertThat(graphQlTypeNameForAggEntity(TgWebApiEntity.class)).isEqualTo("TgWebApiEntity_Agg");
        assertThat(queryRootFieldNames()).contains("tgWebApiEntity", ROOT);
    }

    @Test
    public void aggregation_type_has_fields_groupBy_count_avg_sum_max_min() {
        final var result = webApi.execute(input("""
          { __type(name: "TgWebApiEntity_Agg") { fields { name type { name kind ofType { name } } } } }
          """));
        assertNoErrors(result);

        final var fields = listAt(data(result), "__type.fields");
        assertThat(fields).extracting(f -> f.get("name"))
                .containsExactlyInAnyOrder("groupBy", "count", "avg", "sum", "max", "min");
        fields.stream().filter(f -> !"count".equals(f.get("name")))
                .forEach(f -> assertThat(at(f, "type.name")).as("Type of [%s].", f.get("name")).isEqualTo(TgWebApiEntity.class.getSimpleName()));
        final var count = fields.stream().filter(f -> "count".equals(f.get("name"))).findFirst().orElseThrow();
        assertThat(at(count, "type.kind")).isEqualTo("NON_NULL");
        assertThat(at(count, "type.ofType.name")).isEqualTo("Int");
    }

    @Test
    public void union_types_have_no_aggregation_root_field() {
        assertThat(rootFieldName(TgUnion.class)).isEqualTo("tgUnion");
        assertThat(rootFieldNameForAggEntity(TgUnion.class)).isEqualTo("tgUnion_agg");
        assertThat(queryRootFieldNames()).doesNotContain(rootFieldName(TgUnion.class), rootFieldNameForAggEntity(TgUnion.class));
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Count
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @Test
    public void without_groupBy_all_records_form_a_single_group() {
        final var rows = queryAgg("{ tgWebApiEntity_agg { count } }");

        assertThat(rows).containsExactly(Map.of("count", 4));
    }

    @Test
    public void count_over_a_type_without_records_is_zero() {
        final var rows = queryAgg("{ tgCategory_agg { count } }", "tgCategory_agg");

        assertThat(rows).containsExactly(Map.of("count", 0));
    }

    @Test
    public void min_and_max_over_a_type_without_records_are_null() {
        final var rows = queryAgg("{ tgCategory_agg { min { key } max { key } } }", "tgCategory_agg");

        assertThat(rows).hasSize(1);
        assertThat(at(rows.getFirst(), "min.key")).isNull();
        assertThat(at(rows.getFirst(), "max.key")).isNull();
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : groupBy
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @Test
    public void count_is_computed_for_each_group_of_an_entity_typed_property() {
        final var rows = queryAgg("{ tgWebApiEntity_agg { groupBy { model { key } } count } }");

        assertThat(rows).hasSize(3);
        assertThat(rowsBy(rows, "groupBy.model.key", "count"))
                .containsExactlyInAnyOrderEntriesOf(mapOf(t2("316", 2), t2("A4", 1), t2(null, 1)));
    }

    @Test
    public void groupBy_can_follow_a_path_of_several_properties() {
        final var rows = queryAgg("{ tgWebApiEntity_agg { groupBy { model { make { key } } } count } }");

        assertThat(rows).hasSize(3);
        assertThat(rowsBy(rows, "groupBy.model.make.key", "count"))
                .containsExactlyInAnyOrderEntriesOf(mapOf(t2("MERC", 2), t2("AUDI", 1), t2(null, 1)));
    }

    @Test
    public void groupBy_with_several_properties_groups_by_their_combination() {
        final var rows = queryAgg("{ tgWebApiEntity_agg { groupBy { model { key } active } count } }");

        assertThat(rows).hasSize(4);
        assertThat(rows).extracting(row -> listOf(listOf(at(row, "groupBy.model.key"), at(row, "groupBy.active")), at(row, "count")))
                .containsExactlyInAnyOrder(
                        listOf(listOf("316", true), 1),
                        listOf(listOf("316", false), 1),
                        listOf(listOf("A4", true), 1),
                        listOf(listOf(null, false), 1));
    }

    @Test
    public void groupBy_without_aggregates_yields_distinct_values() {
        final var rows = queryAgg("{ tgWebApiEntity_agg { groupBy { model { key } } } }");

        assertThat(rows).extracting(row -> at(row, "groupBy.model.key"))
                .containsExactlyInAnyOrder("316", "A4", null);
    }

    @Test
    public void groupBy_a_value_typed_property() {
        final var rows = queryAgg("{ tgWebApiEntity_agg { groupBy { active } count } }");

        assertThat(rowsBy(rows, "groupBy.active", "count"))
                .containsExactlyInAnyOrderEntriesOf(mapOf(t2(true, 2), t2(false, 2)));
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : sum, avg, min, max
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @Test
    public void sum_is_computed_for_numeric_properties_and_ignores_unassigned_values() {
        final var rows = queryAgg("{ tgWebApiEntity_agg { sum { intProp longProp bigDecimalProp moneyProp } } }");

        assertThat(rows).hasSize(1);
        final var sum = rows.getFirst();
        assertThat(((Number) at(sum, "sum.intProp")).intValue()).isEqualTo(8);
        assertThat(((Number) at(sum, "sum.longProp")).longValue()).isEqualTo(80L);
        assertThat((BigDecimal) at(sum, "sum.bigDecimalProp")).isEqualByComparingTo("8");
        assertThat((BigDecimal) at(sum, "sum.moneyProp")).isEqualByComparingTo("70");
    }

    @Test
    public void sum_is_computed_within_each_group() {
        final var rows = queryAgg("{ tgWebApiEntity_agg { groupBy { model { key } } sum { bigDecimalProp moneyProp } } }");

        final var byModel = rowsBy(rows, "groupBy.model.key");
        assertThat(byModel).containsOnlyKeys("316", "A4", null);
        assertThat((BigDecimal) at(byModel.get("316"), "sum.bigDecimalProp")).isEqualByComparingTo("4");
        assertThat((BigDecimal) at(byModel.get("316"), "sum.moneyProp")).isEqualByComparingTo("30");
        assertThat((BigDecimal) at(byModel.get("A4"), "sum.bigDecimalProp")).isEqualByComparingTo("4");
        assertThat((BigDecimal) at(byModel.get("A4"), "sum.moneyProp")).isEqualByComparingTo("40");
        // A group whose values are all unassigned has no sum.
        assertThat(at(byModel.get(null), "sum.bigDecimalProp")).isNull();
        assertThat(at(byModel.get(null), "sum.moneyProp")).isNull();
    }

    @Test
    public void avg_is_computed_for_decimal_properties_within_each_group() {
        final var rows = queryAgg("{ tgWebApiEntity_agg { groupBy { model { key } } avg { bigDecimalProp moneyProp } } }");

        final var byModel = rowsBy(rows, "groupBy.model.key");
        assertThat((BigDecimal) at(byModel.get("316"), "avg.bigDecimalProp")).isEqualByComparingTo("2");
        assertThat((BigDecimal) at(byModel.get("316"), "avg.moneyProp")).isEqualByComparingTo("15");
        assertThat((BigDecimal) at(byModel.get("A4"), "avg.bigDecimalProp")).isEqualByComparingTo("4");
        assertThat((BigDecimal) at(byModel.get("A4"), "avg.moneyProp")).isEqualByComparingTo("40");
    }

    @Test
    public void avg_of_an_integer_property_is_an_integer() {
        final var rows = queryAgg("{ tgWebApiEntity_agg { avg { intProp } } }");

        assertThat(rows).hasSize(1);
        // True value is 2.67
        assertThat(at(rows.getFirst(), "avg.intProp")).isIn(2, 3);
    }

    @Test
    public void avg_of_a_non_numeric_property_is_an_error() {
        final var result = webApi.execute(input("{ tgWebApiEntity_agg { avg { key } } }"));

        assertThat(errors(result)).isNotEmpty();
    }

    @Test
    public void min_and_max_are_computed_for_numeric_properties() {
        final var rows = queryAgg("{ tgWebApiEntity_agg { min { intProp bigDecimalProp moneyProp } max { intProp bigDecimalProp moneyProp } } }");

        assertThat(rows).hasSize(1);
        final var row = rows.getFirst();
        assertThat(at(row, "min.intProp")).isEqualTo(1);
        assertThat(at(row, "max.intProp")).isEqualTo(5);
        assertThat((BigDecimal) at(row, "min.bigDecimalProp")).isEqualByComparingTo("1.5");
        assertThat((BigDecimal) at(row, "max.bigDecimalProp")).isEqualByComparingTo("4");
        assertThat((BigDecimal) at(row, "min.moneyProp")).isEqualByComparingTo("10");
        assertThat((BigDecimal) at(row, "max.moneyProp")).isEqualByComparingTo("40");
    }

    @Test
    public void min_and_max_are_computed_for_non_numeric_properties() {
        final var rows = queryAgg("{ tgWebApiEntity_agg { min { key dateProp model { key } } max { key dateProp model { key } } } }");

        assertThat(rows).hasSize(1);
        final var row = rows.getFirst();
        assertThat(at(row, "min.key")).isEqualTo("VEH1");
        assertThat(at(row, "max.key")).isEqualTo("VEH4");
        assertThat(at(row, "min.dateProp")).isEqualTo(createDateRepr(DATE_1));
        assertThat(at(row, "max.dateProp")).isEqualTo(createDateRepr(DATE_3));
        assertThat(at(row, "min.model.key")).isEqualTo("316");
        assertThat(at(row, "max.model.key")).isEqualTo("A4");
    }

    @Test
    public void min_and_max_are_computed_within_each_group() {
        final var rows = queryAgg("{ tgWebApiEntity_agg { groupBy { model { key } } min { dateProp } max { dateProp } } }");

        final var byModel = rowsBy(rows, "groupBy.model.key");
        assertThat(at(byModel.get("316"), "min.dateProp")).isEqualTo(createDateRepr(DATE_1));
        assertThat(at(byModel.get("316"), "max.dateProp")).isEqualTo(createDateRepr(DATE_2));
        assertThat(at(byModel.get("A4"), "min.dateProp")).isEqualTo(createDateRepr(DATE_3));
        assertThat(at(byModel.get("A4"), "max.dateProp")).isEqualTo(createDateRepr(DATE_3));
    }

    @Test
    public void all_aggregates_can_be_combined_in_one_query() {
        final var rows = queryAgg("""
          { tgWebApiEntity_agg {
              groupBy { model { key } }
              count
              sum { intProp }
              avg { bigDecimalProp }
              min { dateProp }
              max { dateProp }
          } }
          """);

        final var row = rowsBy(rows, "groupBy.model.key").get("316");
        assertThat(row.get("count")).isEqualTo(2);
        assertThat(((Number) at(row, "sum.intProp")).intValue()).isEqualTo(3);
        assertThat((BigDecimal) at(row, "avg.bigDecimalProp")).isEqualByComparingTo("2");
        assertThat(at(row, "min.dateProp")).isEqualTo(createDateRepr(DATE_1));
        assertThat(at(row, "max.dateProp")).isEqualTo(createDateRepr(DATE_2));
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Selection shapes
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @Test
    public void aliased_aggregation_root_fields_are_computed_independently() {
        final var result = webApi.execute(input("""
          { total: tgWebApiEntity_agg { count }
            byModel: tgWebApiEntity_agg { groupBy { model { key } } count } }
          """));
        assertNoErrors(result);

        assertThat(listAt(data(result), "total")).containsExactly(Map.of("count", 4));
        assertThat(rowsBy(listAt(data(result), "byModel"), "groupBy.model.key", "count"))
                .containsExactlyInAnyOrderEntriesOf(mapOf(t2("316", 2), t2("A4", 1), t2(null, 1)));
    }

    @Test
    public void the_same_aggregate_can_be_selected_twice_under_different_aliases() {
        final var rows = queryAgg("{ tgWebApiEntity_agg { a: sum { intProp } b: sum { longProp } } }");

        assertThat(rows).hasSize(1);
        assertThat(((Number) at(rows.getFirst(), "a.intProp")).intValue()).isEqualTo(8);
        assertThat(((Number) at(rows.getFirst(), "b.longProp")).longValue()).isEqualTo(80L);
    }

    @Test
    public void aggregates_can_be_selected_through_a_fragment() {
        final var rows = queryAgg("""
          { tgWebApiEntity_agg { ...Counts } }
          fragment Counts on TgWebApiEntity_Agg { count }
          """);

        assertThat(rows).containsExactly(Map.of("count", 4));
    }

    @Test
    public void aggregates_can_be_selected_through_an_inline_fragment() {
        final var rows = queryAgg("{ tgWebApiEntity_agg { ... on TgWebApiEntity_Agg { count } } }");

        assertThat(rows).containsExactly(Map.of("count", 4));
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Security
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @Test
    public void aggregation_is_permitted_when_reading_is_authorised() {
        assertHasToken(TgVehicleModel_CanRead_Token.class);
        final var rows = queryAgg("{ tgVehicleModel_agg { count } }", "tgVehicleModel_agg");

        assertThat(rows).containsExactly(Map.of("count", 2));
    }

    /// Aggregation reveals data of the type, so it requires the same authorisation as reading the type through its own root field.
    ///
    @Test
    public void aggregation_is_denied_if_CanRead_access_is_prohibited() {
        runWithoutToken(TgVehicleModel_CanRead_Token.class, () -> {
            final var result = webApi.execute(input("{ tgVehicleModel_agg { count } }"));

            assertThat(errors(result)).hasSize(1);
            assertThat(errors(result).getFirst().toString()).contains("Permission denied");
        });
    }

    @Test
    public void properties_are_not_available_for_aggregation_if_CanReadModel_access_is_prohibited() {
        runWithoutToken(TgVehicleModel_CanReadModel_Token.class, () -> {
            final var result = webApi.execute(input("{ tgVehicleModel_agg { groupBy { key } } }"));

            assertThat(errors(result)).hasSize(1);
            assertThat(errors(result).getFirst().toString()).contains("Field 'key' in type 'TgVehicleModel' is undefined");
        });
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Utilities
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    private List<Map<String, Object>> queryAgg(final String query) {
        return queryAgg(query, ROOT);
    }

    private List<Map<String, Object>> queryAgg(final String query, final String rootField) {
        final var result = webApi.execute(input(query));
        assertNoErrors(result);
        return listAt(data(result), rootField);
    }

    private List<String> queryRootFieldNames() {
        final var result = webApi.execute(input("{ __schema { queryType { fields { name } } } }"));
        assertNoErrors(result);
        return listAt(data(result), "__schema.queryType.fields").stream().map(f -> (String) f.get("name")).toList();
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

    /// Indexes `rows` by the value at `path`, which must be unique across `rows`.
    ///
    private static Map<Object, Map<String, Object>> rowsBy(final List<Map<String, Object>> rows, final String path) {
        final var result = new HashMap<Object, Map<String, Object>>();
        rows.forEach(row -> assertThat(result.put(at(row, path), row)).as("Duplicate group [%s].", at(row, path)).isNull());
        return result;
    }

    /// Indexes `rows` by the value at `path`, which must be unique across `rows`.
    /// Each row is then transformed into its attribute at `rowPath`.
    ///
    private static Map<Object, Object> rowsBy(final List<Map<String, Object>> rows, final String path, final String rowPath) {
        final var result = new HashMap<>();
        rows.forEach(row -> assertThat(result.put(at(row, path), at(row, rowPath))).as("Duplicate group [%s].", at(row, path)).isNull());
        return result;
    }

    private static void assertNoErrors(final Map<String, Object> result) {
        final var errors = errors(result);
        assertThat(errors).as(errors.toString()).isEmpty();
    }

    /// Withdraws `token` from the role of the current user.
    ///
    private void runWithoutToken(final Class<? extends ISecurityToken> token, final Runnable runnable) {
        final SecurityRoleAssociationCo co$ = co$(SecurityRoleAssociation.class);
        final var assocs = co$.getAllEntities(from(co$.selectActiveAssociations(getUser(), token)).model());
        if (!assocs.isEmpty()) {
            co$.removeAssociations(assocs);
            try {
                runnable.run();
            } finally {
                co$.addAssociations(assocs.stream()
                                           .map(assoc -> new_(SecurityRoleAssociation.class).setRole(assoc.getRole()).setSecurityToken(assoc.getSecurityToken()))
                                           .toList());
            }
        }
        else {
            runnable.run();
        }
    }

    private void assertHasToken(final Class<? extends ISecurityToken> tokenType) {
        final SecurityRoleAssociationCo coSecurityRoleAssociation = co(SecurityRoleAssociation.class);
        assertThat(coSecurityRoleAssociation.countActiveAssociations(getUser(), tokenType))
                .as(() -> "Expected user [%s] to have token [%s].".formatted(getUser(), tokenType.getSimpleName()))
                .isNotZero();
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
        final var m316 = save(new_(TgVehicleModel.class, "316", "316").setMake(merc));
        final var a4 = save(new_(TgVehicleModel.class, "A4", "A4").setMake(audi));

        save(new_(TgWebApiEntity.class, "VEH1").setModel(m316).setIntProp(1).setLongProp(10L)
                     .setBigDecimalProp(new BigDecimal("1.5")).setMoneyProp(new Money("10")).setDateProp(DATE_1).setActive(true));
        save(new_(TgWebApiEntity.class, "VEH2").setModel(m316).setIntProp(2).setLongProp(20L)
                     .setBigDecimalProp(new BigDecimal("2.5")).setMoneyProp(new Money("20")).setDateProp(DATE_2).setActive(false));
        save(new_(TgWebApiEntity.class, "VEH3").setModel(a4).setIntProp(5).setLongProp(50L)
                     .setBigDecimalProp(new BigDecimal("4.0")).setMoneyProp(new Money("40")).setDateProp(DATE_3).setActive(true));
        save(new_(TgWebApiEntity.class, "VEH4").setActive(false));
    }

}
