package ua.com.fielden.platform.eql.stage3;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils;
import ua.com.fielden.platform.eql.meta.EqlStage3TestCase;
import ua.com.fielden.platform.eql.meta.EqlTestCase;
import ua.com.fielden.platform.sample.domain.TgFuelUsage;
import ua.com.fielden.platform.sample.domain.TgVehicle;

import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.query.DbVersion.MSSQL;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;

/// Structural tests for [AggregateOperandMaterialiser].
///
/// Each test compiles an "actual" query with the transformation enabled and compares it against an "expected" query
/// obtained by compiling, with the transformation disabled, an EQL model that reflects the transformed shape.
/// The comparison is by alpha-equivalence ([EqlStage3TestCase#assertAlphaEq]) rather than structural equality, because
/// the transformation generates fresh source and SQL identifiers that a recompiled model would not reproduce.
/// Alpha-equivalence disregards exactly those generated identifiers (renaming bound source ids, ignoring derived columns),
/// while still comparing structure, yield aliases, types, operators, and the source that each property resolves to.
///
/// Queries that are not eligible for the transformation are tested differently: by structural equality
/// ([EqlStage3TestCase#assertStructEq]) against the same query compiled with the transformation disabled, asserting
/// that the transformation is a no-op.
///
/// [AggregateOperandMaterialiser] applies only to [DbVersion#MSSQL], which requires the [DbVersion] to be adjusted before each test
/// and restored afterwards.
/// This remains necessary until the base test class [EqlTestCase] is refactored using IoC.
///
public class AggregateOperandMaterialiserTest extends EqlStage3TestCase {

    private DbVersion prevDbVersion;

    @Before
    public void setup() {
        prevDbVersion = dbVersion();
        setDbVersion(MSSQL);
    }

    @After
    public void afterTest() {
        AggregateOperandMaterialiser.enabled = true;
        setDbVersion(prevDbVersion);
    }

    @Test
    public void multiple_aggregations_over_the_same_operand_share_the_same_column() {
        final var actualEql = select(TgVehicle.class)
                .yield().maxOf().absOf().prop("id").as("maxId")
                .yield().minOf().absOf().prop("id").as("minId")
                .modelAsAggregate();

        final var expectedEql = select(select(TgVehicle.class)
                                          .yield().absOf().prop("id").as("c1")
                                          .modelAsAggregate())
                .yield().maxOf().prop("c1").as("maxId")
                .yield().minOf().prop("c1").as("minId")
                .modelAsAggregate();

        final var actual = qry(actualEql);
        AggregateOperandMaterialiser.enabled = false;
        final var expected = qry(expectedEql);
        assertAlphaEq(expected, actual);
    }

    /// A group-by key that is also yielded as a non-aggregate property is materialised once (as `c2`) and referenced by
    /// both the outer `group by` and the outer yield.
    ///
    @Test
    public void groupBy_and_yield_that_use_the_same_operand_in_original_query_use_the_same_column_in_transformed_query() {
        final var actualEql = select(TgVehicle.class)
                .groupBy().prop("key")
                .yield().prop("key").as("vehKey")
                .yield().avgOf().absOf().prop("id").as("avg")
                .modelAsAggregate();

        final var expectedEql = select(select(TgVehicle.class)
                                          .yield().absOf().prop("id").as("c1")
                                          .yield().prop("key").as("c2")
                                          .modelAsAggregate())
                .groupBy().prop("c2")
                .yield().prop("c2").as("vehKey")
                .yield().avgOf().prop("c1").as("avg")
                .modelAsAggregate();

        final var actual = qry(actualEql);
        AggregateOperandMaterialiser.enabled = false;
        final var expected = qry(expectedEql);
        assertAlphaEq(expected, actual);
    }

    @Test
    public void where_conditions_are_attached_to_the_source_query_and_not_to_the_outer_query_01() {
        final var actualEql = select(TgVehicle.class).where()
                .prop("purchasePrice").gt().val(100)
                // To trigger transformation.
                .yield().maxOf().absOf().prop("id").as("maxId")
                .modelAsAggregate();

        final var expectedEql = select(select(TgVehicle.class).where()
                                          .prop("purchasePrice").gt().val(100)
                                          .yield().absOf().prop("id").as("c1")
                                          .modelAsAggregate())
                .yield().maxOf().prop("c1").as("maxId")
                .modelAsAggregate();

        final var actual = qry(actualEql);
        AggregateOperandMaterialiser.enabled = false;
        final var expected = qry(expectedEql);
        assertAlphaEq(expected, actual);
    }

    @Test
    public void where_conditions_are_attached_to_the_source_query_and_not_to_the_outer_query_02() {
        final var actualEql = select(TgVehicle.class).where()
                .condition(EntityQueryUtils.cond().prop("purchasePrice").gt().val(100).model())
                .and().prop("replacedBy").isNotNull()
                .or().exists(select(TgFuelUsage.class).where().prop("vehicle").eq().extProp(ID).model())
                .yield().maxOf().prop("sumOfPrices").as("maxPrice")
                .modelAsAggregate();

        final var expectedEql = select(select(TgVehicle.class).where()
                                          .condition(EntityQueryUtils.cond().prop("purchasePrice").gt().val(100).model())
                                          .and().prop("replacedBy").isNotNull()
                                          .or().exists(select(TgFuelUsage.class).where().prop("vehicle").eq().extProp(ID).model())
                                          .yield().prop("sumOfPrices").as("c1")
                                          .modelAsAggregate())
                .yield().maxOf().prop("c1").as("maxPrice")
                .modelAsAggregate();

        final var actual = qry(actualEql);
        AggregateOperandMaterialiser.enabled = false;
        final var expected = qry(expectedEql);
        assertAlphaEq(expected, actual);
    }

    @Test
    public void order_by_yield_is_preserved_under_transformation__standalone_order_by() {
        final var actualEql = select(TgVehicle.class)
                .groupBy().prop("key")
                .orderBy().yield("maxId").desc()
                .yield().maxOf().absOf().prop("id").as("maxId")
                .modelAsAggregate();

        final var expectedEql = select(select(TgVehicle.class)
                                          .yield().absOf().prop("id").as("c1")
                                          .yield().prop("key").as("c2")
                                          .modelAsAggregate())
                .groupBy().prop("c2")
                .orderBy().yield("maxId").desc()
                .yield().maxOf().prop("c1").as("maxId")
                .modelAsAggregate();

        final var actual = qry(actualEql);
        AggregateOperandMaterialiser.enabled = false;
        final var expected = qry(expectedEql);
        assertAlphaEq(expected, actual);
    }

    @Test
    public void order_by_yield_is_preserved_under_transformation__inline_order_by() {
        final var actualEql = select(TgVehicle.class)
                .groupBy().prop("key")
                .orderBy().yield("maxId").desc()
                .yield().maxOf().absOf().prop("id").as("maxId")
                .modelAsAggregate();

        final var expectedEql = select(select(TgVehicle.class)
                                          .yield().absOf().prop("id").as("c1")
                                          .yield().prop("key").as("c2")
                                          .modelAsAggregate())
                .groupBy().prop("c2")
                .orderBy().yield("maxId").desc()
                .yield().maxOf().prop("c1").as("maxId")
                .modelAsAggregate();

        final var actual = qry(actualEql);
        AggregateOperandMaterialiser.enabled = false;
        final var expected = qry(expectedEql);
        assertAlphaEq(expected, actual);
    }

    /// An aggregation within an order-by has its argument materialised, just like an aggregation within a yield.
    /// Otherwise, the outer order-by would aggregate over the original source, which the outer query no longer accesses.
    ///
    @Test
    public void order_by_an_aggregation_has_its_argument_materialised() {
        final var order = orderBy().expr(expr().maxOf().beginExpr().prop("qty").div().val(3).endExpr().model()).asc().model();
        final var actualEql = select(TgFuelUsage.class)
                .yield().sumOf().beginExpr().prop("qty").mult().val(2).endExpr().as("doubleQty")
                .modelAsAggregate();

        final var expectedEql = select(select(TgFuelUsage.class)
                                          .yield().beginExpr().prop("qty").mult().val(2).endExpr().as("c1")
                                          .yield().beginExpr().prop("qty").div().val(3).endExpr().as("c2")
                                          .modelAsAggregate())
                .orderBy().expr(expr().maxOf().prop("c2").model()).asc()
                .yield().sumOf().prop("c1").as("doubleQty")
                .modelAsAggregate();

        final var actual = qry(actualEql, order);
        AggregateOperandMaterialiser.enabled = false;
        final var expected = qry(expectedEql);
        assertAlphaEq(expected, actual);
    }

    /// An aggregation occurring only in an order-by (with none in the yields) makes the query eligible for the
    /// transformation on its own.
    ///
    @Test
    public void an_order_by_aggregation_alone_triggers_the_transformation() {
        final var order = orderBy().expr(expr().maxOf().beginExpr().prop("qty").mult().val(2).endExpr().model()).desc().model();
        final var actualEql = select(TgFuelUsage.class)
                .groupBy().prop("date")
                .yield().prop("date").as("d")
                .modelAsAggregate();

        final var expectedEql = select(select(TgFuelUsage.class)
                                          .yield().beginExpr().prop("qty").mult().val(2).endExpr().as("c1")
                                          .yield().prop("date").as("c2")
                                          .modelAsAggregate())
                .groupBy().prop("c2")
                .orderBy().expr(expr().maxOf().prop("c1").model()).desc()
                .yield().prop("c2").as("d")
                .modelAsAggregate();

        final var actual = qry(actualEql, order);
        AggregateOperandMaterialiser.enabled = false;
        final var expected = qry(expectedEql);
        assertAlphaEq(expected, actual);
    }

    @Test
    public void query_without_aggregation_is_not_transformed() {
        final var query = select(TgVehicle.class).where()
                .prop("price").gt().val(100)
                .yield().prop("key").as("vehicleKey")
                .yield().prop("price").as("vehiclePrice")
                .modelAsAggregate();

        final var actual = qry(query);
        AggregateOperandMaterialiser.enabled = false;
        final var expected = qry(query);
        assertStructEq(expected, actual);
    }

    /// Group-by operands alone do not make a query eligible for the transformation, regardless of their complexity.
    /// A non-aggregating query with a non-trivial group-by operand is evaluated by SQL Server natively.
    ///
    @Test
    public void non_aggregating_query_with_a_non_trivial_groupBy_operand_is_not_transformed() {
        final var query = select(TgVehicle.class)
                .groupBy().lowerCase().prop("key")
                .yield().lowerCase().prop("key").as("k")
                .modelAsAggregate();

        final var actual = qry(query);
        AggregateOperandMaterialiser.enabled = false;
        final var expected = qry(query);
        assertStructEq(expected, actual);
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
        AggregateOperandMaterialiser.enabled = false;
        final var expected = qry(query);
        assertStructEq(expected, actual);
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
        final var actualEql = select(TgFuelUsage.class)
                .yield().sumOf().beginExpr().prop("qty").mult().val(2).endExpr().as("doubleQty")
                .yield().sumOf().prop("qty").as("totalQty")
                .modelAsAggregate();

        final var expectedEql = select(select(TgFuelUsage.class)
                                          .yield().beginExpr().prop("qty").mult().val(2).endExpr().as("c1")
                                          .yield().prop("qty").as("c2")
                                          .modelAsAggregate())
                .yield().sumOf().prop("c1").as("doubleQty")
                .yield().sumOf().prop("c2").as("totalQty")
                .modelAsAggregate();

        final var actual = qry(actualEql);
        AggregateOperandMaterialiser.enabled = false;
        final var expected = qry(expectedEql);
        assertAlphaEq(expected, actual);
    }

    @Test
    public void query_that_has_aggregation_only_within_a_subquery_is_not_transformed_but_the_subquery_is() {
        final var actualEql = select(TgVehicle.class)
                .yield().model(select(TgFuelUsage.class).where().prop("vehicle").eq().extProp(ID).yield().maxOf().round().prop("qty").to(2).modelAsPrimitive()).as("maxQty")
                .modelAsAggregate();

        final var expectedEql = select(TgVehicle.class)
                .yield().model(select(select(TgFuelUsage.class).where()
                                              .prop("vehicle").eq().extProp(ID)
                                              .yield().round().prop("qty").to(2).as("c1")
                                              .modelAsAggregate())
                                       .yield().maxOf().prop("c1")
                                       .modelAsPrimitive())
                    .as("maxQty")
                .modelAsAggregate();

        final var actual = qry(actualEql);
        AggregateOperandMaterialiser.enabled = false;
        final var expected = qry(expectedEql);
        assertAlphaEq(expected, actual);
    }

    @Test
    public void equal_prop_nodes_under_the_same_parent_node_are_replaced_by_the_same_materialised_prop() {
        final var actualEql = select(TgVehicle.class)
                // To trigger transformation.
                .yield().sumOf().beginExpr().prop("price").mult().val(2).endExpr().as("cost")
                // prop("key") -- 2 equal nodes that should be replaced by the same materialised prop.
                .yield().concatOf().prop("key").orderBy().prop("key").asc().separator().val(" ").as("keys")
                .modelAsAggregate();

        final var expectedEql = select(select(TgVehicle.class)
                                          .yield().beginExpr().prop("price").mult().val(2).endExpr().as("c1")
                                          .yield().prop("key").as("c2")
                                          .modelAsAggregate())
                .yield().sumOf().prop("c1").as("cost")
                .yield().concatOf().prop("c2").orderBy().prop("c2").asc().separator().val(" ").as("keys")
                .modelAsAggregate();

        final var actual = qry(actualEql);
        AggregateOperandMaterialiser.enabled = false;
        final var expected = qry(expectedEql);
        assertAlphaEq(expected, actual);
    }

    // The following tests cover the rewriting of source properties referenced within the conditions of a `caseWhen`.
    // Such a `caseWhen` is yielded alongside an aggregation, which triggers the transformation.
    // The aggregated argument (`price`) becomes `c2`, and the property referenced by the condition becomes `c1`.
    // The grammar does not permit an aggregate function inside a `caseWhen`, so a condition can only reference
    // per-row source properties, never another aggregation.

    @Test
    public void comparison_predicate_in_case_when_has_its_properties_transformed() {
        final var actualEql = select(TgVehicle.class)
                .groupBy().prop("key")
                // Aggregation to trigger transformation.
                .yield().maxOf().absOf().prop("id").as("maxId")
                .yield().caseWhen().prop("key").eq().val("ABC").then().val(1).otherwise().val(0).end().as("flag")
                .modelAsAggregate();

        final var expectedEql = select(select(TgVehicle.class)
                                          .yield().absOf().prop("id").as("c1")
                                          .yield().prop("key").as("c2")
                                          .modelAsAggregate())
                .groupBy().prop("c2")
                .yield().maxOf().prop("c1").as("maxId")
                .yield().caseWhen().prop("c2").eq().val("ABC").then().val(1).otherwise().val(0).end().as("flag")
                .modelAsAggregate();

        final var actual = qry(actualEql);
        AggregateOperandMaterialiser.enabled = false;
        final var expected = qry(expectedEql);
        assertAlphaEq(expected, actual);
    }

    @Test
    public void comparison_predicate_in_case_when_has_its_properties_transformed__nested_operand_case() {
        final var actualEql = select(TgVehicle.class)
                .groupBy().prop("initDate")
                // Aggregation to trigger transformation.
                .yield().maxOf().absOf().prop("id").as("maxId")
                // prop("initDate") is nested within another operand secondOf()
                .yield().caseWhen().val(40).eq().secondOf().prop("initDate").then().val(1).otherwise().val(0).end().as("flag")
                .modelAsAggregate();

        final var expectedEql = select(select(TgVehicle.class)
                                          .yield().absOf().prop("id").as("c1")
                                          .yield().prop("initDate").as("c2")
                                          .modelAsAggregate())
                .groupBy().prop("c2")
                .yield().maxOf().prop("c1").as("maxId")
                .yield().caseWhen().val(40).eq().secondOf().prop("c2").then().val(1).otherwise().val(0).end().as("flag")
                .modelAsAggregate();

        final var actual = qry(actualEql);
        AggregateOperandMaterialiser.enabled = false;
        final var expected = qry(expectedEql);
        assertAlphaEq(expected, actual);
    }

    @Test
    public void null_predicate_in_case_when_has_its_properties_transformed() {
        final var actualEql = select(TgVehicle.class)
                .groupBy().prop("initDate")
                // Aggregation to trigger transformation.
                .yield().maxOf().absOf().prop("id").as("maxId")
                .yield().caseWhen().prop("initDate").isNotNull().then().val(1).otherwise().val(0).end().as("flag")
                .modelAsAggregate();

        final var expectedEql = select(select(TgVehicle.class)
                                          .yield().absOf().prop("id").as("c1")
                                          .yield().prop("initDate").as("c2")
                                          .modelAsAggregate())
                .groupBy().prop("c2")
                .yield().maxOf().prop("c1").as("maxId")
                .yield().caseWhen().prop("c2").isNotNull().then().val(1).otherwise().val(0).end().as("flag")
                .modelAsAggregate();

        final var actual = qry(actualEql);
        AggregateOperandMaterialiser.enabled = false;
        final var expected = qry(expectedEql);
        assertAlphaEq(expected, actual);
    }

    @Test
    public void like_predicate_in_case_when_has_its_properties_transformed() {
        final var actualEql = select(TgVehicle.class)
                .groupBy().prop("key")
                // Aggregation to trigger transformation.
                .yield().maxOf().absOf().prop("id").as("maxId")
                .yield().caseWhen().prop("key").like().val("ABC%").then().val(1).otherwise().val(0).end().as("flag")
                .modelAsAggregate();

        final var expectedEql = select(select(TgVehicle.class)
                                          .yield().absOf().prop("id").as("c1")
                                          .yield().prop("key").as("c2")
                                          .modelAsAggregate())
                .groupBy().prop("c2")
                .yield().maxOf().prop("c1").as("maxId")
                .yield().caseWhen().prop("c2").like().val("ABC%").then().val(1).otherwise().val(0).end().as("flag")
                .modelAsAggregate();

        final var actual = qry(actualEql);
        AggregateOperandMaterialiser.enabled = false;
        final var expected = qry(expectedEql);
        assertAlphaEq(expected, actual);
    }

    @Test
    public void set_predicate_in_case_when_has_its_properties_transformed() {
        final var actualEql = select(TgVehicle.class)
                .groupBy().prop("key")
                // Aggregation to trigger transformation.
                .yield().maxOf().absOf().prop("id").as("maxId")
                .yield().caseWhen().prop("key").in().values("ABC", "DEF").then().val(1).otherwise().val(0).end().as("flag")
                .modelAsAggregate();

        final var expectedEql = select(select(TgVehicle.class)
                                          .yield().absOf().prop("id").as("c1")
                                          .yield().prop("key").as("c2")
                                          .modelAsAggregate())
                .groupBy().prop("c2")
                .yield().maxOf().prop("c1").as("maxId")
                .yield().caseWhen().prop("c2").in().values("ABC", "DEF").then().val(1).otherwise().val(0).end().as("flag")
                .modelAsAggregate();

        final var actual = qry(actualEql);
        AggregateOperandMaterialiser.enabled = false;
        final var expected = qry(expectedEql);
        assertAlphaEq(expected, actual);
    }

    // The following tests cover `concatOf`, whose intra-aggregate `order by` may reference properties that appear
    // nowhere else in the query.
    // Such properties must be materialised as columns of the source query so that the outer `concatOf` can order by them.
    // The aggregated expression is handled like any other aggregate argument.

    /// The `order by` of a `concatOf` references `initDate`, which appears nowhere else in the query.
    /// The transformation is triggered by the `concatOf` itself, whose aggregated argument is an expression.
    /// `initDate` must be materialised as a column so that the outer `concatOf` can order by it.
    ///
    @Test
    public void concatOf_orderBy_property_referenced_nowhere_else_is_materialised_and_rewritten() {
        final var actualEql = select(TgVehicle.class)
                .yield().concatOf().absOf().prop("id").orderBy().prop("initDate").asc().separator().val(", ").as("ids")
                .modelAsAggregate();

        final var expectedEql = select(select(TgVehicle.class)
                                          .yield().absOf().prop("id").as("c1")
                                          .yield().prop("initDate").as("c2")
                                          .modelAsAggregate())
                .yield().concatOf().prop("c1").orderBy().prop("c2").asc().separator().val(", ").as("ids")
                .modelAsAggregate();

        final var actual = qry(actualEql);
        AggregateOperandMaterialiser.enabled = false;
        final var expected = qry(expectedEql);
        assertAlphaEq(expected, actual);
    }

    /// The `concatOf` aggregates over a persistent property (`qty`) and orders by another persistent property (`date`).
    /// Neither alone would trigger the transformation, but the co-occurring `sum(qty * 2)` does.
    /// Both the aggregated `qty` and the order-by `date` must then be materialised as columns of the source query.
    ///
    @Test
    public void concatOf_orderBy_property_is_materialised_when_transformation_triggered_by_another_aggregation() {
        final var actualEql = select(TgFuelUsage.class)
                .yield().sumOf().beginExpr().prop("qty").mult().val(2).endExpr().as("doubleSum")
                .yield().concatOf().prop("qty").orderBy().prop("date").asc().separator().val(", ").as("qtys")
                .modelAsAggregate();

        final var expectedEql = select(select(TgFuelUsage.class)
                                          .yield().beginExpr().prop("qty").mult().val(2).endExpr().as("c1")
                                          .yield().prop("qty").as("c2")
                                          .yield().prop("date").as("c3")
                                          .modelAsAggregate())
                .yield().concatOf().prop("c2").orderBy().prop("c3").asc().separator().val(", ").as("qtys")
                .yield().sumOf().prop("c1").as("doubleSum")
                .modelAsAggregate();

        final var actual = qry(actualEql);
        AggregateOperandMaterialiser.enabled = false;
        final var expected = qry(expectedEql);
        assertAlphaEq(expected, actual);
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Group-by over a sub-query.
    // : A sub-query used as a group-by item is materialised as a column of the source query, so that the outer `group by`
    // : references that column instead of a sub-query that correlates to the original source the outer query no longer accesses.
    // : The transformation is triggered by the co-occurring aggregation over a non-persistent operand.

    @Test
    public void groupBy_a_subquery_that_is_not_yielded_materialises_it_as_a_column() {
        final var countFuelUsage = select(TgFuelUsage.class).where().prop("vehicle").eq().extProp(ID).yield().countAll().modelAsPrimitive();
        final var actualEql = select(TgVehicle.class)
                .groupBy().model(countFuelUsage)
                .yield().sumOf().prop("sumOfPrices").as("total")
                .modelAsAggregate();

        final var expectedEql = select(select(TgVehicle.class)
                                          .yield().beginExpr().val(1).mult().prop("price.amount").add().prop("purchasePrice.amount").endExpr().as("c1")
                                          .yield().model(countFuelUsage).as("c2")
                                          .modelAsAggregate())
                .groupBy().prop("c2")
                .yield().sumOf().prop("c1").as("total")
                .modelAsAggregate();

        final var actual = qry(actualEql);
        AggregateOperandMaterialiser.enabled = false;
        final var expected = qry(expectedEql);
        assertAlphaEq(expected, actual);
    }

    /// `count(*)` is an aggregation without an argument: it contributes nothing to materialisation, but it does make
    /// the query eligible for the transformation, enabling the materialisation of the group-by subquery.
    ///
    @Test
    public void groupBy_a_subquery_yielding_only_countAll_is_transformed() {
        final var countFuelUsage = select(TgFuelUsage.class).where().prop("vehicle").eq().extProp(ID).yield().countAll().modelAsPrimitive();
        final var actualEql = select(TgVehicle.class)
                .groupBy().model(countFuelUsage)
                .yield().countAll().as("n")
                .modelAsAggregate();

        final var expectedEql = select(select(TgVehicle.class)
                                          .yield().model(countFuelUsage).as("c1")
                                          .modelAsAggregate())
                .groupBy().prop("c1")
                .yield().countAll().as("n")
                .modelAsAggregate();

        final var actual = qry(actualEql);
        AggregateOperandMaterialiser.enabled = false;
        final var expected = qry(expectedEql);
        assertAlphaEq(expected, actual);
    }

    @Test
    public void groupBy_a_calculated_property_that_contains_a_subquery_and_is_not_yielded_materialises_it_as_a_column() {
        final var actualEql = select(TgVehicle.class)
                .groupBy().prop("lastFuelUsageQty")
                .yield().sumOf().prop("sumOfPrices").as("total")
                .modelAsAggregate();

        final var expectedEql = select(select(TgVehicle.class)
                                          .yield().beginExpr().val(1).mult().prop("price.amount").add().prop("purchasePrice.amount").endExpr().as("c1")
                                          .yield().prop("lastFuelUsageQty").as("c2")
                                          .modelAsAggregate())
                .groupBy().prop("c2")
                .yield().sumOf().prop("c1").as("total")
                .modelAsAggregate();

        final var actual = qry(actualEql);
        AggregateOperandMaterialiser.enabled = false;
        final var expected = qry(expectedEql);
        assertAlphaEq(expected, actual);
    }

    // The following tests cover subqueries that appear in a yield or an order-by, outside of an aggregate function's
    // argument. The replacement operation does not descend into subqueries, so such a subquery can take part in the
    // transformation only by being materialised as a whole column.
    // When the subquery is also grouped by, it is materialised (as a group-by operand) and the yielded or ordered-by
    // occurrence, being alpha-equivalent to it, reuses that column -- see the `..._is_materialised_under_one_column` tests.
    // Otherwise the subquery is left unmaterialised: it would keep referencing the original source that the outer query
    // no longer accesses, so the transformation is skipped, leaving the query untouched -- see the `..._skips_...` tests.

    @Test
    public void same_subquery_used_in_groupBy_and_yield_is_materialised_under_one_column() {
        final var countFuelUsage = select(TgFuelUsage.class).where().prop("vehicle").eq().extProp(ID).yield().countAll().modelAsPrimitive();
        final var query = select(TgVehicle.class)
                .groupBy().model(countFuelUsage)
                .yield().model(countFuelUsage).as("count")
                .yield().sumOf().prop("sumOfPrices").as("total")
                .modelAsAggregate();
        final var expectedEql = select(
                    select(TgVehicle.class)
                            .yield().model(countFuelUsage).as("c2")
                            .yield().prop("sumOfPrices").as("c1")
                            .modelAsAggregate()
                )
                .groupBy().prop("c2")
                .yield().prop("c2").as("count")
                .yield().sumOf().prop("c1").as("total")
                .modelAsAggregate();

        final var actual = qry(query);
        AggregateOperandMaterialiser.enabled = false;
        final var expected = qry(expectedEql);
        assertAlphaEq(expected, actual);
    }

    @Test
    public void subquery_used_in_groupBy_and_part_of_a_yield_is_materialised_under_one_column() {
        final var countFuelUsage = select(TgFuelUsage.class).where().prop("vehicle").eq().extProp(ID).yield().countAll().modelAsPrimitive();
        final var query = select(TgVehicle.class)
                .groupBy().model(countFuelUsage)
                .yield().absOf().model(countFuelUsage).as("count")
                .yield().sumOf().prop("sumOfPrices").as("total")
                .modelAsAggregate();
        final var expectedEql = select(
                select(TgVehicle.class)
                        .yield().model(countFuelUsage).as("c2")
                        .yield().prop("sumOfPrices").as("c1")
                        .modelAsAggregate()
        )
                .groupBy().prop("c2")
                .yield().absOf().prop("c2").as("count")
                .yield().sumOf().prop("c1").as("total")
                .modelAsAggregate();

        final var actual = qry(query);
        AggregateOperandMaterialiser.enabled = false;
        final var expected = qry(expectedEql);
        assertAlphaEq(expected, actual);
    }

    /// A subquery in an order-by (outside of an aggregate argument) prevents the transformation for the same reason
    /// as a yielded subquery.
    /// The same query without the order-by is transformed (see `groupBy_a_subquery_that_is_not_yielded_materialises_it_as_a_column`).
    ///
    @Test
    public void same_subquery_used_in_groupBy_and_orderBy_is_materialised_under_one_column() {
        final var countFuelUsage = select(TgFuelUsage.class).where().prop("vehicle").eq().extProp(ID).yield().countAll().modelAsPrimitive();
        final var actualEql = select(TgVehicle.class)
                .groupBy().model(countFuelUsage)
                .orderBy().model(countFuelUsage).desc()
                .yield().sumOf().prop("sumOfPrices").as("total")
                .modelAsAggregate();
        final var expectedEql = select(
                select(TgVehicle.class)
                        .yield().model(countFuelUsage).as("c2")
                        .yield().prop("sumOfPrices").as("c1")
                        .modelAsAggregate()
        )
                .groupBy().prop("c2")
                .orderBy().prop("c2").desc()
                .yield().sumOf().prop("c1").as("total")
                .modelAsAggregate();

        final var actual = qry(actualEql);
        AggregateOperandMaterialiser.enabled = false;
        final var expected = qry(expectedEql);
        assertAlphaEq(expected, actual);
    }

    /// A subquery in an order-by (outside of an aggregate argument) prevents the transformation for the same reason
    /// as a yielded subquery.
    /// The same query without the order-by is transformed (see `groupBy_a_subquery_that_is_not_yielded_materialises_it_as_a_column`).
    ///
    @Test
    public void subquery_used_in_groupBy_and_part_of_orderBy_is_materialised_under_one_column() {
        final var countFuelUsage = select(TgFuelUsage.class).where().prop("vehicle").eq().extProp(ID).yield().countAll().modelAsPrimitive();
        final var actualEql = select(TgVehicle.class)
                .groupBy().model(countFuelUsage)
                .orderBy().absOf().model(countFuelUsage).desc()
                .yield().sumOf().prop("sumOfPrices").as("total")
                .modelAsAggregate();
        final var expectedEql = select(
                select(TgVehicle.class)
                        .yield().model(countFuelUsage).as("c2")
                        .yield().prop("sumOfPrices").as("c1")
                        .modelAsAggregate()
        )
                .groupBy().prop("c2")
                .orderBy().absOf().prop("c2").desc()
                .yield().sumOf().prop("c1").as("total")
                .modelAsAggregate();

        final var actual = qry(actualEql);
        AggregateOperandMaterialiser.enabled = false;
        final var expected = qry(expectedEql);
        assertAlphaEq(expected, actual);
    }

    @Test
    public void same_subquery_used_in_groupBy_and_yield_via_calculated_property_is_materialised_under_one_column() {
        final var actualEql = select(TgVehicle.class)
                .groupBy().prop("lastFuelUsageQty")
                .yield().prop("lastFuelUsageQty").as("qty")
                .yield().sumOf().prop("sumOfPrices").as("total")
                .modelAsAggregate();
        final var expectedEql = select(
                select(TgVehicle.class)
                        .yield().prop("lastFuelUsageQty").as("c2")
                        .yield().prop("sumOfPrices").as("c1")
                        .modelAsAggregate()
        )
                .groupBy().prop("c2")
                .yield().prop("c2").as("qty")
                .yield().sumOf().prop("c1").as("total")
                .modelAsAggregate();

        final var actual = qry(actualEql);
        AggregateOperandMaterialiser.enabled = false;
        final var expected = qry(expectedEql);
        assertAlphaEq(expected, actual);
    }


    /// A yielded correlated subquery prevents the transformation: it would keep correlating to the original source,
    /// which the outer query would no longer access.
    ///
    @Test
    public void yielded_subquery_alongside_an_aggregation_skips_the_transformation_01() {
        // This subquery can be viewed as a function of the group-by expression `prop("key")`.
        final var countFuelUsage = select(TgFuelUsage.class).where().prop("vehicle.key").eq().extProp(KEY).yield().countAll().modelAsPrimitive();
        final var query = select(TgVehicle.class)
                .groupBy().prop("key")
                .yield().prop("key").as("k")
                .yield().model(countFuelUsage).as("count")
                .yield().sumOf().prop("sumOfPrices").as("total")
                .modelAsAggregate();

        final var actual = qry(query);
        AggregateOperandMaterialiser.enabled = false;
        final var expected = qry(query);
        assertStructEq(expected, actual);
    }

    /// The yielded subquery contains an aggregation that binds to the top-level source, illustrating a complex case of
    /// cross-level aggregate binding.
    /// The transformation is skipped because one of the yielded expressions is a subquery.
    ///
    @Test
    public void yielded_subquery_alongside_an_aggregation_skips_the_transformation_02() {
        final var query = select(TgVehicle.class).as("v") // (1)
                .groupBy().prop("model")
                .yield().prop("model").as("model")
                .yield().sumOf().prop("sumOfPrices").as("total") // Triggers the transformation
                // Count all Fuel Usage records dated after the earliest Fuel Usage within a group.
                .yield().model(select(TgFuelUsage.class) // (2)
                                       .where()
                                       // Although this minOf aggregation is syntactically within (2), it semantically binds to (1).
                                       .prop("date").gt().expr(expr().minOf().model(select(TgFuelUsage.class).where()
                                                                                            .prop("vehicle").eq().prop("v.id")
                                                                                            // This minOf is simple -- binds to the enclosing source.
                                                                                            .yield().minOf().prop("date")
                                                                                            .modelAsPrimitive())
                                                                       .model())
                                       .yield().countAll()
                                       .modelAsPrimitive())
                .as("n")
                .modelAsAggregate();

        final var actual = qry(query);
        AggregateOperandMaterialiser.enabled = false;
        final var expected = qry(query);
        assertStructEq(expected, actual);
    }

    /// The yielded subquery contains an aggregation that binds to the top-level source, illustrating a complex case of
    /// cross-level aggregate binding.
    /// The transformation is not applicable because the implementation does not analyse subqueries, hence cannot see the aggregation.
    ///
    @Test
    public void aggregation_within_a_yielded_subquery_does_not_trigger_the_transformation() {
        final var query = select(TgVehicle.class).as("v") // (1)
                .groupBy().prop("model")
                .yield().prop("model").as("model")
                // Count all Fuel Usage records dated after the earliest Fuel Usage within a group.
                .yield().model(select(TgFuelUsage.class) // (2)
                                       .where()
                                       // Although this minOf aggregation is syntactically within (2), it semantically binds to (1).
                                       .prop("date").gt().expr(expr().minOf().model(select(TgFuelUsage.class).where()
                                                                                            .prop("vehicle").eq().prop("v.id")
                                                                                            // This minOf is simple -- binds to the enclosing source.
                                                                                            .yield().minOf().prop("date")
                                                                                            .modelAsPrimitive())
                                                                       .model())
                                       .yield().countAll()
                                       .modelAsPrimitive())
                .as("n")
                .modelAsAggregate();

        final var actual = qry(query);
        AggregateOperandMaterialiser.enabled = false;
        final var expected = qry(query);
        assertStructEq(expected, actual);
    }

    // The following tests cover an `exists` subquery, which differs from other subqueries in its stage 3 type
    // (`SubQueryForExists3` rather than `SubQuery3`) and is reachable from a yield through `caseWhen`.
    // It must be treated like any other subquery: opaque to analysis, and hence an obstacle to the transformation.

    /// An `exists` subquery in a yield prevents the transformation for the same reason as any other yielded subquery:
    /// it correlates to the original source, which the outer query would no longer access.
    ///
    @Test
    public void exists_subquery_in_a_yield_alongside_an_aggregation_skips_the_transformation() {
        final var fuelUsage = select(TgFuelUsage.class).where().prop("vehicle.key").eq().extProp(KEY).model();
        final var query = select(TgVehicle.class)
                .groupBy().prop(KEY)
                .yield().prop(KEY).as("k")
                .yield().caseWhen().exists(fuelUsage).then().val(1).otherwise().val(0).end().as("hasFuelUsage")
                // Would trigger the transformation, were the `exists` subquery absent.
                .yield().sumOf().prop("sumOfPrices").as("total")
                .modelAsAggregate();

        final var actual = qry(query);
        AggregateOperandMaterialiser.enabled = false;
        final var expected = qry(query);
        assertStructEq(expected, actual);
    }

    /// An aggregation within an `exists` subquery belongs to that subquery's level, not to the level of the outer query's
    /// source, hence it must not make the outer query eligible for the transformation.
    /// Here the only aggregation is the `sum` inside the `exists` subquery, while the outer query has a non-trivial
    /// group-by operand that would be materialised were the transformation to apply.
    ///
    @Test
    public void aggregation_within_an_exists_subquery_does_not_trigger_the_transformation() {
        final var countFuelUsage = select(TgFuelUsage.class).where().prop("vehicle").eq().extProp(ID).yield().sumOf().prop("qty").modelAsPrimitive();
        final var query = select(TgVehicle.class)
                .groupBy().lowerCase().prop("key")
                .yield().lowerCase().prop("key").as("k")
                .yield().caseWhen().exists(countFuelUsage).then().val(1).otherwise().val(0).end().as("flag")
                .modelAsAggregate();

        final var actual = qry(query);
        AggregateOperandMaterialiser.enabled = false;
        final var expected = qry(query);
        assertStructEq(expected, actual);
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Calculated properties

    @Test
    public void aggregation_over_a_calculated_property_whose_expression_does_not_contain_a_subquery_is_transformed() {
        final var actualEql = select(TgVehicle.class).yield().maxOf().prop("constValueProp").as("max").modelAsAggregate();

        final var expectedEql = select(select(TgVehicle.class).yield().expr(expr().val(10).add().val(20).model()).as("c1").modelAsAggregate())
                .yield().maxOf().prop("c1").as("max")
                .modelAsAggregate();

        final var actual = qry(actualEql);
        AggregateOperandMaterialiser.enabled = false;
        final var expected = qry(expectedEql);
        assertAlphaEq(expected, actual);
    }

    @Test
    public void aggregation_over_a_calculated_property_whose_expression_contains_a_subquery_is_transformed() {
        final var actualEql = select(TgVehicle.class).yield().maxOf().prop("lastFuelUsageQty").as("max").modelAsAggregate();

        final var expectedEql = select(select(TgVehicle.class).yield().prop("lastFuelUsageQty").as("c1").modelAsAggregate())
                .yield().maxOf().prop("c1").as("max")
                .modelAsAggregate();

        final var actual = qry(actualEql);
        AggregateOperandMaterialiser.enabled = false;
        final var expected = qry(expectedEql);
        assertAlphaEq(expected, actual);
    }

    @Test
    public void aggregation_over_a_calculated_property_whose_expression_contains_another_calculated_property_containing_a_subquery_is_transformed() {
        final var actualEql = select(TgVehicle.class).yield().maxOf().prop("halfLastFuelUsageQty").as("max").modelAsAggregate();

        final var expectedEql = select(select(TgVehicle.class).yield().prop("halfLastFuelUsageQty").as("c1").modelAsAggregate())
                .yield().maxOf().prop("c1").as("max")
                .modelAsAggregate();

        final var actual = qry(actualEql);
        AggregateOperandMaterialiser.enabled = false;
        final var expected = qry(expectedEql);
        assertAlphaEq(expected, actual);
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // The transformation algorithm is not recursive.
    // The argument of an aggregate function is materialised once, and is not itself transformed.

    @Test
    public void nested_aggregation_with_depth_2_is_transformed() {
        final var actualEql = select(TgVehicle.class)
                .yield().sumOf().expr(expr().maxOf().prop("sumOfPrices").model()).as("total")
                .modelAsAggregate();

        final var expectedEql = select(select(TgVehicle.class)
                                          .yield().expr(expr().maxOf().prop("sumOfPrices").model()).as("c1")
                                          .modelAsAggregate())
                .yield().sumOf().prop("c1").as("total")
                .modelAsAggregate();

        final var actual = qry(actualEql);
        AggregateOperandMaterialiser.enabled = false;
        final var expected = qry(expectedEql);
        assertAlphaEq(expected, actual);
    }

    @Test
    public void nested_aggregation_with_depth_3_is_transformed_only_once() {
        final var actualEql = select(TgVehicle.class)
                .yield().avgOf().expr(expr().sumOf().expr(expr().maxOf().prop("sumOfPrices").model()).model()).as("result")
                .modelAsAggregate();

        final var expectedEql = select(select(TgVehicle.class)
                                          .yield().expr(expr().sumOf().expr(expr().maxOf().prop("sumOfPrices").model()).model()).as("c1")
                                          .modelAsAggregate())
                .yield().avgOf().prop("c1").as("result")
                .modelAsAggregate();

        final var actual = qry(actualEql);
        AggregateOperandMaterialiser.enabled = false;
        final var expected = qry(expectedEql);
        assertAlphaEq(expected, actual);
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

}
