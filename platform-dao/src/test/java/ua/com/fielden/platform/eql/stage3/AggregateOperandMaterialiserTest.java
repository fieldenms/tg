package ua.com.fielden.platform.eql.stage3;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils;
import ua.com.fielden.platform.entity.query.fluent.LikeOptions;
import ua.com.fielden.platform.eql.meta.EqlStage3TestCase;
import ua.com.fielden.platform.eql.meta.EqlTestCase;
import ua.com.fielden.platform.eql.stage3.conditions.ExistencePredicate3;
import ua.com.fielden.platform.eql.stage3.conditions.LikePredicate3;
import ua.com.fielden.platform.eql.stage3.conditions.SetPredicate3;
import ua.com.fielden.platform.eql.stage3.operands.CompoundSingleOperand3;
import ua.com.fielden.platform.eql.stage3.operands.Expression3;
import ua.com.fielden.platform.eql.stage3.operands.OperandsBasedSet3;
import ua.com.fielden.platform.eql.stage3.operands.Value3;
import ua.com.fielden.platform.eql.stage3.operands.functions.*;
import ua.com.fielden.platform.eql.stage3.queries.SubQueryForExists3;
import ua.com.fielden.platform.eql.stage3.sources.JoinLeafNode3;
import ua.com.fielden.platform.eql.stage3.sources.Source3BasedOnQueries;
import ua.com.fielden.platform.eql.stage3.sundries.GroupBy3;
import ua.com.fielden.platform.eql.stage3.sundries.OrderBy3;
import ua.com.fielden.platform.sample.domain.TgFuelUsage;
import ua.com.fielden.platform.sample.domain.TgVehicle;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.query.DbVersion.MSSQL;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;
import static ua.com.fielden.platform.entity.query.fluent.enums.ArithmeticalOperator.*;
import static ua.com.fielden.platform.eql.meta.PropType.*;
import static ua.com.fielden.platform.eql.stage3.sundries.Yield3.yieldWithoutAlias;
import static ua.com.fielden.platform.types.tuples.T2.t2;

/// Structural tests for [AggregateOperandMaterialiser].
///
/// Each test compiles an "actual" query with the transformation enabled and compares it against an "expected"
/// stage-3 AST that is constructed by hand.
/// The AST is built by hand (rather than by compiling an equivalent EQL model with the transformation disabled)
/// because the transformation generates fresh source and SQL identifiers that cannot be reproduced by recompiling a model.
/// Each test also declares a query that reflects the shape of the expected query.
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
        AggregateOperandMaterialiser.resetAliasGenerator();
        setDbVersion(prevDbVersion);
    }

    @Test
    public void multiple_aggregations_over_the_same_operand_share_the_same_column() {
        final var actualEql = select(TgVehicle.class)
                .yield().maxOf().absOf().prop("id").as("maxId")
                .yield().minOf().absOf().prop("id").as("minId")
                .modelAsAggregate();

        // This is the expected query, and its AST must be constructed by hand because of generated IDs.
        final var expectedEql = select(select(TgVehicle.class)
                                          .yield().absOf().prop("id").as("c1")
                                          .modelAsAggregate())
                .yield().maxOf().prop("c1").as("maxId")
                .yield().minOf().prop("c1").as("minId")
                .modelAsAggregate();

        final var srcQrySource = source(TgVehicle.class, 1, 1);
        final var srcQry = srcqry(new JoinLeafNode3(srcQrySource),
                                  yields(mkYield(new AbsOf3(prop(ID, srcQrySource, LONG_PROP_TYPE), LONG_PROP_TYPE), "c1", 4)),
                                  EntityAggregates.class);

        final var topSource = new Source3BasedOnQueries(List.of(srcQry), 2, 5);
        final var prop_c1 = prop("c1", topSource, LONG_PROP_TYPE);
        final var topYield_maxId = mkYield(new MaxOf3(prop_c1, LONG_PROP_TYPE), "maxId", 2);
        final var topYield_minId = mkYield(new MinOf3(prop_c1, LONG_PROP_TYPE), "minId", 3);

        final var expected = qry(new JoinLeafNode3(topSource),
                                 yields(topYield_maxId, topYield_minId));

        AggregateOperandMaterialiser.setAliasGenerator(() -> mkAliasGenerator());
        final var actual = qry(actualEql);
        assertQueryEquals(expected, actual);
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

        // This is the expected query, and its AST must be constructed by hand because of generated IDs.
        final var expectedEql = select(select(TgVehicle.class)
                                          .yield().absOf().prop("id").as("c1")
                                          .yield().prop("key").as("c2")
                                          .modelAsAggregate())
                .groupBy().prop("c2")
                .yield().prop("c2").as("vehKey")
                .yield().avgOf().prop("c1").as("avg")
                .modelAsAggregate();

        final var srcQrySource = source(TgVehicle.class, 1, 1);
        final var srcQry = srcqry(new JoinLeafNode3(srcQrySource),
                                  yields(yieldProp("key", srcQrySource, "c2", STRING_PROP_TYPE, 5),
                                         mkYield(new AbsOf3(prop(ID, srcQrySource, LONG_PROP_TYPE), LONG_PROP_TYPE), "c1", 4)),
                                  EntityAggregates.class);

        final var topSource = new Source3BasedOnQueries(List.of(srcQry), 2, 6);
        final var prop_c1 = prop("c1", topSource, LONG_PROP_TYPE);
        final var prop_c2 = prop("c2", topSource, STRING_PROP_TYPE);
        final var topYield_vehKey = mkYield(prop_c2, "vehKey", 3);
        final var topYield_avg = mkYield(new AverageOf3(prop_c1, false, LONG_PROP_TYPE), "avg", 2);
        final var topGroupBy_c2 = new GroupBy3(prop_c2);

        final var expected = qry(new JoinLeafNode3(topSource),
                                 yields(topYield_vehKey, topYield_avg),
                                 groups(topGroupBy_c2));

        AggregateOperandMaterialiser.setAliasGenerator(() -> mkAliasGenerator());
        final var actual = qry(actualEql);
        assertQueryEquals(expected, actual);
    }

    @Test
    public void where_conditions_are_attached_to_the_source_query_and_not_to_the_outer_query_01() {
        final var actualEql = select(TgVehicle.class).where()
                .prop("purchasePrice").gt().val(100)
                // To trigger transformation.
                .yield().maxOf().absOf().prop("id").as("maxId")
                .modelAsAggregate();

        // This is the expected query, and its AST must be constructed by hand because of generated IDs.
        final var expectedEql = select(select(TgVehicle.class).where()
                                          .prop("purchasePrice").gt().val(100)
                                          .yield().absOf().prop("id").as("c1")
                                          .modelAsAggregate())
                .yield().maxOf().prop("c1").as("maxId")
                .modelAsAggregate();

        final var srcQrySource = source(TgVehicle.class, 1, 1);
        final var srcQry = srcqry(new JoinLeafNode3(srcQrySource),
                                  cond(gt(prop("purchasePrice.amount", srcQrySource, BIGDECIMAL_PROP_TYPE), new Value3(100, INTEGER_PROP_TYPE))),
                                  yields(mkYield(new AbsOf3(prop(ID, srcQrySource, LONG_PROP_TYPE), LONG_PROP_TYPE), "c1", 3)));

        final var topSource = new Source3BasedOnQueries(List.of(srcQry), 2, 4);
        final var prop_c1 = prop("c1", topSource, LONG_PROP_TYPE);
        final var topYield_maxId = mkYield(new MaxOf3(prop_c1, LONG_PROP_TYPE), "maxId", 2);

        final var expected = qry(new JoinLeafNode3(topSource),
                                 yields(topYield_maxId));

        AggregateOperandMaterialiser.setAliasGenerator(() -> mkAliasGenerator());
        final var actual = qry(actualEql);
        assertQueryEquals(expected, actual);
    }

    @Test
    public void where_conditions_are_attached_to_the_source_query_and_not_to_the_outer_query_02() {
        final var actualEql = select(TgVehicle.class).where()
                .condition(EntityQueryUtils.cond().prop("purchasePrice").gt().val(100).model())
                .and().prop("replacedBy").isNotNull()
                .or().exists(select(TgFuelUsage.class).where().prop("vehicle").eq().extProp(ID).model())
                .yield().maxOf().prop("sumOfPrices").as("maxPrice")
                .modelAsAggregate();

        // This is the expected query, and its AST must be constructed by hand because of generated IDs.
        final var expectedEql = select(select(TgVehicle.class).where()
                                          .condition(EntityQueryUtils.cond().prop("purchasePrice").gt().val(100).model())
                                          .and().prop("replacedBy").isNotNull()
                                          .or().exists(select(TgFuelUsage.class).where().prop("vehicle").eq().extProp(ID).model())
                                          .yield().prop("sumOfPrices").as("c1")
                                          .modelAsAggregate())
                .yield().maxOf().prop("c1").as("maxPrice")
                .modelAsAggregate();

        final var vehicleSource = source(TgVehicle.class, 1, 1);
        final var fuelUsageSource = source(TgFuelUsage.class, 2, 2);

        // The `exists` subquery (`fuel usage for this vehicle`) -- it is pushed into the source query verbatim.
        final var existsSubQry = new SubQueryForExists3(
                new QueryComponents3(Optional.of(new JoinLeafNode3(fuelUsageSource)),
                                     cond(eq(entityProp("vehicle", fuelUsageSource, TgVehicle.class), idProp(vehicleSource))),
                                     yields(yieldWithoutAlias(new Value3(null, NULL_TYPE), NULL_TYPE)),
                                     null,
                                     null));

        // `sumOfPrices` is calculated as `1 * price.amount + purchasePrice.amount`, so it is materialised as that expression.
        final var sumOfPrices = new Expression3(new Value3(1, INTEGER_PROP_TYPE),
                                                List.of(new CompoundSingleOperand3(prop("price.amount", vehicleSource, BIGDECIMAL_PROP_TYPE), MULT),
                                                        new CompoundSingleOperand3(prop("purchasePrice.amount", vehicleSource, BIGDECIMAL_PROP_TYPE), ADD)),
                                                BIGDECIMAL_PROP_TYPE);

        // The whole `where` is attached to the source query: `(purchasePrice.amount > 100 AND replacedBy IS NOT NULL) OR EXISTS(...)`.
        final var srcQry = srcqry(new JoinLeafNode3(vehicleSource),
                                  or(and(or(and(cond(gt(prop("purchasePrice.amount", vehicleSource, BIGDECIMAL_PROP_TYPE), new Value3(100, INTEGER_PROP_TYPE))),
                                                isNotNull(entityProp("replacedBy", vehicleSource, TgVehicle.class))))),
                                     and(new ExistencePredicate3(false, existsSubQry))),
                                  yields(mkYield(sumOfPrices, "c1", 5)));

        final var topSource = new Source3BasedOnQueries(List.of(srcQry), 3, 6);
        final var prop_c1 = prop("c1", topSource, BIGDECIMAL_PROP_TYPE);
        final var topYield_maxPrice = mkYield(new MaxOf3(prop_c1, BIGDECIMAL_PROP_TYPE), "maxPrice", 4);

        final var expected = qry(new JoinLeafNode3(topSource),
                                 yields(topYield_maxPrice));

        AggregateOperandMaterialiser.setAliasGenerator(() -> mkAliasGenerator());
        final var actual = qry(actualEql);
        assertQueryEquals(expected, actual);
    }

    @Test
    public void order_by_yield_is_preserved_under_transformation__standalone_order_by() {
        final var order = orderBy().yield("maxId").desc().model();

        final var actualEql = select(TgVehicle.class)
                .groupBy().prop("key")
                .yield().maxOf().absOf().prop("id").as("maxId")
                .modelAsAggregate();

        // This is the expected query, and its AST must be constructed by hand because of generated IDs.
        final var expectedEql = select(select(TgVehicle.class)
                                          .yield().absOf().prop("id").as("c1")
                                          .yield().prop("key").as("c2")
                                          .modelAsAggregate())
                .groupBy().prop("c2")
                .yield().maxOf().prop("c1").as("maxId")
                .modelAsAggregate();

        final var srcQrySource = source(TgVehicle.class, 1, 1);
        final var srcQry = srcqry(new JoinLeafNode3(srcQrySource),
                                  yields(yieldProp("key", srcQrySource, "c2", STRING_PROP_TYPE, 4),
                                         mkYield(new AbsOf3(prop(ID, srcQrySource, LONG_PROP_TYPE), LONG_PROP_TYPE), "c1", 3)),
                                  EntityAggregates.class);

        final var topSource = new Source3BasedOnQueries(List.of(srcQry), 2, 5);
        final var prop_c1 = prop("c1", topSource, LONG_PROP_TYPE);
        final var prop_c2 = prop("c2", topSource, STRING_PROP_TYPE);
        final var topYield_avgPrice = mkYield(new MaxOf3(prop_c1, LONG_PROP_TYPE), "maxId", 2);
        final var topGroupBy_c2 = new GroupBy3(prop_c2);

        final var expected = qry(new JoinLeafNode3(topSource),
                                 yields(topYield_avgPrice),
                                 groups(topGroupBy_c2),
                                 orders(new OrderBy3(topYield_avgPrice, true)));

        AggregateOperandMaterialiser.setAliasGenerator(() -> mkAliasGenerator());
        final var actual = qry(actualEql, order);
        assertQueryEquals(expected, actual);
    }

    @Test
    public void order_by_yield_is_preserved_under_transformation__inline_order_by() {
        final var actualEql = select(TgVehicle.class)
                .groupBy().prop("key")
                .orderBy().yield("maxId").desc()
                .yield().maxOf().absOf().prop("id").as("maxId")
                .modelAsAggregate();

        // This is the expected query, and its AST must be constructed by hand because of generated IDs.
        final var expectedEql = select(select(TgVehicle.class)
                                          .yield().absOf().prop("id").as("c1")
                                          .yield().prop("key").as("c2")
                                          .modelAsAggregate())
                .groupBy().prop("c2")
                .orderBy().yield("maxId").desc()
                .yield().maxOf().prop("c1").as("maxId")
                .modelAsAggregate();

        final var srcQrySource = source(TgVehicle.class, 1, 1);
        final var srcQry = srcqry(new JoinLeafNode3(srcQrySource),
                                  yields(yieldProp("key", srcQrySource, "c2", STRING_PROP_TYPE, 4),
                                         mkYield(new AbsOf3(prop(ID, srcQrySource, LONG_PROP_TYPE), LONG_PROP_TYPE), "c1", 3)),
                                  EntityAggregates.class);

        final var topSource = new Source3BasedOnQueries(List.of(srcQry), 2, 5);
        final var prop_c1 = prop("c1", topSource, LONG_PROP_TYPE);
        final var prop_c2 = prop("c2", topSource, STRING_PROP_TYPE);
        final var topYield_avgPrice = mkYield(new MaxOf3(prop_c1, LONG_PROP_TYPE), "maxId", 2);
        final var topGroupBy_c2 = new GroupBy3(prop_c2);

        final var expected = qry(new JoinLeafNode3(topSource),
                                 yields(topYield_avgPrice),
                                 groups(topGroupBy_c2),
                                 orders(new OrderBy3(topYield_avgPrice, true)));

        AggregateOperandMaterialiser.setAliasGenerator(() -> mkAliasGenerator());
        final var actual = qry(actualEql);
        assertQueryEquals(expected, actual);
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
        assertQueryEquals(expected, actual);
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
        assertQueryEquals(expected, actual);
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

        // This is the expected query, and its AST must be constructed by hand because of generated IDs.
        final var expectedEql = select(select(TgFuelUsage.class)
                                          .yield().beginExpr().prop("qty").mult().val(2).endExpr().as("c1")
                                          .yield().prop("qty").as("c2")
                                          .modelAsAggregate())
                .yield().sumOf().prop("c1").as("doubleQty")
                .yield().sumOf().prop("c2").as("totalQty")
                .modelAsAggregate();

        final var srcQrySource = source(TgFuelUsage.class, 1, 1);
        final var qtyTimes2 = new Expression3(prop("qty", srcQrySource, BIGDECIMAL_PROP_TYPE),
                                              List.of(new CompoundSingleOperand3(new Value3(2, INTEGER_PROP_TYPE), MULT)),
                                              BIGDECIMAL_PROP_TYPE);
        final var srcQry = srcqry(new JoinLeafNode3(srcQrySource),
                                  yields(mkYield(qtyTimes2, "c1", 4),
                                         yieldProp("qty", srcQrySource, "c2", BIGDECIMAL_PROP_TYPE, 5)),
                                  EntityAggregates.class);

        final var topSource = new Source3BasedOnQueries(List.of(srcQry), 2, 6);
        final var prop_c1 = prop("c1", topSource, BIGDECIMAL_PROP_TYPE);
        final var prop_c2 = prop("c2", topSource, BIGDECIMAL_PROP_TYPE);
        final var topYield_doubleQty = mkYield(new SumOf3(prop_c1, false, BIGDECIMAL_PROP_TYPE), "doubleQty", 2);
        final var topYield_totalQty = mkYield(new SumOf3(prop_c2, false, BIGDECIMAL_PROP_TYPE), "totalQty", 3);

        final var expected = qry(new JoinLeafNode3(topSource),
                                 yields(topYield_doubleQty, topYield_totalQty));

        AggregateOperandMaterialiser.setAliasGenerator(() -> mkAliasGenerator());
        final var actual = qry(actualEql);
        assertQueryEquals(expected, actual);
    }

    @Test
    public void query_that_has_aggregation_only_within_a_subquery_is_not_transformed_but_the_subquery_is() {
        final var actualEql = select(TgVehicle.class)
                .yield().model(select(TgFuelUsage.class).where().prop("vehicle").eq().extProp(ID).yield().maxOf().round().prop("qty").to(2).modelAsPrimitive()).as("maxQty")
                .modelAsAggregate();

        // This is the expected query, and its AST must be constructed by hand because of generated source IDs.
        final var expectedEql = select(TgVehicle.class)/*ID=2*/
                .yield().model(select(select(TgFuelUsage.class)/*ID=1*/.where()
                                              .prop("vehicle").eq().extProp(ID)
                                              .yield().round().prop("qty").to(2).as("c1")
                                              .modelAsAggregate())/*ID=3*/
                                       .yield().maxOf().prop("c1")
                                       .modelAsPrimitive())
                    .as("maxQty")
                .modelAsAggregate();

        final var source2 = source(TgVehicle.class, 2, 1);
        final var source1 = source(TgFuelUsage.class, 1, 2);

        final var srcQry = srcqry(sources(source1),
                                  cond(eq(entityProp("vehicle", source1, TgVehicle.class), idProp(source2))),
                                  yields(mkYield(new RoundTo3(prop("qty", source1, BIGDECIMAL_PROP_TYPE), new Value3(2, INTEGER_PROP_TYPE), BIGDECIMAL_PROP_TYPE),
                                                    "c1", 4)));

        final var srcQryAsSource = new Source3BasedOnQueries(List.of(srcQry), 3, 5);
        final var subQry = subqry(sources(srcQryAsSource),
                                  yields(yieldWithoutAlias(new MaxOf3(prop("c1", srcQryAsSource, BIGDECIMAL_PROP_TYPE), BIGDECIMAL_PROP_TYPE), BIGDECIMAL_PROP_TYPE)),
                                  BIGDECIMAL_PROP_TYPE);
        final var expected = qry(sources(source2),
                                 yields(yieldModel(subQry, "maxQty", 6, BIGDECIMAL_PROP_TYPE)));

        AggregateOperandMaterialiser.setAliasGenerator(() -> mkAliasGenerator());
        final var actual = qry(actualEql);
        assertQueryEquals(expected, actual);
    }

    @Test
    public void equal_prop_nodes_under_the_same_parent_node_are_replaced_by_the_same_materialised_prop() {
        final var actualEql = select(TgVehicle.class)
                // To trigger transformation.
                .yield().sumOf().beginExpr().prop("price").mult().val(2).endExpr().as("cost")
                // prop("key") -- 2 equal nodes that should be replaced by the same materialised prop.
                .yield().concatOf().prop("key").orderBy().prop("key").asc().separator().val(" ").as("keys")
                .modelAsAggregate();

        // This is the expected query, and its AST must be constructed by hand because of generated IDs.
        final var expectedEql = select(select(TgVehicle.class)
                                          .yield().beginExpr().prop("price").mult().val(2).endExpr().as("c1")
                                          .yield().prop("key").as("c2")
                                          .modelAsAggregate())
                .yield().sumOf().prop("c1").as("cost")
                .yield().concatOf().prop("c2").orderBy().prop("c2").asc().separator().val(" ").as("keys")
                .modelAsAggregate();

        final var srcQrySource = source(TgVehicle.class, 1, 1);
        final var priceTimes2 = new Expression3(prop("price.amount", srcQrySource, BIGDECIMAL_PROP_TYPE),
                                                List.of(new CompoundSingleOperand3(new Value3(2, INTEGER_PROP_TYPE), MULT)),
                                                BIGDECIMAL_PROP_TYPE);
        final var srcQry = srcqry(new JoinLeafNode3(srcQrySource),
                                  yields(mkYield(priceTimes2, "c1", 4),
                                         yieldProp("key", srcQrySource, "c2", STRING_PROP_TYPE, 5)),
                                  EntityAggregates.class);

        final var topSource = new Source3BasedOnQueries(List.of(srcQry), 2, 6);
        final var prop_c1 = prop("c1", topSource, BIGDECIMAL_PROP_TYPE);
        final var prop_c2 = prop("c2", topSource, STRING_PROP_TYPE);
        final var topYield_cost = mkYield(new SumOf3(prop_c1, false, BIGDECIMAL_PROP_TYPE), "cost", 2);
        final var topYield_keys = mkYield(new ConcatOf3(prop_c2, new Value3(" ", "P_1", STRING_PROP_TYPE), STRING_PROP_TYPE, List.of(new OrderBy3(prop_c2, false))),
                                             "keys", 3);

        final var expected = qry(new JoinLeafNode3(topSource),
                                 yields(topYield_cost, topYield_keys));

        AggregateOperandMaterialiser.setAliasGenerator(() -> mkAliasGenerator());
        final var actual = qry(actualEql);
        assertQueryEquals(expected, actual);
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

        // This is the expected query, and its AST must be constructed by hand because of generated IDs.
        final var expectedEql = select(select(TgVehicle.class)
                                          .yield().absOf().prop("id").as("c1")
                                          .yield().prop("key").as("c2")
                                          .modelAsAggregate())
                .groupBy().prop("c2")
                .yield().maxOf().prop("c1").as("maxId")
                .yield().caseWhen().prop("c2").eq().val("ABC").then().val(1).otherwise().val(0).end().as("flag")
                .modelAsAggregate();

        final var srcQrySource = source(TgVehicle.class, 1, 1);
        final var srcQry = srcqry(new JoinLeafNode3(srcQrySource),
                                  yields(yieldProp("key", srcQrySource, "c2", STRING_PROP_TYPE, 5),
                                         mkYield(new AbsOf3(prop(ID, srcQrySource, LONG_PROP_TYPE), LONG_PROP_TYPE), "c1", 4)),
                                  EntityAggregates.class);

        final var topSource = new Source3BasedOnQueries(List.of(srcQry), 2, 6);
        final var prop_c1 = prop("c1", topSource, LONG_PROP_TYPE);
        final var prop_c2 = prop("c2", topSource, STRING_PROP_TYPE);
        final var topYield_maxId = mkYield(new MaxOf3(prop_c1, LONG_PROP_TYPE), "maxId", 3);
        final var topYield_flag = mkYield(
                new CaseWhen3(List.of(t2(eq(prop_c2, new Value3("ABC", "P_1", STRING_PROP_TYPE)),
                                         new Value3(1, INTEGER_PROP_TYPE))),
                              new Value3(0, INTEGER_PROP_TYPE),
                              null,
                              INTEGER_PROP_TYPE),
                "flag", 2);
        final var topGroupBy_c2 = new GroupBy3(prop_c2);

        final var expected = qry(new JoinLeafNode3(topSource),
                                 yields(topYield_maxId, topYield_flag),
                                 groups(topGroupBy_c2));

        AggregateOperandMaterialiser.setAliasGenerator(() -> mkAliasGenerator());
        final var actual = qry(actualEql);
        assertQueryEquals(expected, actual);
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

        // This is the expected query, and its AST must be constructed by hand because of generated IDs.
        final var expectedEql = select(select(TgVehicle.class)
                                          .yield().absOf().prop("id").as("c1")
                                          .yield().prop("initDate").as("c2")
                                          .modelAsAggregate())
                .groupBy().prop("c2")
                .yield().maxOf().prop("c1").as("maxId")
                .yield().caseWhen().val(40).eq().secondOf().prop("c2").then().val(1).otherwise().val(0).end().as("flag")
                .modelAsAggregate();

        final var srcQrySource = source(TgVehicle.class, 1, 1);
        final var srcQry = srcqry(new JoinLeafNode3(srcQrySource),
                                  yields(yieldProp("initDate", srcQrySource, "c2", DATETIME_PROP_TYPE, 5),
                                         mkYield(new AbsOf3(prop(ID, srcQrySource, LONG_PROP_TYPE), LONG_PROP_TYPE), "c1", 4)),
                                  EntityAggregates.class);

        final var topSource = new Source3BasedOnQueries(List.of(srcQry), 2, 6);
        final var prop_c1 = prop("c1", topSource, LONG_PROP_TYPE);
        final var prop_c2 = prop("c2", topSource, DATETIME_PROP_TYPE);
        final var topYield_maxId = mkYield(new MaxOf3(prop_c1, LONG_PROP_TYPE), "maxId", 3);
        final var topYield_flag = mkYield(
                new CaseWhen3(List.of(t2(eq(new Value3(40, INTEGER_PROP_TYPE), new SecondOf3(prop_c2, INTEGER_PROP_TYPE)),
                                         new Value3(1, INTEGER_PROP_TYPE))),
                              new Value3(0, INTEGER_PROP_TYPE),
                              null,
                              INTEGER_PROP_TYPE),
                "flag", 2);
        final var topGroupBy_c2 = new GroupBy3(prop_c2);

        final var expected = qry(new JoinLeafNode3(topSource),
                                 yields(topYield_maxId, topYield_flag),
                                 groups(topGroupBy_c2));

        AggregateOperandMaterialiser.setAliasGenerator(() -> mkAliasGenerator());
        final var actual = qry(actualEql);
        assertQueryEquals(expected, actual);
    }

    @Test
    public void null_predicate_in_case_when_has_its_properties_transformed() {
        final var actualEql = select(TgVehicle.class)
                .groupBy().prop("initDate")
                // Aggregation to trigger transformation.
                .yield().maxOf().absOf().prop("id").as("maxId")
                .yield().caseWhen().prop("initDate").isNotNull().then().val(1).otherwise().val(0).end().as("flag")
                .modelAsAggregate();

        // This is the expected query, and its AST must be constructed by hand because of generated IDs.
        final var expectedEql = select(select(TgVehicle.class)
                                          .yield().absOf().prop("id").as("c1")
                                          .yield().prop("initDate").as("c2")
                                          .modelAsAggregate())
                .groupBy().prop("c2")
                .yield().maxOf().prop("c1").as("maxId")
                .yield().caseWhen().prop("c2").isNotNull().then().val(1).otherwise().val(0).end().as("flag")
                .modelAsAggregate();

        final var srcQrySource = source(TgVehicle.class, 1, 1);
        final var srcQry = srcqry(new JoinLeafNode3(srcQrySource),
                                  yields(yieldProp("initDate", srcQrySource, "c2", DATETIME_PROP_TYPE, 5),
                                         mkYield(new AbsOf3(prop(ID, srcQrySource, LONG_PROP_TYPE), LONG_PROP_TYPE), "c1", 4)),
                                  EntityAggregates.class);

        final var topSource = new Source3BasedOnQueries(List.of(srcQry), 2, 6);
        final var prop_c1 = prop("c1", topSource, LONG_PROP_TYPE);
        final var prop_c2 = prop("c2", topSource, DATETIME_PROP_TYPE);
        final var topYield_maxId = mkYield(new MaxOf3(prop_c1, LONG_PROP_TYPE), "maxId", 3);
        final var topYield_flag = mkYield(
                new CaseWhen3(List.of(t2(isNotNull(prop_c2),
                                         new Value3(1, INTEGER_PROP_TYPE))),
                              new Value3(0, INTEGER_PROP_TYPE),
                              null,
                              INTEGER_PROP_TYPE),
                "flag", 2);
        final var topGroupBy_c2 = new GroupBy3(prop_c2);

        final var expected = qry(new JoinLeafNode3(topSource),
                                 yields(topYield_maxId, topYield_flag),
                                 groups(topGroupBy_c2));

        AggregateOperandMaterialiser.setAliasGenerator(() -> mkAliasGenerator());
        final var actual = qry(actualEql);
        assertQueryEquals(expected, actual);
    }

    @Test
    public void like_predicate_in_case_when_has_its_properties_transformed() {
        final var actualEql = select(TgVehicle.class)
                .groupBy().prop("key")
                // Aggregation to trigger transformation.
                .yield().maxOf().absOf().prop("id").as("maxId")
                .yield().caseWhen().prop("key").like().val("ABC%").then().val(1).otherwise().val(0).end().as("flag")
                .modelAsAggregate();

        // This is the expected query, and its AST must be constructed by hand because of generated IDs.
        final var expectedEql = select(select(TgVehicle.class)
                                          .yield().absOf().prop("id").as("c1")
                                          .yield().prop("key").as("c2")
                                          .modelAsAggregate())
                .groupBy().prop("c2")
                .yield().maxOf().prop("c1").as("maxId")
                .yield().caseWhen().prop("c2").like().val("ABC%").then().val(1).otherwise().val(0).end().as("flag")
                .modelAsAggregate();

        final var srcQrySource = source(TgVehicle.class, 1, 1);
        final var srcQry = srcqry(new JoinLeafNode3(srcQrySource),
                                  yields(yieldProp("key", srcQrySource, "c2", STRING_PROP_TYPE, 5),
                                         mkYield(new AbsOf3(prop(ID, srcQrySource, LONG_PROP_TYPE), LONG_PROP_TYPE), "c1", 4)),
                                  EntityAggregates.class);

        final var topSource = new Source3BasedOnQueries(List.of(srcQry), 2, 6);
        final var prop_c1 = prop("c1", topSource, LONG_PROP_TYPE);
        final var prop_c2 = prop("c2", topSource, STRING_PROP_TYPE);
        final var topYield_maxId = mkYield(new MaxOf3(prop_c1, LONG_PROP_TYPE), "maxId", 3);
        final var topYield_flag = mkYield(
                new CaseWhen3(List.of(t2(new LikePredicate3(prop_c2, new Value3("ABC%", "P_1", STRING_PROP_TYPE), LikeOptions.DEFAULT_OPTIONS),
                                         new Value3(1, INTEGER_PROP_TYPE))),
                              new Value3(0, INTEGER_PROP_TYPE),
                              null,
                              INTEGER_PROP_TYPE),
                "flag", 2);
        final var topGroupBy_c2 = new GroupBy3(prop_c2);

        final var expected = qry(new JoinLeafNode3(topSource),
                                 yields(topYield_maxId, topYield_flag),
                                 groups(topGroupBy_c2));

        AggregateOperandMaterialiser.setAliasGenerator(() -> mkAliasGenerator());
        final var actual = qry(actualEql);
        assertQueryEquals(expected, actual);
    }

    @Test
    public void set_predicate_in_case_when_has_its_properties_transformed() {
        final var actualEql = select(TgVehicle.class)
                .groupBy().prop("key")
                // Aggregation to trigger transformation.
                .yield().maxOf().absOf().prop("id").as("maxId")
                .yield().caseWhen().prop("key").in().values("ABC", "DEF").then().val(1).otherwise().val(0).end().as("flag")
                .modelAsAggregate();

        // This is the expected query, and its AST must be constructed by hand because of generated IDs.
        final var expectedEql = select(select(TgVehicle.class)
                                          .yield().absOf().prop("id").as("c1")
                                          .yield().prop("key").as("c2")
                                          .modelAsAggregate())
                .groupBy().prop("c2")
                .yield().maxOf().prop("c1").as("maxId")
                .yield().caseWhen().prop("c2").in().values("ABC", "DEF").then().val(1).otherwise().val(0).end().as("flag")
                .modelAsAggregate();

        final var srcQrySource = source(TgVehicle.class, 1, 1);
        final var srcQry = srcqry(new JoinLeafNode3(srcQrySource),
                                  yields(yieldProp("key", srcQrySource, "c2", STRING_PROP_TYPE, 5),
                                         mkYield(new AbsOf3(prop(ID, srcQrySource, LONG_PROP_TYPE), LONG_PROP_TYPE), "c1", 4)),
                                  EntityAggregates.class);

        final var topSource = new Source3BasedOnQueries(List.of(srcQry), 2, 6);
        final var prop_c1 = prop("c1", topSource, LONG_PROP_TYPE);
        final var prop_c2 = prop("c2", topSource, STRING_PROP_TYPE);
        final var topYield_maxId = mkYield(new MaxOf3(prop_c1, LONG_PROP_TYPE), "maxId", 3);
        final var topYield_flag = mkYield(
                new CaseWhen3(List.of(t2(new SetPredicate3(prop_c2, false, new OperandsBasedSet3(List.of(new Value3("ABC", "P_1", STRING_PROP_TYPE), new Value3("DEF", "P_2", STRING_PROP_TYPE)))),
                                         new Value3(1, INTEGER_PROP_TYPE))),
                              new Value3(0, INTEGER_PROP_TYPE),
                              null,
                              INTEGER_PROP_TYPE),
                "flag", 2);
        final var topGroupBy_c2 = new GroupBy3(prop_c2);

        final var expected = qry(new JoinLeafNode3(topSource),
                                 yields(topYield_maxId, topYield_flag),
                                 groups(topGroupBy_c2));

        AggregateOperandMaterialiser.setAliasGenerator(() -> mkAliasGenerator());
        final var actual = qry(actualEql);
        assertQueryEquals(expected, actual);
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

        // This is the expected query, and its AST must be constructed by hand because of generated IDs.
        final var expectedEql = select(select(TgVehicle.class)
                                          .yield().absOf().prop("id").as("c1")
                                          .yield().prop("initDate").as("c2")
                                          .modelAsAggregate())
                .yield().concatOf().prop("c1").orderBy().prop("c2").asc().separator().val(", ").as("ids")
                .modelAsAggregate();

        final var srcQrySource = source(TgVehicle.class, 1, 1);
        final var srcQry = srcqry(new JoinLeafNode3(srcQrySource),
                                  yields(yieldProp("initDate", srcQrySource, "c2", DATETIME_PROP_TYPE, 4),
                                         mkYield(new AbsOf3(prop(ID, srcQrySource, LONG_PROP_TYPE), LONG_PROP_TYPE), "c1", 3)),
                                  EntityAggregates.class);

        final var topSource = new Source3BasedOnQueries(List.of(srcQry), 2, 5);
        final var prop_c1 = prop("c1", topSource, LONG_PROP_TYPE);
        final var prop_c2 = prop("c2", topSource, DATETIME_PROP_TYPE);
        final var topYield_prices = mkYield(new ConcatOf3(prop_c1, new Value3(", ", "P_1", STRING_PROP_TYPE), STRING_PROP_TYPE, List.of(new OrderBy3(prop_c2, false))),
                                               "ids", 2);

        final var expected = qry(new JoinLeafNode3(topSource),
                                 yields(topYield_prices));

        AggregateOperandMaterialiser.setAliasGenerator(() -> mkAliasGenerator());
        final var actual = qry(actualEql);
        assertQueryEquals(expected, actual);
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

        // This is the expected query, and its AST must be constructed by hand because of generated IDs.
        final var expectedEql = select(select(TgFuelUsage.class)
                                          .yield().beginExpr().prop("qty").mult().val(2).endExpr().as("c1")
                                          .yield().prop("qty").as("c2")
                                          .yield().prop("date").as("c3")
                                          .modelAsAggregate())
                .yield().concatOf().prop("c2").orderBy().prop("c3").asc().separator().val(", ").as("qtys")
                .yield().sumOf().prop("c1").as("doubleSum")
                .modelAsAggregate();

        final var srcQrySource = source(TgFuelUsage.class, 1, 1);
        final var qtyTimes2 = new Expression3(prop("qty", srcQrySource, BIGDECIMAL_PROP_TYPE),
                                              List.of(new CompoundSingleOperand3(new Value3(2, INTEGER_PROP_TYPE), MULT)),
                                              BIGDECIMAL_PROP_TYPE);
        final var srcQry = srcqry(new JoinLeafNode3(srcQrySource),
                                  yields(yieldProp("date", srcQrySource, "c3", DATETIME_PROP_TYPE, 6),
                                         mkYield(qtyTimes2, "c1", 4),
                                         yieldProp("qty", srcQrySource, "c2", BIGDECIMAL_PROP_TYPE, 5)),
                                  EntityAggregates.class);

        final var topSource = new Source3BasedOnQueries(List.of(srcQry), 2, 7);
        final var prop_c1 = prop("c1", topSource, BIGDECIMAL_PROP_TYPE);
        final var prop_c2 = prop("c2", topSource, BIGDECIMAL_PROP_TYPE);
        final var prop_c3 = prop("c3", topSource, DATETIME_PROP_TYPE);
        final var topYield_qtys = mkYield(new ConcatOf3(prop_c2, new Value3(", ", "P_1", STRING_PROP_TYPE), STRING_PROP_TYPE, List.of(new OrderBy3(prop_c3, false))),
                                             "qtys", 3);
        final var topYield_doubleSum = mkYield(new SumOf3(prop_c1, false, BIGDECIMAL_PROP_TYPE), "doubleSum", 2);

        final var expected = qry(new JoinLeafNode3(topSource),
                                 yields(topYield_qtys, topYield_doubleSum));

        AggregateOperandMaterialiser.setAliasGenerator(() -> mkAliasGenerator());
        final var actual = qry(actualEql);
        assertQueryEquals(expected, actual);
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

        // This is the expected query, and its AST must be constructed by hand because of generated IDs.
        final var expectedEql = select(select(TgVehicle.class)
                                          .yield().beginExpr().val(1).mult().prop("price.amount").add().prop("purchasePrice.amount").endExpr().as("c1")
                                          .yield().model(countFuelUsage).as("c2")
                                          .modelAsAggregate())
                .groupBy().prop("c2")
                .yield().sumOf().prop("c1").as("total")
                .modelAsAggregate();

        final var vehicleSource = source(TgVehicle.class, 1, 1);
        final var fuelUsageSource = source(TgFuelUsage.class, 2, 3);

        // `sumOfPrices` is calculated as `1 * price.amount + purchasePrice.amount`.
        final var sumOfPrices = new Expression3(new Value3(1, INTEGER_PROP_TYPE),
                                                List.of(new CompoundSingleOperand3(prop("price.amount", vehicleSource, BIGDECIMAL_PROP_TYPE), MULT),
                                                        new CompoundSingleOperand3(prop("purchasePrice.amount", vehicleSource, BIGDECIMAL_PROP_TYPE), ADD)),
                                                BIGDECIMAL_PROP_TYPE);

        // The group-by sub-query: the number of this vehicle's fuel usages.
        final var countFuelUsageSubQry = subqry(new JoinLeafNode3(fuelUsageSource),
                                                cond(eq(entityProp("vehicle", fuelUsageSource, TgVehicle.class), idProp(vehicleSource))),
                                                yields(yieldWithoutAlias(CountAll3.INSTANCE, INTEGER_PROP_TYPE)),
                                                INTEGER_PROP_TYPE);

        final var srcQry = srcqry(new JoinLeafNode3(vehicleSource),
                                  yields(mkYield(sumOfPrices, "c1", 5),
                                         mkYield(countFuelUsageSubQry, "c2", 6)),
                                  EntityAggregates.class);

        final var topSource = new Source3BasedOnQueries(List.of(srcQry), 3, 7);
        final var prop_c1 = prop("c1", topSource, BIGDECIMAL_PROP_TYPE);
        final var prop_c2 = prop("c2", topSource, INTEGER_PROP_TYPE);
        final var topYield_total = mkYield(new SumOf3(prop_c1, false, BIGDECIMAL_PROP_TYPE), "total", 2);
        final var topGroupBy_c2 = new GroupBy3(prop_c2);

        final var expected = qry(new JoinLeafNode3(topSource),
                                 yields(topYield_total),
                                 groups(topGroupBy_c2));

        AggregateOperandMaterialiser.setAliasGenerator(() -> mkAliasGenerator());
        final var actual = qry(actualEql);
        assertQueryEquals(expected, actual);
    }

    @Test
    public void groupBy_a_calculated_property_that_contains_a_subquery_and_is_not_yielded_materialises_it_as_a_column() {
        final var actualEql = select(TgVehicle.class)
                .groupBy().prop("lastFuelUsageQty")
                .yield().sumOf().prop("sumOfPrices").as("total")
                .modelAsAggregate();

        // This is the expected query, and its AST must be constructed by hand because of generated IDs.
        final var expectedEql = select(select(TgVehicle.class)
                                          .yield().beginExpr().val(1).mult().prop("price.amount").add().prop("purchasePrice.amount").endExpr().as("c1")
                                          .yield().prop("lastFuelUsageQty").as("c2")
                                          .modelAsAggregate())
                .groupBy().prop("c2")
                .yield().sumOf().prop("c1").as("total")
                .modelAsAggregate();

        final var vehicleSource = source(TgVehicle.class, 1, 1);
        final var fuelUsageSource = source(TgFuelUsage.class, 2, 3);
        final var laterFuelUsageSource = source(TgFuelUsage.class, 3, 4);

        // `sumOfPrices` is calculated as `1 * price.amount + purchasePrice.amount`.
        final var sumOfPrices = new Expression3(new Value3(1, INTEGER_PROP_TYPE),
                                                List.of(new CompoundSingleOperand3(prop("price.amount", vehicleSource, BIGDECIMAL_PROP_TYPE), MULT),
                                                        new CompoundSingleOperand3(prop("purchasePrice.amount", vehicleSource, BIGDECIMAL_PROP_TYPE), ADD)),
                                                BIGDECIMAL_PROP_TYPE);

        // `lastFuelUsageQty` is the `qty` of this vehicle's most recent fuel usage, expressed as a correlated sub-query.
        final var noLaterFuelUsage = new SubQueryForExists3(
                new QueryComponents3(Optional.of(new JoinLeafNode3(laterFuelUsageSource)),
                                     or(and(eq(entityProp("vehicle", laterFuelUsageSource, TgVehicle.class), entityProp("vehicle", fuelUsageSource, TgVehicle.class)),
                                            gt(prop("date", laterFuelUsageSource, DATETIME_PROP_TYPE), prop("date", fuelUsageSource, DATETIME_PROP_TYPE)))),
                                     yields(yieldWithoutAlias(new Value3(null, NULL_TYPE), NULL_TYPE)),
                                     null,
                                     null));
        final var lastFuelUsageQty = subqry(
                new JoinLeafNode3(fuelUsageSource),
                or(and(eq(entityProp("vehicle", fuelUsageSource, TgVehicle.class), idProp(vehicleSource)),
                       new ExistencePredicate3(true, noLaterFuelUsage))),
                yields(yieldWithoutAlias(prop("qty", fuelUsageSource, BIGDECIMAL_PROP_TYPE), BIGDECIMAL_PROP_TYPE)),
                BIGDECIMAL_PROP_TYPE);

        final var srcQry = srcqry(new JoinLeafNode3(vehicleSource),
                                  yields(mkYield(sumOfPrices, "c1", 7),
                                         mkYield(lastFuelUsageQty, "c2", 8)),
                                  EntityAggregates.class);

        final var topSource = new Source3BasedOnQueries(List.of(srcQry), 4, 9);
        final var prop_c1 = prop("c1", topSource, BIGDECIMAL_PROP_TYPE);
        final var prop_c2 = prop("c2", topSource, BIGDECIMAL_PROP_TYPE);
        final var topYield_total = mkYield(new SumOf3(prop_c1, false, BIGDECIMAL_PROP_TYPE), "total", 2);
        final var topGroupBy_c2 = new GroupBy3(prop_c2);

        final var expected = qry(new JoinLeafNode3(topSource),
                                 yields(topYield_total),
                                 groups(topGroupBy_c2));

        AggregateOperandMaterialiser.setAliasGenerator(() -> mkAliasGenerator());
        final var actual = qry(actualEql);
        assertQueryEquals(expected, actual);
    }

    // The following two tests cover a known limitation: a subquery that is both grouped by and yielded cannot be
    // transformed into a valid query.
    // The group-by subquery is materialised as a column, but the yielded subquery is a distinct AST node -- it is not
    // equal to the group-by one (subqueries are compared by their unique generated IDs), so it is left referencing the
    // original source, which the outer query no longer accesses.
    // These tests pin the resulting (invalid) AST.

    @Test
    public void groupBy_a_subquery_that_is_also_yielded_leaves_the_yielded_subquery_referencing_the_original_source() {
        final var countFuelUsage = select(TgFuelUsage.class).where().prop("vehicle").eq().extProp(ID).yield().countAll().modelAsPrimitive();
        final var actualEql = select(TgVehicle.class)
                .groupBy().model(countFuelUsage)
                .yield().model(countFuelUsage).as("count")
                .yield().sumOf().prop("sumOfPrices").as("total")
                .modelAsAggregate();

        // This is the expected query, and its AST must be constructed by hand because of generated IDs.
        final var vehicleSource = source(TgVehicle.class, 2, 1);
        final var fuelUsageSourceForGroupBy = source(TgFuelUsage.class, 3, 6);
        final var fuelUsageSourceForYield = source(TgFuelUsage.class, 1, 2);

        // `sumOfPrices` is calculated as `1 * price.amount + purchasePrice.amount`.
        final var sumOfPrices = new Expression3(new Value3(1, INTEGER_PROP_TYPE),
                                                List.of(new CompoundSingleOperand3(prop("price.amount", vehicleSource, BIGDECIMAL_PROP_TYPE), MULT),
                                                        new CompoundSingleOperand3(prop("purchasePrice.amount", vehicleSource, BIGDECIMAL_PROP_TYPE), ADD)),
                                                BIGDECIMAL_PROP_TYPE);

        // The group-by sub-query is materialised as `c2`, correctly correlating to the vehicle source within the source query.
        final var countFuelUsageForGroupBy = subqry(new JoinLeafNode3(fuelUsageSourceForGroupBy),
                                                    cond(eq(entityProp("vehicle", fuelUsageSourceForGroupBy, TgVehicle.class), idProp(vehicleSource))),
                                                    yields(yieldWithoutAlias(CountAll3.INSTANCE, INTEGER_PROP_TYPE)),
                                                    INTEGER_PROP_TYPE);

        // The yielded sub-query is a distinct node, left intact, still correlating to the vehicle source -- now out of scope in the outer query.
        final var countFuelUsageForYield = subqry(new JoinLeafNode3(fuelUsageSourceForYield),
                                                  cond(eq(entityProp("vehicle", fuelUsageSourceForYield, TgVehicle.class), idProp(vehicleSource))),
                                                  yields(yieldWithoutAlias(CountAll3.INSTANCE, INTEGER_PROP_TYPE)),
                                                  INTEGER_PROP_TYPE);

        final var srcQry = srcqry(new JoinLeafNode3(vehicleSource),
                                  yields(mkYield(sumOfPrices, "c1", 8),
                                         mkYield(countFuelUsageForGroupBy, "c2", 9)),
                                  EntityAggregates.class);

        final var topSource = new Source3BasedOnQueries(List.of(srcQry), 4, 10);
        final var prop_c1 = prop("c1", topSource, BIGDECIMAL_PROP_TYPE);
        final var prop_c2 = prop("c2", topSource, INTEGER_PROP_TYPE);
        final var topYield_count = mkYield(countFuelUsageForYield, "count", 4);
        final var topYield_total = mkYield(new SumOf3(prop_c1, false, BIGDECIMAL_PROP_TYPE), "total", 5);
        final var topGroupBy_c2 = new GroupBy3(prop_c2);

        final var expected = qry(new JoinLeafNode3(topSource),
                                 yields(topYield_count, topYield_total),
                                 groups(topGroupBy_c2));

        AggregateOperandMaterialiser.setAliasGenerator(() -> mkAliasGenerator());
        final var actual = qry(actualEql);
        assertQueryEquals(expected, actual);
    }

    @Test
    public void groupBy_a_calculated_property_containing_a_subquery_that_is_also_yielded_leaves_the_yielded_subquery_referencing_the_original_source() {
        final var actualEql = select(TgVehicle.class)
                .groupBy().prop("lastFuelUsageQty")
                .yield().prop("lastFuelUsageQty").as("qty")
                .yield().sumOf().prop("sumOfPrices").as("total")
                .modelAsAggregate();

        // This is the expected query, and its AST must be constructed by hand because of generated IDs.
        final var vehicleSource = source(TgVehicle.class, 1, 1);
        // Group-by instance of `lastFuelUsageQty` (materialised as `c2`).
        final var fuelUsageSourceForGroupBy = source(TgFuelUsage.class, 2, 8);
        final var laterFuelUsageSourceForGroupBy = source(TgFuelUsage.class, 3, 9);
        // Yielded instance of `lastFuelUsageQty` (left intact).
        final var fuelUsageSourceForYield = source(TgFuelUsage.class, 2, 2);
        final var laterFuelUsageSourceForYield = source(TgFuelUsage.class, 3, 3);

        // `sumOfPrices` is calculated as `1 * price.amount + purchasePrice.amount`.
        final var sumOfPrices = new Expression3(new Value3(1, INTEGER_PROP_TYPE),
                                                List.of(new CompoundSingleOperand3(prop("price.amount", vehicleSource, BIGDECIMAL_PROP_TYPE), MULT),
                                                        new CompoundSingleOperand3(prop("purchasePrice.amount", vehicleSource, BIGDECIMAL_PROP_TYPE), ADD)),
                                                BIGDECIMAL_PROP_TYPE);

        // The group-by instance of `lastFuelUsageQty`: materialised as `c2`, correlating to the vehicle source within the source query.
        final var noLaterFuelUsageForGroupBy = new SubQueryForExists3(
                new QueryComponents3(Optional.of(new JoinLeafNode3(laterFuelUsageSourceForGroupBy)),
                                     or(and(eq(entityProp("vehicle", laterFuelUsageSourceForGroupBy, TgVehicle.class), entityProp("vehicle", fuelUsageSourceForGroupBy, TgVehicle.class)),
                                            gt(prop("date", laterFuelUsageSourceForGroupBy, DATETIME_PROP_TYPE), prop("date", fuelUsageSourceForGroupBy, DATETIME_PROP_TYPE)))),
                                     yields(yieldWithoutAlias(new Value3(null, NULL_TYPE), NULL_TYPE)),
                                     null,
                                     null));
        final var lastFuelUsageQtyForGroupBy = subqry(
                new JoinLeafNode3(fuelUsageSourceForGroupBy),
                or(and(eq(entityProp("vehicle", fuelUsageSourceForGroupBy, TgVehicle.class), idProp(vehicleSource)),
                       new ExistencePredicate3(true, noLaterFuelUsageForGroupBy))),
                yields(yieldWithoutAlias(prop("qty", fuelUsageSourceForGroupBy, BIGDECIMAL_PROP_TYPE), BIGDECIMAL_PROP_TYPE)),
                BIGDECIMAL_PROP_TYPE);

        // The yielded instance of `lastFuelUsageQty`: a distinct sub-query left correlating to the vehicle source -- now out of scope in the outer query.
        final var noLaterFuelUsageForYield = new SubQueryForExists3(
                new QueryComponents3(Optional.of(new JoinLeafNode3(laterFuelUsageSourceForYield)),
                                     or(and(eq(entityProp("vehicle", laterFuelUsageSourceForYield, TgVehicle.class), entityProp("vehicle", fuelUsageSourceForYield, TgVehicle.class)),
                                            gt(prop("date", laterFuelUsageSourceForYield, DATETIME_PROP_TYPE), prop("date", fuelUsageSourceForYield, DATETIME_PROP_TYPE)))),
                                     yields(yieldWithoutAlias(new Value3(null, NULL_TYPE), NULL_TYPE)),
                                     null,
                                     null));
        final var lastFuelUsageQtyForYield = subqry(
                new JoinLeafNode3(fuelUsageSourceForYield),
                or(and(eq(entityProp("vehicle", fuelUsageSourceForYield, TgVehicle.class), idProp(vehicleSource)),
                       new ExistencePredicate3(true, noLaterFuelUsageForYield))),
                yields(yieldWithoutAlias(prop("qty", fuelUsageSourceForYield, BIGDECIMAL_PROP_TYPE), BIGDECIMAL_PROP_TYPE)),
                BIGDECIMAL_PROP_TYPE);

        final var srcQry = srcqry(new JoinLeafNode3(vehicleSource),
                                  yields(mkYield(sumOfPrices, "c1", 12),
                                         mkYield(lastFuelUsageQtyForGroupBy, "c2", 13)),
                                  EntityAggregates.class);

        final var topSource = new Source3BasedOnQueries(List.of(srcQry), 4, 14);
        final var prop_c1 = prop("c1", topSource, BIGDECIMAL_PROP_TYPE);
        final var prop_c2 = prop("c2", topSource, BIGDECIMAL_PROP_TYPE);
        final var topYield_qty = mkYield(lastFuelUsageQtyForYield, "qty", 6);
        final var topYield_total = mkYield(new SumOf3(prop_c1, false, BIGDECIMAL_PROP_TYPE), "total", 7);
        final var topGroupBy_c2 = new GroupBy3(prop_c2);

        final var expected = qry(new JoinLeafNode3(topSource),
                                 yields(topYield_qty, topYield_total),
                                 groups(topGroupBy_c2));

        AggregateOperandMaterialiser.setAliasGenerator(() -> mkAliasGenerator());
        final var actual = qry(actualEql);
        assertQueryEquals(expected, actual);
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Calculated properties

    @Test
    public void aggregation_over_a_calculated_property_whose_expression_does_not_contain_a_subquery_is_transformed() {
        final var actualEql = select(TgVehicle.class).yield().maxOf().prop("constValueProp").as("max").modelAsAggregate();

        // This is the expected query, and its AST must be constructed by hand because of generated IDs.
        final var expectedEql = select(select(TgVehicle.class).yield().expr(expr().val(10).add().val(20).model()).as("c1").modelAsAggregate())
                .yield().maxOf().prop("c1").as("max")
                .modelAsAggregate();

        // `constValueProp` is materialised as its expression.
        final var srcQrySource = source(TgVehicle.class, 1, 1);
        final var constValue = new Expression3(new Value3(10, INTEGER_PROP_TYPE),
                                               List.of(new CompoundSingleOperand3(new Value3(20, INTEGER_PROP_TYPE), ADD)),
                                               INTEGER_PROP_TYPE);
        final var srcQry = srcqry(new JoinLeafNode3(srcQrySource),
                                  yields(mkYield(constValue, "c1", 3)),
                                  EntityAggregates.class);

        final var topSource = new Source3BasedOnQueries(List.of(srcQry), 2, 4);
        final var prop_c1 = prop("c1", topSource, INTEGER_PROP_TYPE);
        final var topYield_max = mkYield(new MaxOf3(prop_c1, INTEGER_PROP_TYPE), "max", 2);

        final var expected = qry(new JoinLeafNode3(topSource),
                                 yields(topYield_max));

        AggregateOperandMaterialiser.setAliasGenerator(() -> mkAliasGenerator());
        final var actual = qry(actualEql);
        assertQueryEquals(expected, actual);
    }

    @Test
    public void aggregation_over_a_calculated_property_whose_expression_contains_a_subquery_is_transformed() {
        final var actualEql = select(TgVehicle.class).yield().maxOf().prop("lastFuelUsageQty").as("max").modelAsAggregate();

        // This is the expected query, and its AST must be constructed by hand because of generated IDs.
        final var expectedEql = select(select(TgVehicle.class).yield().prop("lastFuelUsageQty").as("c1").modelAsAggregate())
                .yield().maxOf().prop("c1").as("max")
                .modelAsAggregate();

        // `lastFuelUsageQty` is calculated as the `qty` of this vehicle's most recent fuel usage, expressed as a
        // correlated sub-query, so it is materialised as that sub-query.
        final var vehicleSource = source(TgVehicle.class, 1, 1);
        final var fuelUsageSource = source(TgFuelUsage.class, 2, 2);
        final var laterFuelUsageSource = source(TgFuelUsage.class, 3, 3);

        // `not exists` (a later fuel usage for the same vehicle), used to single out the most recent one.
        final var noLaterFuelUsage = new SubQueryForExists3(
                new QueryComponents3(Optional.of(new JoinLeafNode3(laterFuelUsageSource)),
                                     or(and(eq(entityProp("vehicle", laterFuelUsageSource, TgVehicle.class), entityProp("vehicle", fuelUsageSource, TgVehicle.class)),
                                            gt(prop("date", laterFuelUsageSource, DATETIME_PROP_TYPE), prop("date", fuelUsageSource, DATETIME_PROP_TYPE)))),
                                     yields(yieldWithoutAlias(new Value3(null, NULL_TYPE), NULL_TYPE)),
                                     null,
                                     null));

        // The whole sub-query: the `qty` of this vehicle's fuel usage that has no later fuel usage.
        final var lastFuelUsageQty = subqry(
                new JoinLeafNode3(fuelUsageSource),
                or(and(eq(entityProp("vehicle", fuelUsageSource, TgVehicle.class), idProp(vehicleSource)),
                       new ExistencePredicate3(true, noLaterFuelUsage))),
                yields(yieldWithoutAlias(prop("qty", fuelUsageSource, BIGDECIMAL_PROP_TYPE), BIGDECIMAL_PROP_TYPE)),
                BIGDECIMAL_PROP_TYPE);

        final var srcQry = srcqry(new JoinLeafNode3(vehicleSource),
                                  yields(mkYield(lastFuelUsageQty, "c1", 7)),
                                  EntityAggregates.class);

        final var topSource = new Source3BasedOnQueries(List.of(srcQry), 4, 8);
        final var prop_c1 = prop("c1", topSource, BIGDECIMAL_PROP_TYPE);
        final var topYield_max = mkYield(new MaxOf3(prop_c1, BIGDECIMAL_PROP_TYPE), "max", 6);

        final var expected = qry(new JoinLeafNode3(topSource),
                                 yields(topYield_max));

        AggregateOperandMaterialiser.setAliasGenerator(() -> mkAliasGenerator());
        final var actual = qry(actualEql);
        assertQueryEquals(expected, actual);
    }

    @Test
    public void aggregation_over_a_calculated_property_whose_expression_contains_another_calculated_property_containing_a_subquery_is_transformed() {
        final var actualEql = select(TgVehicle.class).yield().maxOf().prop("halfLastFuelUsageQty").as("max").modelAsAggregate();

        // This is the expected query, and its AST must be constructed by hand because of generated IDs.
        final var expectedEql = select(select(TgVehicle.class).yield().prop("halfLastFuelUsageQty").as("c1").modelAsAggregate())
                .yield().maxOf().prop("c1").as("max")
                .modelAsAggregate();

        // `halfLastFuelUsageQty` is `lastFuelUsageQty / 2`, where `lastFuelUsageQty` is the `qty` of this vehicle's most
        // recent fuel usage, expressed as a correlated sub-query.
        // The whole expression is materialised, with the nested calculated property inlined as that sub-query.
        final var vehicleSource = source(TgVehicle.class, 1, 1);
        final var fuelUsageSource = source(TgFuelUsage.class, 2, 2);
        final var laterFuelUsageSource = source(TgFuelUsage.class, 3, 3);

        // `not exists` (a later fuel usage for the same vehicle), used to single out the most recent one.
        final var noLaterFuelUsage = new SubQueryForExists3(
                new QueryComponents3(Optional.of(new JoinLeafNode3(laterFuelUsageSource)),
                                     or(and(eq(entityProp("vehicle", laterFuelUsageSource, TgVehicle.class), entityProp("vehicle", fuelUsageSource, TgVehicle.class)),
                                            gt(prop("date", laterFuelUsageSource, DATETIME_PROP_TYPE), prop("date", fuelUsageSource, DATETIME_PROP_TYPE)))),
                                     yields(yieldWithoutAlias(new Value3(null, NULL_TYPE), NULL_TYPE)),
                                     null,
                                     null));

        // `lastFuelUsageQty`: the `qty` of this vehicle's fuel usage that has no later fuel usage.
        final var lastFuelUsageQty = subqry(
                new JoinLeafNode3(fuelUsageSource),
                or(and(eq(entityProp("vehicle", fuelUsageSource, TgVehicle.class), idProp(vehicleSource)),
                       new ExistencePredicate3(true, noLaterFuelUsage))),
                yields(yieldWithoutAlias(prop("qty", fuelUsageSource, BIGDECIMAL_PROP_TYPE), BIGDECIMAL_PROP_TYPE)),
                BIGDECIMAL_PROP_TYPE);

        // The materialised `c1`: the sub-query divided by 2.
        final var halfLastFuelUsageQty = new Expression3(lastFuelUsageQty,
                                                         List.of(new CompoundSingleOperand3(new Value3(2, INTEGER_PROP_TYPE), DIV)),
                                                         BIGDECIMAL_PROP_TYPE);

        final var srcQry = srcqry(new JoinLeafNode3(vehicleSource),
                                  yields(mkYield(halfLastFuelUsageQty, "c1", 7)),
                                  EntityAggregates.class);

        final var topSource = new Source3BasedOnQueries(List.of(srcQry), 4, 8);
        final var prop_c1 = prop("c1", topSource, BIGDECIMAL_PROP_TYPE);
        final var topYield_max = mkYield(new MaxOf3(prop_c1, BIGDECIMAL_PROP_TYPE), "max", 6);

        final var expected = qry(new JoinLeafNode3(topSource),
                                 yields(topYield_max));

        AggregateOperandMaterialiser.setAliasGenerator(() -> mkAliasGenerator());
        final var actual = qry(actualEql);
        assertQueryEquals(expected, actual);
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

        // This is the expected query, and its AST must be constructed by hand because of generated IDs.
        final var expectedEql = select(select(TgVehicle.class)
                                          .yield().expr(expr().maxOf().prop("sumOfPrices").model()).as("c1")
                                          .modelAsAggregate())
                .yield().sumOf().prop("c1").as("total")
                .modelAsAggregate();

        // The aggregated argument of the outer `sum` is materialised once and is not transformed further:
        // `c1` is `max(sumOfPrices)`, with the inner `max` left intact.
        // `sumOfPrices` is calculated as `1 * price.amount + purchasePrice.amount`.
        final var vehicleSource = source(TgVehicle.class, 1, 1);
        final var sumOfPrices = new Expression3(new Value3(1, INTEGER_PROP_TYPE),
                                                List.of(new CompoundSingleOperand3(prop("price.amount", vehicleSource, BIGDECIMAL_PROP_TYPE), MULT),
                                                        new CompoundSingleOperand3(prop("purchasePrice.amount", vehicleSource, BIGDECIMAL_PROP_TYPE), ADD)),
                                                BIGDECIMAL_PROP_TYPE);
        final var maxOfSumOfPrices = new Expression3(new MaxOf3(sumOfPrices, BIGDECIMAL_PROP_TYPE), List.of(), BIGDECIMAL_PROP_TYPE);

        final var srcQry = srcqry(new JoinLeafNode3(vehicleSource),
                                  yields(mkYield(maxOfSumOfPrices, "c1", 3)),
                                  EntityAggregates.class);

        final var topSource = new Source3BasedOnQueries(List.of(srcQry), 2, 4);
        final var prop_c1 = prop("c1", topSource, BIGDECIMAL_PROP_TYPE);
        final var topYield_total = mkYield(new SumOf3(prop_c1, false, BIGDECIMAL_PROP_TYPE), "total", 2);

        final var expected = qry(new JoinLeafNode3(topSource),
                                 yields(topYield_total));

        AggregateOperandMaterialiser.setAliasGenerator(() -> mkAliasGenerator());
        final var actual = qry(actualEql);
        assertQueryEquals(expected, actual);
    }

    @Test
    public void nested_aggregation_with_depth_3_is_transformed_only_once() {
        final var actualEql = select(TgVehicle.class)
                .yield().avgOf().expr(expr().sumOf().expr(expr().maxOf().prop("sumOfPrices").model()).model()).as("result")
                .modelAsAggregate();

        // This is the expected query, and its AST must be constructed by hand because of generated IDs.
        final var expectedEql = select(select(TgVehicle.class)
                                          .yield().expr(expr().sumOf().expr(expr().maxOf().prop("sumOfPrices").model()).model()).as("c1")
                                          .modelAsAggregate())
                .yield().avgOf().prop("c1").as("result")
                .modelAsAggregate();

        // The aggregated argument of the outer `avg` is materialised once and is not transformed further:
        // `c1` is `sum(max(sumOfPrices))`, with the nested `sum` and `max` left intact.
        // `sumOfPrices` is calculated as `1 * price.amount + purchasePrice.amount`.
        final var vehicleSource = source(TgVehicle.class, 1, 1);
        final var sumOfPrices = new Expression3(new Value3(1, INTEGER_PROP_TYPE),
                                                List.of(new CompoundSingleOperand3(prop("price.amount", vehicleSource, BIGDECIMAL_PROP_TYPE), MULT),
                                                        new CompoundSingleOperand3(prop("purchasePrice.amount", vehicleSource, BIGDECIMAL_PROP_TYPE), ADD)),
                                                BIGDECIMAL_PROP_TYPE);
        final var maxOfSumOfPrices = new Expression3(new MaxOf3(sumOfPrices, BIGDECIMAL_PROP_TYPE), List.of(), BIGDECIMAL_PROP_TYPE);
        final var sumOfMax = new Expression3(new SumOf3(maxOfSumOfPrices, false, BIGDECIMAL_PROP_TYPE), List.of(), BIGDECIMAL_PROP_TYPE);

        final var srcQry = srcqry(new JoinLeafNode3(vehicleSource),
                                  yields(mkYield(sumOfMax, "c1", 3)),
                                  EntityAggregates.class);

        final var topSource = new Source3BasedOnQueries(List.of(srcQry), 2, 4);
        final var prop_c1 = prop("c1", topSource, BIGDECIMAL_PROP_TYPE);
        final var topYield_result = mkYield(new AverageOf3(prop_c1, false, BIGDECIMAL_PROP_TYPE), "result", 2);

        final var expected = qry(new JoinLeafNode3(topSource),
                                 yields(topYield_result));

        AggregateOperandMaterialiser.setAliasGenerator(() -> mkAliasGenerator());
        final var actual = qry(actualEql);
        assertQueryEquals(expected, actual);
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Utilities
    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    private Stream<String> mkAliasGenerator() {
        return IntStream.iterate(1, i -> i + 1).mapToObj(i -> "c" + i);
    }

}
