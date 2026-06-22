package ua.com.fielden.platform.eql.stage1;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils;
import ua.com.fielden.platform.eql.meta.EqlStage2TestCase;
import ua.com.fielden.platform.eql.meta.query.QuerySourceInfo;
import ua.com.fielden.platform.eql.meta.query.QuerySourceItemForPrimType;
import ua.com.fielden.platform.eql.stage2.operands.functions.MaxOf2;
import ua.com.fielden.platform.eql.stage2.operands.functions.RoundTo2;
import ua.com.fielden.platform.sample.domain.TgFuelUsage;
import ua.com.fielden.platform.sample.domain.TgVehicle;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.eql.meta.PropType.BIGDECIMAL_PROP_TYPE;

/// Tests for [AggregationQueryWrapper].
///
/// Each test compares an "actual" query (compiled with the transformation enabled) against an "expected" query that
/// describes the expected query shape.
/// The transformation is disabled (via [AggregationQueryWrapper#enabled]) before the "expected" query is compiled, so
/// that its structure is preserved verbatim rather than being affected by the transformation.
/// [#afterTest()] restores the flag after every test to avoid leaking the disabled state into other tests.
///
public class AggregationQueryWrapperTest extends EqlStage2TestCase {

    @After
    public void afterTest() {
        AggregationQueryWrapper.enabled = true;
        AggregationQueryWrapper.resetSourceIdGenerator();
        AggregationQueryWrapper.resetAliasGenerator();
    }

    @Test
    public void multiple_aggregations_over_the_same_operand_share_the_same_column() {
        final var query1 = select(TgVehicle.class)
                .yield().sumOf().prop("sumOfPrices").as("totalPrice")
                .yield().maxOf().prop("sumOfPrices").as("maxPrice")
                .modelAsAggregate();

        final var query2 = select(select(TgVehicle.class)
                                          .yield().prop("sumOfPrices").as("c1")
                                          .modelAsAggregate())
                .yield().sumOf().prop("c1").as("totalPrice")
                .yield().maxOf().prop("c1").as("maxPrice")
                .modelAsAggregate();

        AggregationQueryWrapper.setAliasGenerator(() -> mkAliasGenerator());
        AggregationQueryWrapper.setSourceIdGenerator(mkSourceIdGeneratorFromRange(2));
        final var actual = qry(query1);
        AggregationQueryWrapper.enabled = false;
        final var expected = qry(query2);
        assertEquals(expected, actual);
    }

    /// A group-by key that is also yielded as a non-aggregate property is materialised once (as `c1`) and referenced by
    /// both the outer `group by` and the outer yield.
    ///
    @Test
    public void groupBy_and_yield_that_use_the_same_operand_in_original_query_use_the_same_column_in_transformed_query() {
        final var query1 = select(TgVehicle.class)
                .groupBy().prop("model.key")
                .yield().prop("model.key").as("modelKey")
                .yield().avgOf().prop("sumOfPrices").as("avg")
                .modelAsAggregate();

        final var query2 = select(select(TgVehicle.class)
                                          .yield().prop("model.key").as("c1")
                                          .yield().prop("sumOfPrices").as("c2")
                                          .modelAsAggregate())
                .groupBy().prop("c1")
                .yield().prop("c1").as("modelKey")
                .yield().avgOf().prop("c2").as("avg")
                .modelAsAggregate();

        AggregationQueryWrapper.setAliasGenerator(() -> mkAliasGenerator());
        AggregationQueryWrapper.setSourceIdGenerator(mkSourceIdGeneratorFromRange(2));
        final var actual = qry(query1);
        AggregationQueryWrapper.enabled = false;
        final var expected = qry(query2);
        assertEquals(expected, actual);
    }

    @Test
    public void where_conditions_are_attached_to_the_source_query_and_not_to_the_outer_query_01() {
        final var query1 = select(TgVehicle.class).where()
                .prop("purchasePrice").gt().val(100)
                .yield().maxOf().prop("sumOfPrices").as("maxPrice")
                .modelAsAggregate();

        final var query2 = select(select(TgVehicle.class).where()
                                          .prop("purchasePrice").gt().val(100)
                                          .yield().prop("sumOfPrices").as("c1")
                                          .modelAsAggregate())
                .yield().maxOf().prop("c1").as("maxPrice")
                .modelAsAggregate();

        AggregationQueryWrapper.setAliasGenerator(() -> mkAliasGenerator());
        AggregationQueryWrapper.setSourceIdGenerator(mkSourceIdGeneratorFromRange(2));
        final var actual = qry(query1);
        AggregationQueryWrapper.enabled = false;
        final var expected = qry(query2);
        assertEquals(expected, actual);
    }

    @Test
    public void where_conditions_are_attached_to_the_source_query_and_not_to_the_outer_query_02() {
        final var query1 = select(TgVehicle.class).where()
                .condition(EntityQueryUtils.cond().prop("purchasePrice").gt().val(100).model())
                .and().prop("replacedBy.initDate").isNotNull()
                .or().exists(select(TgFuelUsage.class).where().prop("vehicle").eq().extProp(ID).model())
                .yield().maxOf().prop("sumOfPrices").as("maxPrice")
                .modelAsAggregate();

        final var query2 = select(select(TgVehicle.class).where()
                                          .condition(EntityQueryUtils.cond().prop("purchasePrice").gt().val(100).model())
                                          .and().prop("replacedBy.initDate").isNotNull()
                                          .or().exists(select(TgFuelUsage.class).where().prop("vehicle").eq().extProp(ID).model())
                                          .yield().prop("sumOfPrices").as("c1")
                                          .modelAsAggregate())
                .yield().maxOf().prop("c1").as("maxPrice")
                .modelAsAggregate();

        AggregationQueryWrapper.setAliasGenerator(() -> mkAliasGenerator());
        AggregationQueryWrapper.setSourceIdGenerator(mkSourceIdGeneratorFromRange(3));
        final var actual = qry(query1);
        AggregationQueryWrapper.enabled = false;
        final var expected = qry(query2);
        assertEquals(expected, actual);
    }

    @Test
    public void order_by_yield_is_preserved_under_transformation__standalone_order_by() {
        final var order = orderBy().yield("avgPrice").desc().model();

        final var query1 = select(TgVehicle.class)
                .groupBy().prop("model.key")
                .yield().avgOf().prop("sumOfPrices").as("avgPrice")
                .modelAsAggregate();

        final var query2 = select(select(TgVehicle.class)
                                          .yield().prop("model.key").as("c1")
                                          .yield().prop("sumOfPrices").as("c2")
                                          .modelAsAggregate())
                .groupBy().prop("c1")
                .yield().avgOf().prop("c2").as("avgPrice")
                .modelAsAggregate();

        AggregationQueryWrapper.setAliasGenerator(() -> mkAliasGenerator());
        AggregationQueryWrapper.setSourceIdGenerator(mkSourceIdGeneratorFromRange(2));
        final var actual = qry(query1, order);
        AggregationQueryWrapper.enabled = false;
        final var expected = qry(query2, order);
        assertEquals(expected, actual);
    }

    @Test
    public void order_by_yield_is_preserved_under_transformation__inline_order_by() {
        final var query1 = select(TgVehicle.class)
                .groupBy().prop("model.key")
                .orderBy().yield("avgPrice").desc()
                .yield().avgOf().prop("sumOfPrices").as("avgPrice")
                .modelAsAggregate();

        final var query2 = select(select(TgVehicle.class)
                                          .yield().prop("model.key").as("c1")
                                          .yield().prop("sumOfPrices").as("c2")
                                          .modelAsAggregate())
                .groupBy().prop("c1")
                .orderBy().yield("avgPrice").desc()
                .yield().avgOf().prop("c2").as("avgPrice")
                .modelAsAggregate();

        AggregationQueryWrapper.setAliasGenerator(() -> mkAliasGenerator());
        AggregationQueryWrapper.setSourceIdGenerator(mkSourceIdGeneratorFromRange(2));
        final var actual = qry(query1);
        AggregationQueryWrapper.enabled = false;
        final var expected = qry(query2);
        assertEquals(expected, actual);
    }

    @Test
    public void query_without_aggregation_is_not_transformed() {
        final var query = select(TgVehicle.class).where()
                .prop("price").gt().val(100)
                .yield().prop("key").as("vehicleKey")
                .yield().prop("price").as("vehiclePrice")
                .modelAsAggregate();

        final var actual = qry(query);
        AggregationQueryWrapper.enabled = false;
        final var expected = qry(query);
        assertEquals(expected, actual);
    }

    @Test
    public void aggregation_over_persistent_properties_does_not_trigger_transformation() {
        final var query = select(TgVehicle.class)
                .yield().sumOf().prop("price").as("c1")
                .yield().maxOf().prop("replacedBy.price").as("c2")
                // lastFuelUsage is calculated, but qty is persistent.
                .yield().maxOf().prop("lastFuelUsage.qty").as("c3")
                .modelAsAggregate();

        final var actual = qry(query);
        AggregationQueryWrapper.enabled = false;
        final var expected = qry(query);
        assertEquals(expected, actual);
    }

    /// A query may yield an aggregation over a persistent property (`sum(qty)`) alongside an aggregation over a
    /// non-trivial operand (`sum(qty * 2)`).
    /// The latter triggers the transformation, and the former must be transformed too: its argument is materialised as
    /// a column so that the outer aggregation references a column of the source query, rather than a property of the
    /// original source that the outer query no longer accesses.
    /// A transformation is skipped only when *all* aggregations are over persistent properties.
    ///
    @Test
    public void aggregation_over_persistent_property_is_materialised_when_another_aggregation_triggers_the_transformation() {
        final var query1 = select(TgFuelUsage.class)
                .yield().sumOf().beginExpr().prop("qty").mult().val(2).endExpr().as("doubleQty")
                .yield().sumOf().prop("qty").as("totalQty")
                .modelAsAggregate();

        final var query2 = select(select(TgFuelUsage.class)
                                          .yield().beginExpr().prop("qty").mult().val(2).endExpr().as("c1")
                                          .yield().prop("qty").as("c2")
                                          .modelAsAggregate())
                .yield().sumOf().prop("c1").as("doubleQty")
                .yield().sumOf().prop("c2").as("totalQty")
                .modelAsAggregate();

        AggregationQueryWrapper.setAliasGenerator(() -> mkAliasGenerator());
        AggregationQueryWrapper.setSourceIdGenerator(mkSourceIdGeneratorFromRange(2));
        final var actual = qry(query1);
        AggregationQueryWrapper.enabled = false;
        final var expected = qry(query2);
        assertEquals(expected, actual);
    }

    @Test
    public void query_that_has_aggregation_only_within_a_subquery_is_not_transformed_but_the_subquery_is() {
        final var query1 = select(TgVehicle.class).where()
                .prop("price").gt().val(100)
                .yield().model(select(TgFuelUsage.class).where().prop("vehicle").eq().extProp(ID).yield().maxOf().round().prop("qty").to(2).modelAsPrimitive()).as("maxQty")
                .modelAsAggregate();

        // This is the expected query, and its AST must be constructed by hand because of generated source IDs.
        final var query2 = select(TgVehicle.class)/*ID=2*/.where()
                .prop("price").gt().val(100)
                .yield().model(select(select(TgFuelUsage.class)/*ID=1*/.where()
                                              .prop("vehicle").eq().extProp(ID)
                                              .yield().round().prop("qty").to(2).as("c1")
                                              .modelAsAggregate())/*ID=3*/
                                       .yield().maxOf().prop("c1")
                                       .modelAsPrimitive())
                    .as("maxQty")
                .modelAsAggregate();

        final var source3QuerySourceInfo = new QuerySourceInfo<>(EntityAggregates.class, false, List.of(new QuerySourceItemForPrimType<>("c1", BIGDECIMAL_PROP_TYPE.javaType(), BIGDECIMAL_PROP_TYPE.hibType())));

        final var source2 = source(2, TgVehicle.class);
        final var source1 = source(1, TgFuelUsage.class);
        final var source3 = source(source3QuerySourceInfo, 3,
                                   srcqry(sources(source1),
                                          cond(eq(prop(source1, "vehicle"), prop(source2, ID))),
                                          mkYields(mkYield(new RoundTo2(prop(source1, "qty"), val(2)), "c1"))));
        final var maxQtyYield = subqry(sources(source3), mkYields(mkYield(new MaxOf2(prop(source3, "c1")))), BIGDECIMAL_PROP_TYPE);
        final var expected = qry(sources(source2),
                                 cond(gt(prop(source2, path(TgVehicle.class, "price.amount")), val(100))),
                                 mkYields(mkYield(maxQtyYield, "maxQty")));

        AggregationQueryWrapper.setAliasGenerator(() -> mkAliasGenerator());
        AggregationQueryWrapper.setSourceIdGenerator(mkSourceIdGeneratorFromRange(3));
        final var actual = qry(query1);
        assertEquals(expected, actual);
    }

    // The following tests cover the rewriting of source properties referenced within the conditions of a `caseWhen`.
    // Such a `caseWhen` is yielded alongside an aggregation, which triggers the transformation.
    // The aggregated argument (`price`) becomes `c2`, and the property referenced by the condition becomes `c1`.
    // The grammar does not permit an aggregate function inside a `caseWhen`, so a condition can only reference
    // per-row source properties, never another aggregation.

    @Test
    public void comparison_predicate_in_case_when_has_its_properties_transformed() {
        final var query1 = select(TgVehicle.class)
                .groupBy().prop("key")
                // Aggregation to trigger transformation.
                .yield().sumOf().prop("sumOfPrices").as("total")
                .yield().caseWhen().prop("key").eq().val("ABC").then().val(1).otherwise().val(0).end().as("flag")
                .modelAsAggregate();

        final var query2 = select(select(TgVehicle.class)
                                          .yield().prop("key").as("c1")
                                          .yield().prop("sumOfPrices").as("c2")
                                          .modelAsAggregate())
                .groupBy().prop("c1")
                .yield().sumOf().prop("c2").as("total")
                .yield().caseWhen().prop("c1").eq().val("ABC").then().val(1).otherwise().val(0).end().as("flag")
                .modelAsAggregate();

        AggregationQueryWrapper.setAliasGenerator(() -> mkAliasGenerator());
        AggregationQueryWrapper.setSourceIdGenerator(mkSourceIdGeneratorFromRange(2));
        final var actual = qry(query1);
        AggregationQueryWrapper.enabled = false;
        final var expected = qry(query2);
        assertEquals(expected, actual);
    }

    @Test
    public void comparison_predicate_in_case_when_has_its_properties_transformed__nested_operand_case() {
        final var query1 = select(TgVehicle.class)
                .groupBy().prop("initDate")
                // Aggregation to trigger transformation.
                .yield().sumOf().beginExpr().prop("sumOfPrices").add().val(1).endExpr().as("total")
                // prop("initDate") is nested within another operand secondOf()
                .yield().caseWhen().val(40).eq().secondOf().prop("initDate").then().val(1).otherwise().val(0).end().as("flag")
                .modelAsAggregate();

        final var query2 = select(select(TgVehicle.class)
                                          .yield().prop("initDate").as("c1")
                                          .yield().beginExpr().prop("sumOfPrices").add().val(1).endExpr().as("c2")
                                          .modelAsAggregate())
                .groupBy().prop("c1")
                .yield().sumOf().prop("c2").as("total")
                .yield().caseWhen().val(40).eq().secondOf().prop("c1").then().val(1).otherwise().val(0).end().as("flag")
                .modelAsAggregate();

        AggregationQueryWrapper.setAliasGenerator(() -> mkAliasGenerator());
        AggregationQueryWrapper.setSourceIdGenerator(mkSourceIdGeneratorFromRange(2));
        final var actual = qry(query1);
        AggregationQueryWrapper.enabled = false;
        final var expected = qry(query2);
        assertEquals(expected, actual);
    }

    @Test
    public void null_predicate_in_case_when_has_its_properties_transformed() {
        final var query1 = select(TgVehicle.class)
                .groupBy().prop("initDate")
                // Aggregation to trigger transformation.
                .yield().sumOf().prop("sumOfPrices").as("total")
                .yield().caseWhen().prop("initDate").isNotNull().then().val(1).otherwise().val(0).end().as("flag")
                .modelAsAggregate();

        final var query2 = select(select(TgVehicle.class)
                                          .yield().prop("initDate").as("c1")
                                          .yield().prop("sumOfPrices").as("c2")
                                          .modelAsAggregate())
                .groupBy().prop("c1")
                .yield().sumOf().prop("c2").as("total")
                .yield().caseWhen().prop("c1").isNotNull().then().val(1).otherwise().val(0).end().as("flag")
                .modelAsAggregate();

        AggregationQueryWrapper.setAliasGenerator(() -> mkAliasGenerator());
        AggregationQueryWrapper.setSourceIdGenerator(mkSourceIdGeneratorFromRange(2));
        final var actual = qry(query1);
        AggregationQueryWrapper.enabled = false;
        final var expected = qry(query2);
        assertEquals(expected, actual);
    }

    @Test
    public void like_predicate_in_case_when_has_its_properties_transformed() {
        final var query1 = select(TgVehicle.class)
                .groupBy().prop("key")
                // Aggregation to trigger transformation.
                .yield().sumOf().prop("sumOfPrices").as("total")
                .yield().caseWhen().prop("key").like().val("ABC%").then().val(1).otherwise().val(0).end().as("flag")
                .modelAsAggregate();

        final var query2 = select(select(TgVehicle.class)
                                          .yield().prop("key").as("c1")
                                          .yield().prop("sumOfPrices").as("c2")
                                          .modelAsAggregate())
                .groupBy().prop("c1")
                .yield().sumOf().prop("c2").as("total")
                .yield().caseWhen().prop("c1").like().val("ABC%").then().val(1).otherwise().val(0).end().as("flag")
                .modelAsAggregate();

        AggregationQueryWrapper.setSourceIdGenerator(mkSourceIdGeneratorFromRange(2));
        AggregationQueryWrapper.setAliasGenerator(() -> mkAliasGenerator());
        final var actual = qry(query1);
        AggregationQueryWrapper.enabled = false;
        final var expected = qry(query2);
        assertEquals(expected, actual);
    }

    @Test
    public void set_predicate_in_case_when_has_its_properties_transformed() {
        final var query1 = select(TgVehicle.class)
                .groupBy().prop("key")
                .yield().sumOf().prop("sumOfPrices").as("total")
                .yield().caseWhen().prop("key").in().values("ABC", "DEF").then().val(1).otherwise().val(0).end().as("flag")
                .modelAsAggregate();

        final var query2 = select(select(TgVehicle.class)
                                          .yield().prop("key").as("c1")
                                          .yield().prop("sumOfPrices").as("c2")
                                          .modelAsAggregate())
                .groupBy().prop("c1")
                .yield().sumOf().prop("c2").as("total")
                .yield().caseWhen().prop("c1").in().values("ABC", "DEF").then().val(1).otherwise().val(0).end().as("flag")
                .modelAsAggregate();

        AggregationQueryWrapper.setAliasGenerator(() -> mkAliasGenerator());
        AggregationQueryWrapper.setSourceIdGenerator(mkSourceIdGeneratorFromRange(2));
        final var actual = qry(query1);
        AggregationQueryWrapper.enabled = false;
        final var expected = qry(query2);
        assertEquals(expected, actual);
    }

    @Test
    @Ignore("Nested aggregations are not possible in EQL.")
    public void nested_aggregations_are_not_transformed() {
        // 1. The EQL grammar does not permit an aggregation to be used as an argument of another aggregation.
        /*
        final var query1 = select(TgVehicle.class)
                // No such method `maxOf` in this position.
                .yield().sumOf().maxOf().prop("sumOfPrices").as("total")
                .modelAsAggregate();
        */

        // 2. A calculated property cannot expand into a direct aggregate function call -- that would be an invalid definition,
        // although the EQL grammar does permit such standalone expressions.
        /*
        select(TgVehicle.class)
                .yield().sumOf().prop("calc").as("total")
                .modelAsAggregate();

        cannot expand into

        select(TgVehicle.class)
                .yield().sumOf().maxOf().prop("x").as("total")
                .modelAsAggregate();
        */
    }

    // The following tests cover `concatOf`, whose intra-aggregate `order by` may reference properties that appear
    // nowhere else in the query.
    // Such properties must be materialised as columns of the source query so that the outer `concatOf` can order by them.
    // The aggregated expression is handled like any other aggregate argument.

    /// The `order by` of a `concatOf` references `initDate`, which appears nowhere else in the query.
    /// The transformation is triggered by the `concatOf` itself, whose aggregated argument is a calculated property.
    /// `initDate` must be materialised as a column so that the outer `concatOf` can order by it.
    ///
    @Test
    public void concatOf_orderBy_property_referenced_nowhere_else_is_materialised_and_rewritten() {
        final var query1 = select(TgVehicle.class)
                .yield().concatOf().prop("sumOfPrices").orderBy().prop("initDate").asc().separator().val(", ").as("prices")
                .modelAsAggregate();

        final var query2 = select(select(TgVehicle.class)
                                          .yield().prop("initDate").as("c1")
                                          .yield().prop("sumOfPrices").as("c2")
                                          .modelAsAggregate())
                .yield().concatOf().prop("c2").orderBy().prop("c1").asc().separator().val(", ").as("prices")
                .modelAsAggregate();

        AggregationQueryWrapper.setAliasGenerator(() -> mkAliasGenerator());
        AggregationQueryWrapper.setSourceIdGenerator(mkSourceIdGeneratorFromRange(2));
        final var actual = qry(query1);
        AggregationQueryWrapper.enabled = false;
        final var expected = qry(query2);
        assertEquals(expected, actual);
    }

    /// The `concatOf` aggregates over a persistent property (`qty`) and orders by another persistent property (`date`).
    /// Neither alone would trigger the transformation, but the co-occurring `sum(qty * 2)` does.
    /// Both the aggregated `qty` and the order-by `date` must then be materialised as columns of the source query.
    ///
    @Test
    public void concatOf_orderBy_property_is_materialised_when_transformation_triggered_by_another_aggregation() {
        final var query1 = select(TgFuelUsage.class)
                .yield().sumOf().beginExpr().prop("qty").mult().val(2).endExpr().as("doubleSum")
                .yield().concatOf().prop("qty").orderBy().prop("date").asc().separator().val(", ").as("qtys")
                .modelAsAggregate();

        final var query2 = select(select(TgFuelUsage.class)
                                          .yield().prop("date").as("c1")
                                          .yield().beginExpr().prop("qty").mult().val(2).endExpr().as("c2")
                                          .yield().prop("qty").as("c3")
                                          .modelAsAggregate())
                .yield().concatOf().prop("c3").orderBy().prop("c1").asc().separator().val(", ").as("qtys")
                .yield().sumOf().prop("c2").as("doubleSum")
                .modelAsAggregate();

        AggregationQueryWrapper.setAliasGenerator(() -> mkAliasGenerator());
        AggregationQueryWrapper.setSourceIdGenerator(mkSourceIdGeneratorFromRange(2));
        final var actual = qry(query1);
        AggregationQueryWrapper.enabled = false;
        final var expected = qry(query2);
        assertEquals(expected, actual);
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Utilities
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    private Stream<String> mkAliasGenerator() {
        return IntStream.iterate(1, i -> i + 1).mapToObj(i -> "c" + i);
    }

    private Supplier<Integer> mkSourceIdGeneratorFromRange(final int start) {
        return new Supplier<>() {
            int n = start;

            @Override
            public Integer get() {
                return n++;
            }
        };
    }

}
