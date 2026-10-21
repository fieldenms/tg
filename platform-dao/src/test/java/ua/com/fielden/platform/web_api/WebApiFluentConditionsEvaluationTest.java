package ua.com.fielden.platform.web_api;

import org.joda.time.DateTime;
import org.junit.Test;
import ua.com.fielden.platform.entity.activatable.test_entities.Member1;
import ua.com.fielden.platform.entity.activatable.test_entities.Member2;
import ua.com.fielden.platform.entity.activatable.test_entities.Union;
import ua.com.fielden.platform.entity.activatable.test_entities.UnionOwner;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.sample.domain.TgWebApiEntity;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.types.Colour;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.types.Money;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static ua.com.fielden.platform.web_api.EntityCondToEqlCompiler.*;
import static ua.com.fielden.platform.web_api.GraphQLCommon.rootFieldName;
import static ua.com.fielden.platform.web_api.GraphQLCommon.rootFieldNameForAggEntity;
import static ua.com.fielden.platform.web_api.WebApiUtils.*;

/// Test for the operational semantics of conditions given through argument `where` of root fields:
/// queries with various conditions are run, and the entities they return are asserted.
///
/// The test data comprises five instances of [TgWebApiEntity]:
///
/// | key   | desc                | model | make | intProp | longProp | bigDecimalProp | moneyProp | dateProp   | active | hyperlinkProp | colourProp |
/// |-------|---------------------|-------|------|---------|----------|----------------|-----------|------------|--------|---------------|------------|
/// | VEH1  | Reach stacker, blue | 316   | MERC | 1       | 10       | 1.5            | 10        | 2025-01-10 | true   | assigned      | assigned   |
/// | VEH2  | Forklift            | 316   | MERC | 2       | 20       | 2.5            | 20        | 2025-02-10 | false  |               |            |
/// | VEH3  | reach truck         | A4    | AUDI | 5       | 50       | 4.0            | 40        | 2025-03-10 | true   |               |            |
/// | VEH4  |                     |       |      |         |          |                |           |            | false  |               |            |
/// | VEH_5 | Pump                | A4    | AUDI |         |          |                |           |            | true   |               |            |
///
/// And three instances of [UnionOwner], whose property `union` is of union type [Union]:
///
/// | key | union             |
/// |-----|-------------------|
/// | O1  | member1: M1A      |
/// | O2  | member2: M2A      |
/// | O3  |                   |
///
public class WebApiFluentConditionsEvaluationTest extends AbstractDaoTestCase {

    private static final String
            ROOT = rootFieldName(TgWebApiEntity.class),
            AGG_ROOT = rootFieldNameForAggEntity(TgWebApiEntity.class),
            UNION_ROOT = rootFieldName(UnionOwner.class);

    private static final String VEH1 = "VEH1", VEH2 = "VEH2", VEH3 = "VEH3", VEH4 = "VEH4", VEH5 = "VEH_5";
    private static final List<String> ALL = List.of(VEH1, VEH2, VEH3, VEH4, VEH5);

    private static final Date
            DATE_1 = new DateTime(2025, 1, 10, 0, 0).toDate(),
            DATE_2 = new DateTime(2025, 2, 10, 0, 0).toDate(),
            DATE_3 = new DateTime(2025, 3, 10, 0, 0).toDate();

    private final IWebApi webApi = getInstance(IWebApi.class);

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Absence of conditions
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @Test
    public void omitted_where_imposes_no_condition() {
        final var result = webApi.execute(input("{%s{key}}".formatted(ROOT)));
        assertNoErrors(result);
        assertThat(keysOf(result, ROOT)).containsExactlyInAnyOrderElementsOf(ALL);
    }

    @Test
    public void where_null_imposes_no_condition() {
        assertThat(keys("null")).containsExactlyInAnyOrderElementsOf(ALL);
    }

    @Test
    public void empty_cond_imposes_no_condition() {
        assertThat(keys("{cond: {}}")).containsExactlyInAnyOrderElementsOf(ALL);
    }

    @Test
    public void empty_value_condition_imposes_no_condition() {
        assertThat(keys("{cond: {desc: {}}}")).containsExactlyInAnyOrderElementsOf(ALL);
    }

    @Test
    public void null_operator_imposes_no_condition_and_does_not_test_for_a_missing_value() {
        assertThat(keys("{cond: {desc: {eq: null}}}")).containsExactlyInAnyOrderElementsOf(ALL);
    }

    @Test
    public void null_property_condition_imposes_no_condition() {
        assertThat(keys("{cond: {desc: null}}")).containsExactlyInAnyOrderElementsOf(ALL);
    }

    @Test
    public void empty_and_and_empty_or_are_ignored() {
        assertThat(keys("{and: []}")).containsExactlyInAnyOrderElementsOf(ALL);
        assertThat(keys("{or: []}")).containsExactlyInAnyOrderElementsOf(ALL);
    }

    @Test
    public void a_condition_that_consists_only_of_ignored_conditions_is_ignored() {
        assertThat(keys("{not: {or: []}}")).containsExactlyInAnyOrderElementsOf(ALL);
        assertThat(keys("{not: {and: [{cond: {}}, {or: []}]}}")).containsExactlyInAnyOrderElementsOf(ALL);
    }

    @Test
    public void an_ignored_condition_on_an_entity_typed_property_does_not_require_the_property_to_be_assigned() {
        assertThat(keys("{cond: {model: {cond: {}}}}")).containsExactlyInAnyOrderElementsOf(ALL);
        assertThat(keys("{cond: {model: {and: []}}}")).containsExactlyInAnyOrderElementsOf(ALL);
    }

    @Test
    public void where_can_be_given_as_a_variable() {
        final var result = webApi.execute(input(
                "query($w: TgWebApiEntity_Cond) {%s(where: $w){key}}".formatted(ROOT),
                Map.of("w", Map.of("cond", Map.of("key", Map.of("eq", VEH2))))));
        assertNoErrors(result);
        assertThat(keysOf(result, ROOT)).containsExactly(VEH2);
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Combination
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @Test
    public void property_conditions_in_one_cond_are_combined_with_AND() {
        assertThat(keys("{cond: {active: {eq: true}, intProp: {ge: 2}}}")).containsExactlyInAnyOrder(VEH3);
    }

    @Test
    public void operators_in_one_value_condition_are_combined_with_AND() {
        assertThat(keys("{cond: {intProp: {ge: 2, lt: 5}}}")).containsExactlyInAnyOrder(VEH2);
    }

    @Test
    public void and_requires_all_conditions_to_hold() {
        assertThat(keys("{and: [{cond: {active: {eq: true}}}, {cond: {model: {cond: {key: {eq: \"A4\"}}}}}]}"))
                .containsExactlyInAnyOrder(VEH3, VEH5);
    }

    @Test
    public void or_requires_at_least_one_condition_to_hold() {
        assertThat(keys("{or: [{cond: {key: {eq: \"VEH1\"}}}, {cond: {key: {eq: \"VEH4\"}}}]}"))
                .containsExactlyInAnyOrder(VEH1, VEH4);
    }

    @Test
    public void or_with_a_single_condition_is_that_condition() {
        assertThat(keys("{or: [{cond: {key: {eq: \"VEH1\"}}}]}")).containsExactlyInAnyOrder(VEH1);
    }

    @Test
    public void not_does_not_match_entities_with_unassigned_properties_that_the_negated_condition_refers_to() {
        assertThat(keys("{not: {cond: {desc: {eq: \"Forklift\"}}}}")).containsExactlyInAnyOrder(VEH1, VEH3, VEH5);
        assertThat(keys("{not: {cond: {intProp: {gt: 1}}}}")).containsExactlyInAnyOrder(VEH1);
    }

    @Test
    public void entities_with_unassigned_properties_are_matched_by_testing_for_missing_values_explicitly() {
        assertThat(keys("{or: [{not: {cond: {desc: {eq: \"Forklift\"}}}}, {cond: {desc: {isNull: true}}}]}"))
                .containsExactlyInAnyOrder(VEH1, VEH3, VEH4, VEH5);
    }

    @Test
    public void double_negation_is_the_original_condition() {
        assertThat(keys("{not: {not: {cond: {intProp: {gt: 1}}}}}")).containsExactlyInAnyOrder(VEH2, VEH3);
    }

    @Test
    public void connectives_nest() {
        // (active AND intProp < 5) OR NOT (model.key = 316)
        // VEH4 is not matched: it is inactive, and it has no model, which makes the negated condition unknown.
        assertThat(keys("""
                {or: [
                  {and: [{cond: {active: {eq: true}}}, {cond: {intProp: {lt: 5}}}]},
                  {not: {cond: {model: {cond: {key: {eq: "316"}}}}}}
                ]}"""))
                .containsExactlyInAnyOrder(VEH1, VEH3, VEH5);
    }

    @Test
    public void or_inside_a_condition_on_an_entity_typed_property_applies_to_the_referenced_entity() {
        assertThat(keys("""
                        {cond: {model: {or: [{cond: {key: {eq: "316"}}}, {isNull: true}]}}}"""))
                .containsExactlyInAnyOrder(VEH1, VEH2, VEH4);
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Strings
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @Test
    public void String_eq_matches_the_whole_value() {
        assertThat(keys("{cond: {key: {eq: \"VEH1\"}}}")).containsExactlyInAnyOrder(VEH1);
        assertThat(keys("{cond: {key: {eq: \"VEH\"}}}")).isEmpty();
    }

    @Test
    public void String_eq_treats_asterisk_as_an_ordinary_character() {
        assertThat(keys("{cond: {key: {eq: \"VEH*\"}}}")).isEmpty();
    }

    @Test
    public void String_values_containing_commas_are_matched_as_they_are() {
        assertThat(keys("{cond: {desc: {eq: \"Reach stacker, blue\"}}}")).containsExactlyInAnyOrder(VEH1);
        assertThat(keys("{cond: {desc: {in: [\"Reach stacker, blue\"]}}}")).containsExactlyInAnyOrder(VEH1);
    }

    @Test
    public void String_ne_does_not_match_unassigned_values() {
        assertThat(keys("{cond: {desc: {ne: \"Forklift\"}}}")).containsExactlyInAnyOrder(VEH1, VEH3, VEH5);
    }

    @Test
    public void String_in_and_notIn() {
        assertThat(keys("{cond: {key: {in: [\"VEH1\", \"VEH3\", \"NONE\"]}}}")).containsExactlyInAnyOrder(VEH1, VEH3);
        assertThat(keys("{cond: {desc: {notIn: [\"Forklift\", \"reach truck\"]}}}")).containsExactlyInAnyOrder(VEH1, VEH5);
    }

    @Test
    public void String_like_uses_asterisk_as_a_wildcard_matching_any_sequence_of_characters_including_none() {
        assertThat(keys("{cond: {key: {like: \"VEH*\"}}}")).containsExactlyInAnyOrderElementsOf(ALL);
        assertThat(keys("{cond: {key: {like: \"VEH1*\"}}}")).containsExactlyInAnyOrder(VEH1);
        assertThat(keys("{cond: {desc: {like: \"*stacker*\"}}}")).containsExactlyInAnyOrder(VEH1);
    }

    @Test
    public void String_like_without_asterisk_matches_the_whole_value() {
        assertThat(keys("{cond: {desc: {like: \"stacker\"}}}")).isEmpty();
        assertThat(keys("{cond: {desc: {like: \"Forklift\"}}}")).containsExactlyInAnyOrder(VEH2);
    }

    @Test
    public void String_like_matches_underscore_literally() {
        assertThat(keys("{cond: {key: {like: \"VEH_*\"}}}")).containsExactlyInAnyOrder(VEH5);
    }

    @Test
    public void String_notLike_does_not_match_unassigned_values() {
        assertThat(keys("{cond: {desc: {notLike: \"*stacker*\"}}}")).containsExactlyInAnyOrder(VEH2, VEH3, VEH5);
    }

    @Test
    public void String_iLike_and_notILike_ignore_case() {
        assertThat(keys("{cond: {desc: {iLike: \"*REACH*\"}}}")).containsExactlyInAnyOrder(VEH1, VEH3);
        assertThat(keys("{cond: {desc: {notILike: \"*REACH*\"}}}")).containsExactlyInAnyOrder(VEH2, VEH5);
    }

    @Test
    public void String_isNull() {
        assertThat(keys("{cond: {desc: {isNull: true}}}")).containsExactlyInAnyOrder(VEH4);
        assertThat(keys("{cond: {desc: {isNull: false}}}")).containsExactlyInAnyOrder(VEH1, VEH2, VEH3, VEH5);
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Booleans
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @Test
    public void Boolean_eq() {
        assertThat(keys("{cond: {active: {eq: true}}}")).containsExactlyInAnyOrder(VEH1, VEH3, VEH5);
        assertThat(keys("{cond: {active: {eq: false}}}")).containsExactlyInAnyOrder(VEH2, VEH4);
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Numbers and money
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @Test
    public void Int_eq_and_ne_do_not_match_unassigned_values() {
        assertThat(keys("{cond: {intProp: {eq: 2}}}")).containsExactlyInAnyOrder(VEH2);
        assertThat(keys("{cond: {intProp: {ne: 2}}}")).containsExactlyInAnyOrder(VEH1, VEH3);
    }

    @Test
    public void Int_strict_comparisons_exclude_the_bound_and_non_strict_ones_include_it() {
        assertThat(keys("{cond: {intProp: {lt: 2}}}")).containsExactlyInAnyOrder(VEH1);
        assertThat(keys("{cond: {intProp: {le: 2}}}")).containsExactlyInAnyOrder(VEH1, VEH2);
        assertThat(keys("{cond: {intProp: {gt: 2}}}")).containsExactlyInAnyOrder(VEH3);
        assertThat(keys("{cond: {intProp: {ge: 2}}}")).containsExactlyInAnyOrder(VEH2, VEH3);
    }

    @Test
    public void Int_in_and_notIn_do_not_match_unassigned_values() {
        assertThat(keys("{cond: {intProp: {in: [1, 5, 7]}}}")).containsExactlyInAnyOrder(VEH1, VEH3);
        assertThat(keys("{cond: {intProp: {notIn: [1, 5]}}}")).containsExactlyInAnyOrder(VEH2);
    }

    @Test
    public void Int_isNull() {
        assertThat(keys("{cond: {intProp: {isNull: true}}}")).containsExactlyInAnyOrder(VEH4, VEH5);
        assertThat(keys("{cond: {intProp: {isNull: false}}}")).containsExactlyInAnyOrder(VEH1, VEH2, VEH3);
    }

    @Test
    public void Long_conditions() {
        assertThat(keys("{cond: {longProp: {gt: 10, le: 50}}}")).containsExactlyInAnyOrder(VEH2, VEH3);
        assertThat(keys("{cond: {longProp: {in: [10, 50]}}}")).containsExactlyInAnyOrder(VEH1, VEH3);
        assertThat(keys("{cond: {longProp: {isNull: true}}}")).containsExactlyInAnyOrder(VEH4, VEH5);
    }

    @Test
    public void BigDecimal_conditions() {
        assertThat(keys("{cond: {bigDecimalProp: {ge: 2.5}}}")).containsExactlyInAnyOrder(VEH2, VEH3);
        assertThat(keys("{cond: {bigDecimalProp: {lt: 2}}}")).containsExactlyInAnyOrder(VEH1);
        assertThat(keys("{cond: {bigDecimalProp: {eq: 4}}}")).containsExactlyInAnyOrder(VEH3);
        assertThat(keys("{cond: {bigDecimalProp: {in: [1.5, 4.0]}}}")).containsExactlyInAnyOrder(VEH1, VEH3);
    }

    @Test
    public void Money_values_are_compared_by_amount() {
        assertThat(keys("{cond: {moneyProp: {ge: 20}}}")).containsExactlyInAnyOrder(VEH2, VEH3);
        assertThat(keys("{cond: {moneyProp: {lt: 20.5}}}")).containsExactlyInAnyOrder(VEH1, VEH2);
        assertThat(keys("{cond: {moneyProp: {eq: 40}}}")).containsExactlyInAnyOrder(VEH3);
        assertThat(keys("{cond: {moneyProp: {notIn: [10, 40]}}}")).containsExactlyInAnyOrder(VEH2);
        assertThat(keys("{cond: {moneyProp: {isNull: true}}}")).containsExactlyInAnyOrder(VEH4, VEH5);
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Dates
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @Test
    public void Date_eq_and_ne() {
        assertThat(keys("{cond: {dateProp: {eq: \"2025-01-10\"}}}")).containsExactlyInAnyOrder(VEH1);
        assertThat(keys("{cond: {dateProp: {ne: \"2025-01-10\"}}}")).containsExactlyInAnyOrder(VEH2, VEH3);
    }

    @Test
    public void Date_calendar_period_is_expressed_with_a_half_open_range() {
        assertThat(keys("{cond: {dateProp: {ge: \"2025-02\", lt: \"2025-03\"}}}")).containsExactlyInAnyOrder(VEH2);
    }

    @Test
    public void Date_less_precise_value_denotes_an_instant() {
        // "2025-03" is 2025-03-01 00:00, which precedes 2025-03-10.
        assertThat(keys("{cond: {dateProp: {gt: \"2025-03\"}}}")).containsExactlyInAnyOrder(VEH3);
        assertThat(keys("{cond: {dateProp: {le: \"2025-02-10\"}}}")).containsExactlyInAnyOrder(VEH1, VEH2);
    }

    @Test
    public void Date_accepts_numbers_in_the_basic_date_format() {
        assertThat(keys("{cond: {dateProp: {lt: 20250201}}}")).containsExactlyInAnyOrder(VEH1);
    }

    @Test
    public void Date_isNull() {
        assertThat(keys("{cond: {dateProp: {isNull: true}}}")).containsExactlyInAnyOrder(VEH4, VEH5);
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Hyperlink and Colour
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @Test
    public void Hyperlink_isNull() {
        assertThat(keys("{cond: {hyperlinkProp: {isNull: false}}}")).containsExactlyInAnyOrder(VEH1);
        assertThat(keys("{cond: {hyperlinkProp: {isNull: true}}}")).containsExactlyInAnyOrder(VEH2, VEH3, VEH4, VEH5);
    }

    @Test
    public void Colour_isNull() {
        assertThat(keys("{cond: {colourProp: {isNull: false}}}")).containsExactlyInAnyOrder(VEH1);
        assertThat(keys("{cond: {colourProp: {isNull: true}}}")).containsExactlyInAnyOrder(VEH2, VEH3, VEH4, VEH5);
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Entity-typed properties
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @Test
    public void condition_on_an_entity_typed_property_applies_to_the_referenced_entity() {
        assertThat(keys("{cond: {model: {cond: {key: {eq: \"316\"}}}}}")).containsExactlyInAnyOrder(VEH1, VEH2);
    }

    @Test
    public void conditions_on_entity_typed_properties_apply_to_any_depth() {
        assertThat(keys("{cond: {model: {cond: {make: {cond: {key: {eq: \"AUDI\"}}}}}}}")).containsExactlyInAnyOrder(VEH3, VEH5);
        assertThat(keys("{cond: {model: {cond: {make: {cond: {desc: {iLike: \"merc*\"}}}}}}}")).containsExactlyInAnyOrder(VEH1, VEH2);
    }

    @Test
    public void isNull_on_an_entity_typed_property() {
        assertThat(keys("{cond: {model: {isNull: true}}}")).containsExactlyInAnyOrder(VEH4);
        assertThat(keys("{cond: {model: {isNull: false}}}")).containsExactlyInAnyOrder(VEH1, VEH2, VEH3, VEH5);
    }

    @Test
    public void a_property_reached_through_an_unassigned_entity_typed_property_is_unassigned() {
        assertThat(keys("{cond: {model: {cond: {make: {isNull: true}}}}}")).containsExactlyInAnyOrder(VEH4);
        assertThat(keys("{cond: {model: {cond: {key: {ne: \"316\"}}}}}")).containsExactlyInAnyOrder(VEH3, VEH5);
    }

    @Test
    public void negation_inside_and_outside_an_entity_typed_property_is_the_same() {
        assertThat(keys("{cond: {model: {not: {cond: {key: {eq: \"316\"}}}}}}")).containsExactlyInAnyOrder(VEH3, VEH5);
        assertThat(keys("{not: {cond: {model: {cond: {key: {eq: \"316\"}}}}}}")).containsExactlyInAnyOrder(VEH3, VEH5);
    }

    @Test
    public void negated_isNull_on_an_entity_typed_property() {
        assertThat(keys("{cond: {model: {not: {isNull: true}}}}")).containsExactlyInAnyOrder(VEH1, VEH2, VEH3, VEH5);
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Union-typed properties
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @Test
    public void condition_through_a_union_member_holds_iff_the_member_is_active_and_satisfies_the_condition() {
        assertThat(unionOwnerKeys("{cond: {union: {cond: {member1: {cond: {key: {eq: \"M1A\"}}}}}}}")).containsExactlyInAnyOrder("O1");
        assertThat(unionOwnerKeys("{cond: {union: {cond: {member2: {cond: {key: {eq: \"M1A\"}}}}}}}")).isEmpty();
    }

    @Test
    public void isNull_on_a_union_member_tests_whether_that_member_is_active() {
        assertThat(unionOwnerKeys("{cond: {union: {cond: {member1: {isNull: false}}}}}")).containsExactlyInAnyOrder("O1");
        assertThat(unionOwnerKeys("{cond: {union: {cond: {member2: {isNull: false}}}}}")).containsExactlyInAnyOrder("O2");
    }

    @Test
    public void isNull_on_a_union_typed_property() {
        assertThat(unionOwnerKeys("{cond: {union: {isNull: true}}}")).containsExactlyInAnyOrder("O3");
        assertThat(unionOwnerKeys("{cond: {union: {isNull: false}}}")).containsExactlyInAnyOrder("O1", "O2");
    }

    @Test
    public void alternatives_over_union_members_are_expressed_with_or() {
        assertThat(unionOwnerKeys("""
                {cond: {union: {or: [
                  {cond: {member1: {cond: {key: {eq: "M1A"}}}}},
                  {cond: {member2: {cond: {key: {eq: "M2A"}}}}}
                ]}}}"""))
                .containsExactlyInAnyOrder("O1", "O2");
    }

    @Test
    public void conditions_on_two_union_members_in_one_cond_never_hold_together() {
        assertThat(unionOwnerKeys("{cond: {union: {cond: {member1: {isNull: false}, member2: {isNull: false}}}}}")).isEmpty();
    }

    @Test
    public void negation_does_not_match_entities_whose_union_member_is_not_active() {
        assertThat(unionOwnerKeys("{not: {cond: {union: {cond: {member1: {cond: {key: {eq: \"M1A\"}}}}}}}}")).isEmpty();
        assertThat(unionOwnerKeys("""
                {or: [
                  {not: {cond: {union: {cond: {member1: {cond: {key: {eq: "M1A"}}}}}}}},
                  {cond: {union: {cond: {member1: {isNull: true}}}}}
                ]}"""))
                .containsExactlyInAnyOrder("O2", "O3");
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Errors
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @Test
    public void empty_where_object_is_an_error() {
        assertHasErrors(query("{}"));
    }

    @Test
    public void more_than_one_field_in_an_entity_condition_is_an_error() {
        assertHasErrors(query("{cond: {key: {eq: \"VEH1\"}}, not: {cond: {key: {eq: \"VEH2\"}}}}"));
        assertHasErrors(query("{cond: {model: {cond: {key: {eq: \"316\"}}, isNull: false}}}"));
    }

    @Test
    public void isNull_in_a_root_level_condition_is_an_error() {
        List.of("{isNull: true}",
                "{isNull: false}",
                "{not: {isNull: true}}",
                "{and: [{cond: {key: {eq: \"VEH1\"}}}, {isNull: false}]}",
                "{or: [{not: {isNull: true}}, {cond: {key: {eq: \"VEH1\"}}}]}")
            .forEach(where -> assertHasErrorMessageSatisfying(
                    query(where),
                    msg -> assertThat(msg).as(where).contains(ERR_IS_NULL_CANNOT_BE_USED_IN_A_ROOT_ENTITY_CONDITION)));
    }

    @Test
    public void isNull_in_a_root_level_condition_on_an_aggregation_root_field_is_an_error() {
        List.of("{isNull: true}",
                "{isNull: false}",
                "{not: {isNull: true}}",
                "{and: [{cond: {key: {eq: \"VEH1\"}}}, {isNull: false}]}",
                "{or: [{not: {isNull: true}}, {cond: {key: {eq: \"VEH1\"}}}]}")
                .forEach(where -> assertHasErrorMessageSatisfying(
                        aggQuery(where, "count"),
                        msg -> assertThat(msg).as(where).contains(ERR_IS_NULL_CANNOT_BE_USED_IN_A_ROOT_ENTITY_CONDITION)));
    }

    @Test
    public void isNull_is_allowed_in_a_condition_on_an_entity_typed_property() {
        assertThat(keys("{or: [{not: {cond: {model: {isNull: true}}}}, {cond: {key: {eq: \"VEH4\"}}}]}"))
                .containsExactlyInAnyOrderElementsOf(ALL);
    }

    @Test
    public void empty_in_and_notIn_lists_are_errors() {
        assertHasErrorMessageSatisfying(query("{cond: {key: {in: []}}}"),
                                        msg -> assertThat(msg).contains(ERR_IN_DOES_NOT_PERMIT_EMPTY_LISTS));
        assertHasErrorMessageSatisfying(query("{cond: {intProp: {notIn: []}}}"),
                                        msg -> assertThat(msg).contains(ERR_NOT_IN_DOES_NOT_PERMIT_EMPTY_LISTS));
    }

    @Test
    public void null_elements_in_in_lists_are_errors() {
        assertHasErrors(query("{cond: {key: {in: [\"VEH1\", null]}}}"));
    }

    @Test
    public void unknown_properties_are_errors() {
        assertHasErrors(query("{cond: {noSuchProp: {eq: \"x\"}}}"));
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Aggregation
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    @Test
    public void where_on_an_aggregation_root_field_restricts_the_rows_before_grouping() {
        final var rows = aggRows("{cond: {active: {eq: true}}}", "count");
        assertThat(rows).hasSize(1);
        assertThat(rows.getFirst().get("count")).isEqualTo(3);
    }

    @Test
    public void groups_are_formed_only_from_the_entities_that_satisfy_where() {
        final var rows = aggRows("{cond: {active: {eq: true}}}", "groupBy{model{key}} count");
        assertThat(rowsBy(rows, "groupBy.model.key", "count"))
                .containsOnly(entry("316", 1), entry("A4", 2));
    }

    @Test
    public void aggregates_are_computed_only_over_the_entities_that_satisfy_where() {
        final var rows = aggRows("{cond: {model: {cond: {key: {eq: \"316\"}}}}}",
                                 "count sum{intProp longProp bigDecimalProp moneyProp} max{dateProp}");
        assertThat(rows).hasSize(1);
        final var row = rows.getFirst();
        assertThat(row.get("count")).isEqualTo(2);
        assertThat(((Number) at(row, "sum.intProp")).intValue()).isEqualTo(3);
        assertThat(((Number) at(row, "sum.longProp")).longValue()).isEqualTo(30L);
        assertThat((BigDecimal) at(row, "sum.bigDecimalProp")).isEqualByComparingTo("4");
        assertThat((BigDecimal) at(row, "sum.moneyProp")).isEqualByComparingTo("30");
        assertThat(at(row, "max.dateProp.millis")).isEqualTo(DATE_2.getTime());
    }

    @Test
    public void where_may_refer_to_properties_that_are_neither_grouped_by_nor_aggregated() {
        final var rows = aggRows("{cond: {desc: {iLike: \"*reach*\"}}}", "groupBy{active} sum{longProp}");
        assertThat(rows).hasSize(1);
        assertThat(at(rows.getFirst(), "groupBy.active")).isEqualTo(true);
        assertThat(((Number) at(rows.getFirst(), "sum.longProp")).longValue()).isEqualTo(60L);
    }

    @Test
    public void connectives_and_tests_for_missing_values_restrict_the_rows_before_grouping() {
        final var rows = aggRows("{or: [{cond: {intProp: {ge: 5}}}, {cond: {intProp: {isNull: true}}}]}", "groupBy{active} count");
        assertThat(rowsBy(rows, "groupBy.active", "count"))
                .containsOnly(entry(true, 2), entry(false, 1));
    }

    @Test
    public void entities_without_the_property_that_is_grouped_by_form_a_group_if_they_satisfy_where() {
        final var rows = aggRows("{not: {cond: {active: {eq: true}}}}", "groupBy{model{key}} count");
        assertThat(rowsBy(rows, "groupBy.model.key", "count"))
                .containsOnly(entry("316", 1), entry(null, 1));
    }

    @Test
    public void where_that_no_entity_satisfies_yields_a_single_row_without_groupBy_and_no_rows_with_groupBy() {
        final var where = "{cond: {key: {eq: \"NONE\"}}}";
        assertThat(aggRows(where, "count")).containsExactly(Map.of("count", 0));
        assertThat(aggRows(where, "groupBy{model{key}} count")).isEmpty();
    }

    @Test
    public void count_of_an_aggregation_query_equals_the_number_of_entities_returned_by_a_data_query_with_the_same_where() {
        List.of("null",
                "{cond: {active: {eq: false}}}",
                "{cond: {desc: {ne: \"Forklift\"}}}",
                "{not: {cond: {desc: {eq: \"Forklift\"}}}}",
                "{cond: {model: {isNull: true}}}",
                "{cond: {model: {cond: {make: {cond: {key: {eq: \"AUDI\"}}}}}}}",
                "{or: [{cond: {intProp: {lt: 2}}}, {cond: {longProp: {gt: 20}}}]}",
                "{and: [{cond: {key: {like: \"VEH*\"}}}, {not: {cond: {intProp: {in: [1, 2]}}}}]}")
            .forEach(where -> {
                final var rows = aggRows(where, "count");
                assertThat(rows).as(where).hasSize(1);
                assertThat(rows.getFirst().get("count")).as(where).isEqualTo(keys(where).size());
            });
    }

    @Test
    public void where_on_an_aggregation_root_field_can_be_given_as_a_variable_of_the_same_condition_type_as_for_the_data_root_field() {
        final var result = webApi.execute(input(
                "query($w: TgWebApiEntity_Cond) {%s(where: $w){count}}".formatted(AGG_ROOT),
                Map.of("w", Map.of("cond", Map.of("key", Map.of("in", List.of(VEH1, VEH2)))))));
        assertNoErrors(result);
        assertThat(listAt(data(result), AGG_ROOT)).containsExactly(Map.of("count", 2));
    }

    @Test
    public void invalid_conditions_on_an_aggregation_root_field_are_errors() {
        assertHasErrors(aggQuery("{}", "count"));
        assertHasErrors(aggQuery("{cond: {key: {eq: \"VEH1\"}}, not: {cond: {key: {eq: \"VEH2\"}}}}", "count"));
        assertHasErrors(aggQuery("{cond: {noSuchProp: {eq: \"x\"}}}", "count"));
        assertHasErrorMessageSatisfying(aggQuery("{cond: {key: {in: []}}}", "count"),
                                        msg -> assertThat(msg).contains(ERR_IN_DOES_NOT_PERMIT_EMPTY_LISTS));
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

        save(new_(TgWebApiEntity.class, VEH1, "Reach stacker, blue").setModel(m316).setIntProp(1).setLongProp(10L)
                     .setBigDecimalProp(new BigDecimal("1.5")).setMoneyProp(new Money("10")).setDateProp(DATE_1).setActive(true)
                     .setHyperlinkProp(new Hyperlink("https://www.fielden.com.au")).setColourProp(new Colour("FF0000")));
        save(new_(TgWebApiEntity.class, VEH2, "Forklift").setModel(m316).setIntProp(2).setLongProp(20L)
                     .setBigDecimalProp(new BigDecimal("2.5")).setMoneyProp(new Money("20")).setDateProp(DATE_2).setActive(false));
        save(new_(TgWebApiEntity.class, VEH3, "reach truck").setModel(a4).setIntProp(5).setLongProp(50L)
                     .setBigDecimalProp(new BigDecimal("4.0")).setMoneyProp(new Money("40")).setDateProp(DATE_3).setActive(true));
        save(new_(TgWebApiEntity.class, VEH4).setActive(false));
        save(new_(TgWebApiEntity.class, VEH5, "Pump").setModel(a4).setActive(true));

        final var m1a = save(new_(Member1.class, "M1A").setActive(true));
        final var m2a = save(new_(Member2.class, "M2A").setActive(true));
        save(new_(UnionOwner.class, "O1").setUnion(new_(Union.class).setMember1(m1a)));
        save(new_(UnionOwner.class, "O2").setUnion(new_(Union.class).setMember2(m2a)));
        save(new_(UnionOwner.class, "O3"));
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Utilities
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    /// Keys of the instances of [TgWebApiEntity] that satisfy `where`, given as a GraphQL literal.
    ///
    private List<String> keys(final String where) {
        final var result = query(where);
        assertNoErrors(result);
        return keysOf(result, ROOT);
    }

    /// Keys of the instances of [UnionOwner] that satisfy `where`, given as a GraphQL literal.
    ///
    private List<String> unionOwnerKeys(final String where) {
        final var result = webApi.execute(input("{%s(where: %s){key}}".formatted(UNION_ROOT, where)));
        assertNoErrors(result);
        return keysOf(result, UNION_ROOT);
    }

    private Map<String, Object> query(final String where) {
        return webApi.execute(input("{%s(where: %s){key}}".formatted(ROOT, where)));
    }

    /// Rows of an aggregation query over [TgWebApiEntity] with `where`, given as a GraphQL literal, and the specified selection.
    ///
    private List<Map<String, Object>> aggRows(final String where, final String selection) {
        final var result = aggQuery(where, selection);
        assertNoErrors(result);
        return listAt(data(result), AGG_ROOT);
    }

    private Map<String, Object> aggQuery(final String where, final String selection) {
        return webApi.execute(input("{%s(where: %s){%s}}".formatted(AGG_ROOT, where, selection)));
    }

    /// Maps the value at `path` in each of `rows`, which must be unique across `rows`, to the value at `rowPath`.
    ///
    private static Map<Object, Object> rowsBy(final List<Map<String, Object>> rows, final String path, final String rowPath) {
        final var result = new HashMap<>();
        rows.forEach(row -> assertThat(result.put(at(row, path), at(row, rowPath))).as("Duplicate group [%s].", at(row, path)).isNull());
        return result;
    }

    /// The value at the dot-separated `path` in `map`, or `null` if any value along it is `null`.
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

    private static List<String> keysOf(final Map<String, Object> result, final String rootField) {
        return listAt(data(result), rootField).stream().map(row -> (String) row.get("key")).toList();
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> listAt(final Map<String, Object> map, final String key) {
        return (List<Map<String, Object>>) map.get(key);
    }

    private static void assertNoErrors(final Map<String, Object> result) {
        final var errors = errors(result);
        assertThat(errors).as(errors.toString()).isEmpty();
    }

    private static void assertHasErrors(final Map<String, Object> result) {
        assertThat(errors(result)).as("Errors are expected.").isNotEmpty();
    }

    private static void assertHasErrorMessageSatisfying(final Map<String, Object> result, final Consumer<? super String> fn) {
        assertThat(errors(result)).anySatisfy(error -> fn.accept((String) ((Map<String, Object>) error).get("message")));
    }

}
