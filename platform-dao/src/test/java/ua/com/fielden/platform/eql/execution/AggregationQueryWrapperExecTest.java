package ua.com.fielden.platform.eql.execution;

import org.junit.Test;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.eql.retrieval.exceptions.EntityRetrievalException;
import ua.com.fielden.platform.eql.stage3.AggregationQueryWrapper;
import ua.com.fielden.platform.sample.domain.*;
import ua.com.fielden.platform.types.Money;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;

/// Operational test for [AggregationQueryWrapper].
///
/// This suite covers the correctness of execution of EQL queries that get transformed by [AggregationQueryWrapper].
/// See `AggregationQueryWrapperTest` for the corresponding denotational tests (which assert the shape of the
/// transformed stage-3 AST rather than the values it produces when executed).
///
/// Each test here mirrors an invariant from the denotational suite, but instead of comparing ASTs it runs the query
/// against the database and asserts the retrieved values.
/// Queries are constructed so that they trigger the transformation -- i.e., they aggregate over a non-persistent
/// operand at the level of the source (a calculated property, an expression, or a sub-query) -- and so that their
/// expected results are easy to compute by hand from the populated domain.
///
public class AggregationQueryWrapperExecTest extends AbstractEqlExecutionTestCase {

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Materialisation of aggregate arguments

    /// `max` and `min` over the same non-persistent property (`sumOfPrices`) materialise it once and both execute correctly.
    ///
    @Test
    public void multiple_aggregations_over_the_same_operand() {
        final var qry = select(TgVehicle.class)
                .yield().maxOf().prop("sumOfPrices").as("maxSum")
                .yield().minOf().prop("sumOfPrices").as("minSum")
                .modelAsAggregate();

        final var row = retrieveOne(qry);
        assertNumericEquals("33", row.get("maxSum"));
        assertNumericEquals("11", row.get("minSum"));
    }

    /// A group-by key that is also yielded as a non-aggregate property is materialised once and referenced by both the
    /// outer `group by` and the outer yield.
    ///
    @Test
    public void groupBy_key_that_is_also_yielded() {
        final var qry = select(TgVehicle.class)
                .groupBy().prop("active")
                .yield().prop("active").as("flag")
                .yield().sumOf().prop("sumOfPrices").as("total")
                .modelAsAggregate();

        final var byFlag = retrieveByKey(qry, "flag");
        assertEquals(2, byFlag.size());
        // active == true:  V1 (11) + V3 (33) = 44
        assertNumericEquals("44", byFlag.get(true).get("total"));
        // active == false: V2 (22)
        assertNumericEquals("22", byFlag.get(false).get("total"));
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Where conditions are applied within the source query

    /// A simple `where` is applied within the source query, so it filters the rows before aggregation.
    ///
    @Test
    public void where_condition_is_applied_before_aggregation() {
        final var qry = select(TgVehicle.class).where()
                .prop("price.amount").gt().val(15)
                .yield().sumOf().prop("sumOfPrices").as(RESULT)
                .modelAsAggregate();

        // Only V2 (price 20) and V3 (price 30) match, so the sum is over their sumOfPrices: 22 + 33 = 55.
        assertNumericEquals("55", retrieveResult(qry));
    }

    /// A compound `where` (with `and`, `or`, and an `exists` sub-query) is applied within the source query.
    ///
    @Test
    public void compound_where_condition_with_exists_is_applied_before_aggregation() {
        final var qry = select(TgVehicle.class).where()
                .prop("active").eq().val(false)
                .or().exists(select(TgFuelUsage.class).where().prop("vehicle").eq().extProp("id").model())
                .yield().sumOf().prop("sumOfPrices").as(RESULT)
                .modelAsAggregate();

        // V1: inactive? no, but has a fuel usage -> included.
        // V2: inactive -> included.
        // V3: active and has no fuel usage -> excluded.
        // Sum over V1 (11) and V2 (22) = 33.
        assertNumericEquals("33", retrieveResult(qry));
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Order by is preserved under transformation

    /// An inline `order by` over a yielded aggregate is preserved by the transformation.
    ///
    @Test
    public void inline_order_by_a_yielded_aggregate_is_preserved() {
        final var qry = select(TgVehicle.class)
                .groupBy().prop("active")
                .orderBy().yield("total").desc()
                .yield().prop("active").as("flag")
                .yield().sumOf().prop("sumOfPrices").as("total")
                .modelAsAggregate();

        final var rows = retrieveAll(qry);
        assertEquals(2, rows.size());
        // Ordered by total descending: active group (44) before inactive group (22).
        assertNumericEquals("44", rows.get(0).get("total"));
        assertNumericEquals("22", rows.get(1).get("total"));
    }

    /// A standalone `order by` (supplied alongside the query model) over a yielded aggregate is preserved by the transformation.
    ///
    @Test
    public void standalone_order_by_a_yielded_aggregate_is_preserved() {
        final var qry = select(TgVehicle.class)
                .groupBy().prop("active")
                .yield().prop("active").as("flag")
                .yield().sumOf().prop("sumOfPrices").as("total")
                .modelAsAggregate();
        final OrderingModel ordering = orderBy().yield("total").asc().model();

        final var rows = retrieveAll(qry, ordering);
        assertEquals(2, rows.size());
        // Ordered by total ascending: inactive group (22) before active group (44).
        assertNumericEquals("22", rows.get(0).get("total"));
        assertNumericEquals("44", rows.get(1).get("total"));
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Queries that are NOT transformed still execute correctly

    /// A query whose every aggregation is over a persistent property is not transformed, yet executes correctly.
    ///
    @Test
    public void aggregation_over_persistent_properties_only() {
        final var qry = select(TgVehicle.class)
                .yield().sumOf().prop("price.amount").as("total")
                .yield().maxOf().prop("price.amount").as("maxPrice")
                .modelAsAggregate();

        final var row = retrieveOne(qry);
        assertNumericEquals("60", row.get("total")); // 10 + 20 + 30
        assertNumericEquals("30", row.get("maxPrice"));
    }

    /// When one aggregation is over a non-persistent operand (`price * 2`) and another is over a persistent property
    /// (`price`), the whole query is transformed and both aggregations execute correctly.
    ///
    @Test
    public void aggregation_over_persistent_property_when_another_aggregation_triggers_the_transformation() {
        final var qry = select(TgVehicle.class)
                .yield().sumOf().beginExpr().prop("price.amount").mult().val(2).endExpr().as("doubled")
                .yield().sumOf().prop("price.amount").as("total")
                .modelAsAggregate();

        final var row = retrieveOne(qry);
        assertNumericEquals("120", row.get("doubled")); // (10 + 20 + 30) * 2
        assertNumericEquals("60", row.get("total"));    // 10 + 20 + 30
    }

    /// An aggregation occurring only within a sub-query does not transform the outer query, but the sub-query itself is
    /// transformed and executes correctly.
    ///
    @Test
    public void aggregation_only_within_a_subquery() {
        final var qry = select(TgVehicle.class)
                .yield().prop("key").as("vehKey")
                .yield().model(select(TgFuelUsage.class).where()
                                       .prop("vehicle").eq().extProp("id")
                                       .yield().maxOf().round().prop("qty").to(2)
                                       .modelAsPrimitive())
                    .as("maxQty")
                .modelAsAggregate();

        final var byKey = retrieveByKey(qry, "vehKey");
        assertEquals(3, byKey.size());
        assertNumericEquals("70", byKey.get("V1").get("maxQty"));  // max(50, 70)
        assertNumericEquals("100", byKey.get("V2").get("maxQty")); // max(100)
        assertNull(byKey.get("V3").get("maxQty"));                 // no fuel usages
    }

    /// Equal property nodes under the same parent are materialised once and reused.
    /// Here `key` is referenced by both the `concatOf` operand and its `order by`; the transformation, triggered by
    /// `sum(price * 2)`, materialises `key` once and reuses it for both.
    ///
    @Test
    public void equal_prop_nodes_under_the_same_parent() {
        final var qry = select(TgVehicle.class)
                .yield().sumOf().beginExpr().prop("price.amount").mult().val(2).endExpr().as("cost")
                .yield().concatOf().prop("key").orderBy().prop("key").asc().separator().val(" ").as("keys")
                .modelAsAggregate();

        final var row = retrieveOne(qry);
        assertNumericEquals("120", row.get("cost"));
        assertEquals("V1 V2 V3", row.get("keys"));
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Properties referenced within the conditions of a `caseWhen` are rewritten.
    // : Each query is grouped by the property referenced by the `caseWhen`, and the transformation is triggered by the
    // : co-occurring aggregation `max(sumOfPrices)`.

    /// A comparison predicate within a `caseWhen` has its property rewritten and executes correctly.
    ///
    @Test
    public void comparison_predicate_in_case_when() {
        final var qry = select(TgVehicle.class)
                .groupBy().prop("key")
                .yield().prop("key").as("vehKey")
                .yield().maxOf().prop("sumOfPrices").as("maxSum")
                .yield().caseWhen().prop("key").eq().val("V1").then().val(1).otherwise().val(0).end().as("flag")
                .modelAsAggregate();

        final var byKey = retrieveByKey(qry, "vehKey");
        assertEquals(3, byKey.size());
        assertNumericEquals("1", byKey.get("V1").get("flag"));
        assertNumericEquals("0", byKey.get("V2").get("flag"));
        assertNumericEquals("0", byKey.get("V3").get("flag"));
        // The triggering aggregation is correct for each single-row group.
        assertNumericEquals("11", byKey.get("V1").get("maxSum"));
        assertNumericEquals("22", byKey.get("V2").get("maxSum"));
        assertNumericEquals("33", byKey.get("V3").get("maxSum"));
    }

    /// A comparison predicate whose operand is nested within another function (`yearOf(initDate)`) is rewritten and
    /// executes correctly.
    ///
    @Test
    public void comparison_predicate_with_nested_operand_in_case_when() {
        final var qry = select(TgVehicle.class)
                .groupBy().prop("initDate")
                .yield().yearOf().prop("initDate").as("yr")
                .yield().maxOf().prop("sumOfPrices").as("maxSum")
                .yield().caseWhen().val(2001).eq().yearOf().prop("initDate").then().val(1).otherwise().val(0).end().as("flag")
                .modelAsAggregate();

        final var byYear = retrieveByKey(qry, "yr");
        assertEquals(3, byYear.size());
        assertNumericEquals("1", byYear.get(2001).get("flag"));
        assertNumericEquals("0", byYear.get(2002).get("flag"));
        assertNumericEquals("0", byYear.get(2003).get("flag"));
        assertNumericEquals("11", byYear.get(2001).get("maxSum"));
        assertNumericEquals("22", byYear.get(2002).get("maxSum"));
        assertNumericEquals("33", byYear.get(2003).get("maxSum"));
    }

    /// A null predicate within a `caseWhen` has its property rewritten and executes correctly.
    ///
    @Test
    public void null_predicate_in_case_when() {
        final var qry = select(TgVehicle.class)
                .groupBy().prop("key")
                .yield().prop("key").as("vehKey")
                .yield().maxOf().prop("sumOfPrices").as("maxSum")
                .yield().caseWhen().prop("key").isNotNull().then().val(1).otherwise().val(0).end().as("flag")
                .modelAsAggregate();

        final var byKey = retrieveByKey(qry, "vehKey");
        assertEquals(3, byKey.size());
        assertNumericEquals("11", byKey.get("V1").get("maxSum"));
        assertNumericEquals("1", byKey.get("V1").get("flag"));
        assertNumericEquals("22", byKey.get("V2").get("maxSum"));
        assertNumericEquals("1", byKey.get("V2").get("flag"));
        assertNumericEquals("33", byKey.get("V3").get("maxSum"));
        assertNumericEquals("1", byKey.get("V3").get("flag"));
    }

    /// A like predicate within a `caseWhen` has its property rewritten and executes correctly.
    ///
    @Test
    public void like_predicate_in_case_when() {
        final var qry = select(TgVehicle.class)
                .groupBy().prop("key")
                .yield().prop("key").as("vehKey")
                .yield().maxOf().prop("sumOfPrices").as("maxSum")
                .yield().caseWhen().prop("key").like().val("%1").then().val(1).otherwise().val(0).end().as("flag")
                .modelAsAggregate();

        final var byKey = retrieveByKey(qry, "vehKey");
        assertEquals(3, byKey.size());
        assertNumericEquals("1", byKey.get("V1").get("flag"));
        assertNumericEquals("11", byKey.get("V1").get("maxSum"));
        assertNumericEquals("0", byKey.get("V2").get("flag"));
        assertNumericEquals("22", byKey.get("V2").get("maxSum"));
        assertNumericEquals("0", byKey.get("V3").get("flag"));
        assertNumericEquals("33", byKey.get("V3").get("maxSum"));
    }

    /// A set predicate within a `caseWhen` has its property rewritten and executes correctly.
    ///
    @Test
    public void set_predicate_in_case_when() {
        final var qry = select(TgVehicle.class)
                .groupBy().prop("key")
                .yield().prop("key").as("vehKey")
                .yield().maxOf().prop("sumOfPrices").as("maxSum")
                .yield().caseWhen().prop("key").in().values("V1", "V2").then().val(1).otherwise().val(0).end().as("flag")
                .modelAsAggregate();

        final var byKey = retrieveByKey(qry, "vehKey");
        assertEquals(3, byKey.size());
        assertNumericEquals("1", byKey.get("V1").get("flag"));
        assertNumericEquals("11", byKey.get("V1").get("maxSum"));
        assertNumericEquals("1", byKey.get("V2").get("flag"));
        assertNumericEquals("22", byKey.get("V2").get("maxSum"));
        assertNumericEquals("0", byKey.get("V3").get("flag"));
        assertNumericEquals("33", byKey.get("V3").get("maxSum"));
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : `concatOf` whose intra-aggregate `order by` references a property that must be materialised.

    /// The `order by` of a `concatOf` references a property (`price`) that appears nowhere else in the query.
    /// The transformation is triggered by the `concatOf` itself.
    /// `price` is materialised so that the outer `concatOf` can order by it.
    ///
    @Test
    public void concatOf_orderBy_property_referenced_nowhere_else() {
        final var qry = select(TgVehicle.class)
                .yield().concatOf().yearOf().prop("initDate").orderBy().prop("price.amount").asc().separator().val(", ").as(RESULT)
                .modelAsAggregate();

        // Ordered by price ascending: V1 (price 10, year 2001), V2 (20, 2002), V3 (30, 2003).
        assertEquals("2001, 2002, 2003", retrieveResult(qry));
    }

    /// A `concatOf` over a persistent property (`key`), ordered by another persistent property (`initDate`), is
    /// materialised when the transformation is triggered by a co-occurring aggregation (`sum(price * 2)`).
    ///
    @Test
    public void concatOf_orderBy_property_when_transformation_triggered_by_another_aggregation() {
        final var qry = select(TgVehicle.class)
                .yield().sumOf().beginExpr().prop("price.amount").mult().val(2).endExpr().as("doubled")
                .yield().concatOf().prop("key").orderBy().prop("initDate").asc().separator().val(", ").as("keys")
                .modelAsAggregate();

        final var row = retrieveOne(qry);
        assertNumericEquals("120", row.get("doubled"));
        // Ordered by initDate ascending: V1 (2001), V2 (2002), V3 (2003).
        assertEquals("V1, V2, V3", row.get("keys"));
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Aggregation over calculated properties

    /// Aggregation over a calculated property whose expression contains no sub-query (`constValueProp` = 10 + 20).
    ///
    @Test
    public void aggregation_over_a_calculated_property_without_a_subquery() {
        final var qry = select(TgVehicle.class)
                .yield().maxOf().prop("constValueProp").as(RESULT)
                .modelAsAggregate();

        assertNumericEquals("30", retrieveResult(qry)); // 10 + 20, constant for every vehicle
    }

    /// Aggregation over a calculated property whose expression is a sub-query (`lastFuelUsageQty`).
    ///
    @Test
    public void aggregation_over_a_calculated_property_with_a_subquery() {
        final var qry = select(TgVehicle.class)
                .yield().maxOf().prop("lastFuelUsageQty").as("maxQty")
                .yield().minOf().prop("lastFuelUsageQty").as("minQty")
                .yield().sumOf().prop("lastFuelUsageQty").as("sumQty")
                .modelAsAggregate();

        // lastFuelUsageQty: V1 -> 70, V2 -> 100, V3 -> null (ignored by aggregates).
        final var row = retrieveOne(qry);
        assertNumericEquals("100", row.get("maxQty"));
        assertNumericEquals("70", row.get("minQty"));
        assertNumericEquals("170", row.get("sumQty"));
    }

    /// Aggregation over a calculated property whose expression contains another calculated property that itself
    /// contains a sub-query (`halfLastFuelUsageQty` = `lastFuelUsageQty / 2`).
    ///
    @Test
    public void aggregation_over_a_nested_calculated_property_with_a_subquery() {
        final var qry = select(TgVehicle.class)
                .yield().maxOf().prop("halfLastFuelUsageQty").as("max")
                .yield().minOf().prop("halfLastFuelUsageQty").as("min")
                .yield().sumOf().prop("halfLastFuelUsageQty").as("sum")
                .modelAsAggregate();

        // halfLastFuelUsageQty: V1 -> 35, V2 -> 50, V3 -> null.
        final var row = retrieveOne(qry);
        assertNumericEquals("50", row.get("max"));
        assertNumericEquals("35", row.get("min"));
        assertNumericEquals("85", row.get("sum"));
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Nested aggregation.
    // : The transformation materialises the outermost aggregate's argument once, without descending into it.

    /// A depth-2 nested aggregation (`sum(max(sumOfPrices))`) is transformed and executes correctly.
    /// The source query materialises `max(sumOfPrices)`, and the outer `sum` over that single row yields the same value.
    ///
    @Test
    public void nested_aggregation_with_depth_2() {
        final var qry = select(TgVehicle.class)
                .yield().sumOf().expr(expr().maxOf().prop("sumOfPrices").model()).as(RESULT)
                .modelAsAggregate();

        // sum over a single row holding max(sumOfPrices) = max(11, 22, 33) = 33.
        assertNumericEquals("33", retrieveResult(qry));
    }

    /// A depth-3 nested aggregation (`avg(sum(max(sumOfPrices)))`) is transformed only once, leaving a nested aggregate (`sum(max(...))`) in the source query.
    /// Such a query is not valid SQL -- aggregate functions cannot be nested -- so its execution fails.
    ///
    @Test
    public void nested_aggregation_with_depth_3_cannot_be_executed() {
        final var qry = select(TgVehicle.class)
                .yield().avgOf().expr(expr().sumOf().expr(expr().maxOf().prop("sumOfPrices").model()).model()).as(RESULT)
                .modelAsAggregate();

        assertThrows(EntityRetrievalException.class, () -> retrieveResult(qry));
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

        final TgVehicleMake make = save(new_(TgVehicleMake.class, "MK1", "Make 1"));
        final TgVehicleModel model = save(new_(TgVehicleModel.class, "MD1", "Model 1").setMake(make));

        // Vehicles (price, purchasePrice) -> sumOfPrices = 1 * price.amount + purchasePrice.amount:
        //   V1: (10, 1) -> 11,  active,    initDate 2001,  lastMeterReading 100
        //   V2: (20, 2) -> 22,  inactive,  initDate 2002,  lastMeterReading 200
        //   V3: (30, 3) -> 33,  active,    initDate 2003,  lastMeterReading null
        final TgVehicle v1 = save(new_(TgVehicle.class, "V1", "Vehicle 1")
                                          .setModel(model)
                                          .setPrice(new Money("10"))
                                          .setPurchasePrice(new Money("1"))
                                          .setActive(true)
                                          .setInitDate(date("2001-01-01 00:00:00"))
                                          .setLastMeterReading(new BigDecimal("100")));
        final TgVehicle v2 = save(new_(TgVehicle.class, "V2", "Vehicle 2")
                                          .setModel(model)
                                          .setPrice(new Money("20"))
                                          .setPurchasePrice(new Money("2"))
                                          .setActive(false)
                                          .setInitDate(date("2002-01-01 00:00:00"))
                                          .setLastMeterReading(new BigDecimal("200")));
        save(new_(TgVehicle.class, "V3", "Vehicle 3")
                     .setModel(model)
                     .setPrice(new Money("30"))
                     .setPurchasePrice(new Money("3"))
                     .setActive(true)
                     .setInitDate(date("2003-01-01 00:00:00")));

        final TgFuelType unleaded = save(new_(TgFuelType.class, "U", "Unleaded"));

        // Fuel usages drive the calculated property lastFuelUsageQty (qty of the most recent fuel usage):
        //   V1: 50 (2005), 70 (2006) -> lastFuelUsageQty 70
        //   V2: 100 (2005)           -> lastFuelUsageQty 100
        //   V3: none                 -> lastFuelUsageQty null
        save(new_composite(TgFuelUsage.class, v1, date("2005-01-01 00:00:00")).setQty(new BigDecimal("50")).setFuelType(unleaded));
        save(new_composite(TgFuelUsage.class, v1, date("2006-01-01 00:00:00")).setQty(new BigDecimal("70")).setFuelType(unleaded));
        save(new_composite(TgFuelUsage.class, v2, date("2005-01-01 00:00:00")).setQty(new BigDecimal("100")).setFuelType(unleaded));
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Utilities
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    private EntityAggregates retrieveOne(final AggregatedResultQueryModel qry) {
        return aggregateDao.getEntity(from(qry).model());
    }

    private List<EntityAggregates> retrieveAll(final AggregatedResultQueryModel qry) {
        return aggregateDao.getAllEntities(from(qry).model());
    }

    private List<EntityAggregates> retrieveAll(final AggregatedResultQueryModel qry, final OrderingModel ordering) {
        return aggregateDao.getAllEntities(from(qry).with(ordering).model());
    }

    /// Retrieves all rows and indexes them by the value of the yield with the given alias.
    /// Suitable only when that yield is non-null and unique across rows (e.g., a group-by key).
    ///
    private Map<Object, EntityAggregates> retrieveByKey(final AggregatedResultQueryModel qry, final CharSequence keyName) {
        final var byKey = new HashMap<Object, EntityAggregates>();
        for (final var agg : retrieveAll(qry)) {
            byKey.put(agg.get(keyName.toString()), agg);
        }
        return byKey;
    }

    private static void assertNumericEquals(final String expected, final Object actual) {
        assertNotNull("Expected a numeric value but got null.", actual);
        assertThat(new BigDecimal(actual.toString())).isEqualByComparingTo(new BigDecimal(expected));
    }

}
