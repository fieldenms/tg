package ua.com.fielden.platform.entity.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.junit.Ignore;
import org.junit.Test;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.ArithmeticalOperator;
import ua.com.fielden.platform.entity.query.fluent.ComparisonOperator;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere0;
import ua.com.fielden.platform.entity.query.fluent.JoinType;
import ua.com.fielden.platform.entity.query.generation.BaseEntQueryCompositionTCase;
import ua.com.fielden.platform.entity.query.generation.elements.ComparisonTest;
import ua.com.fielden.platform.entity.query.generation.elements.CompoundCondition;
import ua.com.fielden.platform.entity.query.generation.elements.CompoundSingleOperand;
import ua.com.fielden.platform.entity.query.generation.elements.CompoundSource;
import ua.com.fielden.platform.entity.query.generation.elements.Conditions;
import ua.com.fielden.platform.entity.query.generation.elements.DayOf;
import ua.com.fielden.platform.entity.query.generation.elements.EntProp;
import ua.com.fielden.platform.entity.query.generation.elements.EntQuery;
import ua.com.fielden.platform.entity.query.generation.elements.EntValue;
import ua.com.fielden.platform.entity.query.generation.elements.ExistenceTest;
import ua.com.fielden.platform.entity.query.generation.elements.Expression;
import ua.com.fielden.platform.entity.query.generation.elements.GroupBy;
import ua.com.fielden.platform.entity.query.generation.elements.GroupBys;
import ua.com.fielden.platform.entity.query.generation.elements.LikeTest;
import ua.com.fielden.platform.entity.query.generation.elements.MonthOf;
import ua.com.fielden.platform.entity.query.generation.elements.Now;
import ua.com.fielden.platform.entity.query.generation.elements.NullTest;
import ua.com.fielden.platform.entity.query.generation.elements.OrderBy;
import ua.com.fielden.platform.entity.query.generation.elements.OrderBys;
import ua.com.fielden.platform.entity.query.generation.elements.QuantifiedTest;
import ua.com.fielden.platform.entity.query.generation.elements.Quantifier;
import ua.com.fielden.platform.entity.query.generation.elements.QueryBasedSet;
import ua.com.fielden.platform.entity.query.generation.elements.QueryBasedSource;
import ua.com.fielden.platform.entity.query.generation.elements.SetTest;
import ua.com.fielden.platform.entity.query.generation.elements.Sources;
import ua.com.fielden.platform.entity.query.generation.elements.TypeBasedSource;
import ua.com.fielden.platform.entity.query.generation.elements.YearOf;
import ua.com.fielden.platform.entity.query.generation.elements.Yield;
import ua.com.fielden.platform.entity.query.generation.elements.Yields;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel;
import ua.com.fielden.platform.sample.domain.TgFuelUsage;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.sample.domain.TgWorkOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;


public class QueryModelCompositionTest extends BaseEntQueryCompositionTCase {
    protected final IWhere0<TgVehicle> where_veh = select(VEHICLE).where();
    protected final IWhere0<TgWorkOrder> where_wo = select(WORK_ORDER).as("wo").where();


    /////////////////////////////////////////// Conditions ////////////////////////////////////////////////////
    @Test
    public void test_like() {
	assertModelsEquals( //
		conditions(new LikeTest(prop("model.desc"), val(mercLike), false, false)), //
		conditions(where_veh.prop("model.desc").like().val(mercLike)));
    }

    @Test
    public void test_notLike() {
	assertModelsEquals( //
		conditions(new LikeTest(prop("model.desc"), val(mercLike), true, false)), //
		conditions(where_veh.prop("model.desc").notLike().val(mercLike)));
    }

    @Test
    public void test_set_test_with_values() {
	assertModelsEquals(//
		conditions(new SetTest(prop("model"), false, set(val(merc), val(audi)))), //
		conditions(where_veh.prop("model").in().values(merc, audi)));
    }

    @Test
    public void test_set_test_with_query() {
	assertModelsEquals( //
		conditions(new SetTest(prop("model"), false, new QueryBasedSet(entSubQry(select(MODEL).model())))), //
		conditions(where_veh.prop("model").in().model(select(MODEL).model())));
    }

    @Test
    public void test_plain_quantified_test() {
	final EntityResultQueryModel<TgVehicleModel> vehModels = select(MODEL).model();
	assertModelsEquals( //
		conditions(new QuantifiedTest(prop("model"), _eq, Quantifier.ANY, entSubQry(vehModels))), //
		conditions(where_veh.prop("model").eq().any(vehModels)));
    }

    @Test
    public void test_simple_query_model_01() {
	assertModelsEquals( //
		conditions(new NullTest(prop("model"), true), //
			compound(_and, new NullTest(prop("station"), false))), //
		conditions(where_veh.prop("model").isNotNull().and().prop("station").isNull()));
    }

    @Test
    public void test_simple_query_model_02() {
	assertModelsEquals(//
		conditions(new ComparisonTest(prop("price"), _gt, val(100)), //
			compound(_and, new ComparisonTest(prop("purchasePrice"), _lt, prop("price")))), //
		conditions(where_veh.prop("price").gt().val(100).and().prop("purchasePrice").lt().prop("price")));
    }

    @Test
    public void test_simple_query_model_03() {
	final Map<String, Object> paramValues = new HashMap<String, Object>();
	paramValues.put("eqclass", "CL1");

	assertModelsEquals(//
		conditions(new NullTest(prop("model"), true), //
			compound(_and, new NullTest(val("CL1"), false))), //
		conditions(where_veh.prop("model").isNotNull().and().param("eqclass").isNull(), paramValues));
    }

    @Test
    public void test_simple_query_model_04() {
	assertModelsEquals(//
		conditions(group(false, //
			new NullTest(prop("model"), true), //
			compound(_and, new NullTest(prop("station"), false)))), //
		conditions(where_veh.begin().prop("model").isNotNull().and().prop("station").isNull().end()));
    }

    @Test
    public void test_simple_query_model_05() {
	assertModelsEquals( //
	conditions(group(false, //
		new NullTest(prop("model"), true), //
		compound(_and, new NullTest(prop("station"), false))), //
		compound(_and, new NullTest(prop("price"), true))), //
		conditions(where_veh.begin().prop("model").isNotNull().and().prop("station").isNull().end().and().prop("price").isNotNull()));
    }

    @Test
    public void test_simple_query_model_06() {
	assertModelsEquals(//
	conditions(new NullTest(prop("model"), true), //
		compound(_and, new ExistenceTest(false, entSubQry(select(VEHICLE).model())))), //
		conditions(where_veh.prop("model").isNotNull().and().exists(select(VEHICLE).model())));
    }

    @Test
    public void test_simple_query_model_07() {
	assertModelsEquals(//
	conditions(group(false, new NullTest(prop("model"), true), //
		compound(_or, new NullTest(prop("station"), true)))), //
		conditions(where_veh.anyOfProps("model", "station").isNotNull()));
    }

    @Test
    public void test_simple_query_model_08() {
	assertModelsEquals(//
	conditions(new NullTest(expression(prop("price"), compound(_add, prop("purchasePrice"))), false)), //
		conditions(where_veh.beginExpr().prop("price").add().prop("purchasePrice").endExpr().isNull()));
    }

    @Test
    public void test_simple_query_model_09() {
	assertModelsEquals(//
	conditions(new NullTest(expression(prop("price.amount"), compound(_add, prop("purchasePrice.amount"))), false)), //
		conditions(where_veh.expr(expr().prop("price.amount").add().prop("purchasePrice.amount").model()).isNull()));
    }

    @Test
    public void test_simple_query_model_10() {
	assertModelsEquals(//
	conditions(new NullTest(expression(prop("model"), compound(_add, expression(prop("model"), compound(_mult, prop("station"))))), false)), //
		conditions(where_veh.expr(expr().prop("model").add().expr(expr().prop("model").mult().prop("station").model()).model()).isNull()));
    }

    @Test
    public void test_simple_query_model_11() {
	assertModelsEquals(//
		conditions(new ComparisonTest(new DayOf(prop("initDate")), _gt, val(15)), compound(_and, new ComparisonTest(prop("price"), _lt, prop("purchasePrice")))), //
		conditions(where_veh.dayOf().prop("initDate").gt().val(15).and().prop("price").lt().prop("purchasePrice")));
    }

    @Test
    public void test_simple_query_model_12() {
	assertModelsEquals(//
		conditions(new ComparisonTest(new MonthOf(prop("initDate")), _gt, val(3)), compound(_and, new ComparisonTest(prop("price"), _lt, prop("purchasePrice")))), //
		conditions(where_veh.monthOf().prop("initDate").gt().val(3).and().prop("price").lt().prop("purchasePrice")));
    }

    @Test
    public void test_simple_query_model_13() {
	assertModelsEquals(//
		conditions(group(false, //
			new ExistenceTest(false, entSubQry(select(VEHICLE).model())), //
			compound(_or, //
				new ExistenceTest(false, entSubQry(select(WORK_ORDER).model()))))), //
		conditions(where_veh.existsAnyOf(select(VEHICLE).model(), select(WORK_ORDER).model())));
    }

    @Test
    public void test_ignore_of_null_value_in_condition1() {
	assertModelsEquals(//
		conditions(new LikeTest(prop("model.desc"), val(mercLike), false, false)), //
		conditions(where_veh.prop("model.desc").like().iVal(mercLike)));
    }

    @Test
    public void test_ignore_of_null_value_in_condition2() {
	assertModelsEquals(//
		conditions(alwaysTrueCondition), //
		conditions(where_veh.prop("model.desc").like().iVal(null)));
    }

    @Test
    public void test_ignore_of_null_value_in_condition3() {
	final Map<String, Object> paramValues = new HashMap<String, Object>();
	paramValues.put("param", "MERC%");
	assertModelsEquals(//
		conditions(new LikeTest(prop("model.desc"), val("MERC%"), false, false)), //
		conditions(where_veh.prop("model.desc").like().param("param"), paramValues));
    }

    @Test
    public void test_ignore_of_null_value_in_condition4() {
	final Map<String, Object> paramValues = new HashMap<String, Object>();
	paramValues.put("param", null);
	assertModelsEquals(//
		conditions(alwaysTrueCondition), //
		conditions(where_veh.prop("model.desc").like().iParam("param"), paramValues));
    }

    @Test
    public void test_ignore_of_null_value_in_condition5() {
	assertModelsEquals(//
		conditions(group(false, alwaysTrueCondition)), //
		conditions(where_veh.prop("model.desc").like().anyOfValues()));
    }

    @Test
    public void test_expressions1() {
	final Map<String, Object> paramValues = new HashMap<String, Object>();
	paramValues.put("costMultiplier", 1);
	paramValues.put("costDivider", 2);

	assertModelsEquals(//
		conditions(new ComparisonTest(
			expression(
				expression(
					expression(
						expression(prop("wo.actCost.amount"), compound( _mult, val(1))), //
						compound(_add, prop("wo.estCost.amount")), compound(_div, val(2))), //
					compound(_add, prop("wo.yearlyCost.amount"))), //
				compound(_div, val(12))),
		_gt, val(1000))), //

		conditions(where_wo.
			beginExpr().beginExpr(). //
			beginExpr().beginExpr().prop("wo.actCost.amount").mult().param("costMultiplier").endExpr().add().prop("wo.estCost.amount").div().param("costDivider").endExpr(). //
			add().prop("wo.yearlyCost.amount").endExpr(). //
			div().val(12).endExpr(). //
			gt().val(1000), paramValues));
    }

    @Test
    public void test_query_sources() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).as("v").join(TgWorkOrder.class).as("wo").on().prop("v").eq().prop("wo.vehicle").leftJoin(TgWorkOrder.class).as("wo2").on().prop("v").eq().prop("wo2.vehicle"). //
	where().dayOf().prop("initDate").gt().val(15).and().prop("price").lt().prop("purchasePrice").model();

	final Conditions condition1 = new Conditions(new ComparisonTest(prop("v"), _eq, prop("wo.vehicle")), new ArrayList<CompoundCondition>());
	final Conditions condition2 = new Conditions(new ComparisonTest(prop("v"), _eq, prop("wo2.vehicle")), new ArrayList<CompoundCondition>());

	final List<CompoundSource> others = new ArrayList<CompoundSource>();
	others.add(new CompoundSource(new TypeBasedSource(DOMAIN_METADATA_ANALYSER.getEntityMetadata(WORK_ORDER), "wo", DOMAIN_METADATA_ANALYSER), JoinType.IJ, condition1));
	others.add(new CompoundSource(new TypeBasedSource(DOMAIN_METADATA_ANALYSER.getEntityMetadata(WORK_ORDER), "wo2", DOMAIN_METADATA_ANALYSER), JoinType.LJ, condition2));

	final EntQuery act = entResultQry(qry);
	final Sources exp = new Sources(new TypeBasedSource(DOMAIN_METADATA_ANALYSER.getEntityMetadata(VEHICLE), "v", DOMAIN_METADATA_ANALYSER), others);
	assertEquals("models are different", exp, act.getSources());

	final List<CompoundCondition> others2 = new ArrayList<CompoundCondition>();
	others2.add(new CompoundCondition(_and, new ComparisonTest(prop("price"), _lt, prop("purchasePrice"))));
	final Conditions exp2 = new Conditions(new ComparisonTest(new DayOf(prop("initDate")), _gt, val(15)), others2);
	assertEquals(exp2, act.getConditions());
    }

    //////////////////////////////////////////////////// Grouping & Ordering //////////////////////////////////////////////////////////////
    @Test
    public void test_query_with_one_group() {
	final EntityResultQueryModel<TgVehicleModel> qry = select(VEHICLE).groupBy().prop("model").yield().prop("model").modelAsEntity(MODEL);
	final EntQuery act = entResultQry(qry);

	final SortedMap<String, Yield> yields = new TreeMap<String, Yield>();
	yields.put("id", new Yield(new EntProp("model"), "id"));
	final Yields exp = new Yields(yields);

	assertEquals("models are different", exp, act.getYields());

	final List<GroupBy> groups = new ArrayList<GroupBy>();
	groups.add(new GroupBy(new EntProp("model")));
	final GroupBys exp2 = new GroupBys(groups);
	assertEquals("models are different", exp2, act.getGroups());
    }

    @Test
    public void test_query_with_several_groups() {
	final AggregatedResultQueryModel qry = select(VEHICLE).groupBy().prop("model").groupBy().yearOf().prop("initDate").yield().prop("model").as("model").yield().yearOf().prop("initDate").as("initYear").modelAsAggregate();
	final EntQuery act = entResultQry(qry);
	final YearOf yearOfModel = new YearOf(new EntProp("initDate"));
	final EntProp eqClassProp = new EntProp("model");

	final SortedMap<String, Yield> yields = new TreeMap<String, Yield>();
	yields.put("model", new Yield(eqClassProp, "model"));
	yields.put("initYear", new Yield(yearOfModel, "initYear"));
	final Yields exp = new Yields(yields);
	assertEquals("models are different", exp, act.getYields());

	final List<GroupBy> groups = new ArrayList<GroupBy>();
	groups.add(new GroupBy(eqClassProp));
	groups.add(new GroupBy(yearOfModel));
	final GroupBys exp2 = new GroupBys(groups);
	assertEquals("models are different", exp2, act.getGroups());
    }

    @Test
    public void test_query_ordering() {
	final EntityResultQueryModel<TgVehicleModel> qry = select(VEHICLE).groupBy().prop("model").yield().prop("model").modelAsEntity(MODEL);
	final OrderingModel orderModel = orderBy().beginExpr().prop("model").add().now().endExpr().asc().prop("key").desc().model();
	final EntQuery act = entResultQry(qry, orderModel);
	System.out.println(act.sql());

	final List<OrderBy> orderings = new ArrayList<OrderBy>();
	orderings.add(new OrderBy(expression(prop("model"), compound(_add, new Now())), false));
	orderings.add(new OrderBy(prop("key"), true));
	final OrderBys exp2 = new OrderBys(orderings);
	assertEquals("models are different", exp2, act.getOrderings());
    }


    //////////////////////////////////////////////////////// Yielding ///////////////////////////////////////////////////////////////////
    @Test
    public void test1() {
	final Map<String, Object> paramValues = new HashMap<String, Object>();
	paramValues.put("param", 20);
	final AggregatedResultQueryModel qry = select(VEHICLE).yield().prop("station").as("st").yield().beginExpr().prop("model").add().param("param").endExpr().as("m").modelAsAggregate();
	final SortedMap<String, Yield> yields = new TreeMap<String, Yield>();
	yields.put("st", new Yield(new EntProp("station"), "st"));
	final List<CompoundSingleOperand> compSingleOperands = new ArrayList<CompoundSingleOperand>();
	compSingleOperands.add(new CompoundSingleOperand(new EntValue(20), ArithmeticalOperator.ADD));
	final Expression expression = new Expression(new EntProp("model"), compSingleOperands);
	yields.put("m", new Yield(expression, "m"));
	final Yields exp = new Yields(yields);
	assertEquals("models are different", exp, entResultQry(qry, paramValues).getYields());
    }

    @Test
    public void test2() {
	final EntityResultQueryModel<TgVehicleModel> qry = select(VEHICLE).yield().prop("model").modelAsEntity(MODEL);
	final SortedMap<String, Yield> yields = new TreeMap<String, Yield>();
	yields.put("id", new Yield(new EntProp("model"), "id"));
	final Yields exp = new Yields(yields);
	assertEquals("models are different", exp, entResultQry(qry).getYields());
    }

    @Test
    public void test3() {
	final AggregatedResultQueryModel qry = select(VEHICLE).yield().prop("station").as("st").yield().prop("model").as("m").modelAsAggregate();
	final SortedMap<String, Yield> yields = new TreeMap<String, Yield>();
	yields.put("st", new Yield(new EntProp("station"), "st"));
	yields.put("m", new Yield(new EntProp("model"), "m"));
	final Yields exp = new Yields(yields);
	assertEquals("models are different", exp, entResultQry(qry).getYields());
    }

    @Test
    public void test4() {
	final EntityResultQueryModel<TgVehicleModel> qry = select(VEHICLE).as("v").yield().prop("v.model").modelAsEntity(MODEL);
	final SortedMap<String, Yield> yields = new TreeMap<String, Yield>();
	yields.put("id", new Yield(new EntProp("v.model"), "id"));
	final Yields exp = new Yields(yields);
	assertEquals("models are different", exp, entResultQry(qry).getYields());
    }

    @Test
    public void test5() {
	final PrimitiveResultQueryModel qry = select(VEHICLE).as("v").yield().prop("v.model").modelAsPrimitive();
	final SortedMap<String, Yield> yields = new TreeMap<String, Yield>();
	yields.put("", new Yield(new EntProp("v.model"), ""));
	final Yields exp = new Yields(yields);
	assertEquals("models are different", exp, entResultQry(qry).getYields());
    }

    @Test
    @Ignore
    public void test_validation_of_yielded_tree_with_broken_hierarchy() {
	// TODO YieldsModel should be validate to prevent misuse of dot.notated convention - broken hierarchy in yields
	final AggregatedResultQueryModel sourceQry = select(TgFuelUsage.class). //
	groupBy().yearOf().prop("readingDate"). //
	groupBy().prop("vehicle"). //
	yield().prop("vehicle").as("vehicle"). //
	yield().yearOf().prop("readingDate").as("readingYear"). //
	yield().prop("vehicle.model.make").as("vehicle.model.make"). //
	modelAsAggregate();
	try {
	    entResultQry(sourceQry);
	    fail("Should have failed!");
	} catch (final Exception e) {
	}
    }

    @Test
    @Ignore
    public void test_validation_of_yielded_tree_with_not_existing_prop_in_hierarchy() {
	// TODO YieldsModel should be validate to prevent misuse of dot.notated convention - not-existing/invalid property in hierarchy
	final AggregatedResultQueryModel sourceQry = select(TgFuelUsage.class). //
	groupBy().yearOf().prop("readingDate"). //
	groupBy().prop("vehicle"). //
	yield().prop("vehicle").as("vehicle"). //
	yield().yearOf().prop("readingDate").as("readingYear"). //
	yield().prop("vehicle.model.make").as("vehicle.mordor"). //
	modelAsAggregate();
	try {
	    entResultQry(sourceQry);
	    fail("Should have failed!");
	} catch (final Exception e) {
	}
    }

    /////////////////////////////////////////////////////////////  Sources //////////////////////////////////////////////////////////////////////
    @Test
    public void test_query_sources1() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).as("v").join(WORK_ORDER).as("wo").on().prop("v").eq().prop("vehicle").model();

	final Conditions condition = new Conditions(new ComparisonTest(new EntProp("v"), ComparisonOperator.EQ, new EntProp("vehicle")), new ArrayList<CompoundCondition>());

	final List<CompoundSource> others = new ArrayList<CompoundSource>();
	others.add(new CompoundSource(new TypeBasedSource(DOMAIN_METADATA_ANALYSER.getEntityMetadata(WORK_ORDER), "wo", DOMAIN_METADATA_ANALYSER), JoinType.IJ, condition));

	final Sources exp = new Sources(new TypeBasedSource(DOMAIN_METADATA_ANALYSER.getEntityMetadata(VEHICLE), "v", DOMAIN_METADATA_ANALYSER), others);
	assertEquals("models are different", exp, entResultQry(qry).getSources());
    }

    @Test
    public void test_query_sources1a() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).as("v").join(WORK_ORDER).as("wo").on().prop("v").eq().prop("wo.vehicle").where().val(1).isNotNull().model();

	final Conditions condition = new Conditions(new ComparisonTest(new EntProp("v"), ComparisonOperator.EQ, new EntProp("wo.vehicle")), new ArrayList<CompoundCondition>());

	final List<CompoundSource> others = new ArrayList<CompoundSource>();
	others.add(new CompoundSource(new TypeBasedSource(DOMAIN_METADATA_ANALYSER.getEntityMetadata(WORK_ORDER), "wo", DOMAIN_METADATA_ANALYSER), JoinType.IJ, condition));

	final Sources exp = new Sources(new TypeBasedSource(DOMAIN_METADATA_ANALYSER.getEntityMetadata(VEHICLE), "v", DOMAIN_METADATA_ANALYSER), others);
	assertEquals("models are different", exp, entResultQry(qry).getSources());
    }

    @Test
    public void test_query_sources_with_explicit_join_and_without_aliases() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).as("v").join(WORK_ORDER).on().prop("v").eq().prop("vehicle").model();

	final Conditions condition = new Conditions(new ComparisonTest(new EntProp("v"), ComparisonOperator.EQ, new EntProp("vehicle")), new ArrayList<CompoundCondition>());

	final List<CompoundSource> others = new ArrayList<CompoundSource>();
	others.add(new CompoundSource(new TypeBasedSource(DOMAIN_METADATA_ANALYSER.getEntityMetadata(WORK_ORDER), null, DOMAIN_METADATA_ANALYSER), JoinType.IJ, condition));

	final Sources exp = new Sources(new TypeBasedSource(DOMAIN_METADATA_ANALYSER.getEntityMetadata(VEHICLE), "v", DOMAIN_METADATA_ANALYSER), others);
	assertEquals("models are different", exp, entResultQry(qry).getSources());
    }

    @Test
    public void test_query_sources2() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).as("v").join(WORK_ORDER).as("wo").on().prop("v").eq().prop("wo.vehicle").leftJoin(WORK_ORDER).as("wo2").on().prop("v").eq().prop("wo2.vehicle").model();

	final Conditions condition1 = new Conditions(new ComparisonTest(new EntProp("v"), ComparisonOperator.EQ, new EntProp("wo.vehicle")), new ArrayList<CompoundCondition>());
	final Conditions condition2 = new Conditions(new ComparisonTest(new EntProp("v"), ComparisonOperator.EQ, new EntProp("wo2.vehicle")), new ArrayList<CompoundCondition>());

	final List<CompoundSource> others = new ArrayList<CompoundSource>();
	others.add(new CompoundSource(new TypeBasedSource(DOMAIN_METADATA_ANALYSER.getEntityMetadata(WORK_ORDER), "wo", DOMAIN_METADATA_ANALYSER), JoinType.IJ, condition1));
	others.add(new CompoundSource(new TypeBasedSource(DOMAIN_METADATA_ANALYSER.getEntityMetadata(WORK_ORDER), "wo2", DOMAIN_METADATA_ANALYSER), JoinType.LJ, condition2));

	final Sources exp = new Sources(new TypeBasedSource(DOMAIN_METADATA_ANALYSER.getEntityMetadata(VEHICLE), "v", DOMAIN_METADATA_ANALYSER), others);
	assertEquals("models are different", exp, entResultQry(qry).getSources());
    }

    @Test
    public void test_query_with_derived_sources() {
	final EntityResultQueryModel<TgVehicle> sourceQry = select(VEHICLE).as("v").where().prop("v.model").isNotNull().model();
	final EntityResultQueryModel<TgVehicle> qry = select(sourceQry).as("v").where().prop("v.model").isNotNull().model();

	final Sources exp = new Sources(new QueryBasedSource("v", DOMAIN_METADATA_ANALYSER, entSourceQry(sourceQry)), new ArrayList<CompoundSource>());
	assertEquals("models are different", exp, entResultQry(qry).getSources());
    }

//    @Test
//    public void test_query_with_derived_sources2() {
//	final AggregatedResultQueryModel sourceQry = select(VEHICLE).as("v").where().prop("v.model").isNotNull().
//		groupBy().prop("model").
//		groupBy().yearOf().prop("initDate").
//		yield().prop("model").as("vehModel").
//		yield().yearOf().prop("initDate").as("modelYear").modelAsAggregate();
//	final EntityResultQueryModel<TgVehicle> qry = select(sourceQry).as("v").where().prop("vehModel").isNotNull().and().prop("modelYear").ge().val(2000).model();
//	final EntQuery entQry = entResultQry(qry);
//	//final EntQuerySourcesModel exp = new EntQuerySourcesModel(new EntQuerySourceAsModel("v", entQuery1(sourceQry)), new ArrayList<EntQueryCompoundSourceModel>());
//	//assertEquals("models are different", exp, entQuery1(qry).getSources());
//    }
//
//    @Test
//    public void test_query_with_derived_sources3() {
//	final AggregatedResultQueryModel sourceQry = select(VEHICLE).as("v").where().prop("v.model").isNotNull().
//		groupBy().prop("model").
//		yield().prop("model").as("vehModel").
//		yield().minOf().yearOf().prop("initDate").as("modelYear").modelAsAggregate();
//	final EntityResultQueryModel<TgVehicle> qry = select(sourceQry).as("v").where().prop("vehModel").isNotNull().and().prop("modelYear").ge().val(2000).model();
//	final EntQuery entQry = entResultQry(qry);
//	//final EntQuerySourcesModel exp = new EntQuerySourcesModel(new EntQuerySourceAsModel("v", entQuery1(sourceQry)), new ArrayList<EntQueryCompoundSourceModel>());
//	//assertEquals("models are different", exp, entQuery1(qry).getSources());
//    }

    @Test
    public void test_simple_query_model_13_() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).as("v").model();
	final List<CompoundSource> others = new ArrayList<CompoundSource>();
	final Sources exp = new Sources(new TypeBasedSource(DOMAIN_METADATA_ANALYSER.getEntityMetadata(VEHICLE), "v", DOMAIN_METADATA_ANALYSER), others);
	assertEquals("models are different", exp, entResultQry(qry).getSources());
    }

    @Test
    public void test_simple_query_model_14() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).model();
	final List<CompoundSource> others = new ArrayList<CompoundSource>();
	final Sources exp = new Sources(new TypeBasedSource(DOMAIN_METADATA_ANALYSER.getEntityMetadata(VEHICLE), null, DOMAIN_METADATA_ANALYSER), others);
	assertEquals("models are different", exp, entResultQry(qry).getSources());
    }


    ///////////////////////////////////////////////////////////////////////// ignored /////////////////////////////////////////////////////////////////


    @Test
    @Ignore
    public void test_query_model1() {
	final AggregatedResultQueryModel a =
		select(WORK_ORDER).as("wo")
		.where().beginExpr().beginExpr().beginExpr().beginExpr().prop("wo.actCost.amount").mult().param("costMultiplier").endExpr().add().prop("wo.estCost.amount").div().param("costDivider").endExpr().add().prop("wo.yearlyCost.amount").endExpr().div().val(12).endExpr().gt().val(1000)
		.and()
		.begin()
		.beginExpr().param("param1").mult().beginExpr().param("v").add().prop("wo.vehicle.initPrice").endExpr().endExpr().eq().prop("wo.insuranceAmount")
		.and()
		.val("a").isNull().and()
		.prop("wo.insuranceAmount").isNotNull()
		.end()
		.modelAsAggregate();
	entResultQry(a);//.getPropNames();

	select(WORK_ORDER).where()
		.upperCase()
				.beginExpr()
					.prop("bbb").add().prop("aaa")
				.endExpr()
			.like()
			.upperCase().val("AaA")
		.and()
			.prop("SomeDate").gt().now()
		.and()
			.yearOf().prop("LastChange").eq().round()
								.beginExpr()
									.prop("prop1").add().prop("prop2")
								.endExpr().to(1)
		.and()
			.param("a").eq().ifNull().prop("interProp").then().val(1)
		.and()
			.prop("aaa").eq().countDays().between().beginExpr().beginExpr().prop("Start1").add().prop("Start2").endExpr().endExpr().and().ifNull().prop("End").then().now()
		.and()
			.prop("haha").in().props("p1", "p2", "p3")
		.and()
			.param("AAA").in().values(1, 2, 3)
		.modelAsAggregate();

	final String expString = "SELECT\nFROM ua.com.fielden.platform.entity.AbstractEntity AS a\nWHERE (((($a + :v)))) > 1 AND ((:a * (:v + $c)) = $d AND $ad IS NOT NULL) AND UPPERCASE($bbb + $aaa) LIKE UPPERCASE(AaA) AND $SomeDate > NOW() AND YEAR($LastChange) = ROUND(($prop1 + $prop2), 1) AND :a = COALESCE($interProp, 1) AND $aaa = DATEDIFF(COALESCE($End, NOW()), (($Start1 + $Start2))) AND $haha IN ($p1, $p2, $p3) AND :AAA IN (1, 2, 3)";

	final Object b = select(a).as("a_alias").where().prop("a_alias.p1").eq().val(100).and().prop("a_alias.p2").like().anyOfValues("%AC", "%AB", "DD%").model();
    }

    @Ignore
    @Test
    public void test_expressions() {
	assertEquals("Incorrect expression model string representation", "1 + :a + 2", expr().val(1).add().param("a").add().val(2).model().toString());
	assertEquals("Incorrect expression model string representation", "1", expr().val(1).model().toString());
	assertEquals("Incorrect expression model string representation", "1 + :a + AVG(SUM($fuelCost)) + NOW() * SECOND(NOW())", expr().val(1).add().param("a").add().avgOf().expr(expr().sumOf().prop("fuelCost").model()).add().now().mult().expr(expr().secondOf().now().model()).model().toString());
    }

    @Ignore
    @Test
    public void test_query_model3() {
	final Object a = select(WORK_ORDER).as("a")
	    .join(AbstractEntity.class).as("b")
	    .on().prop("a").eq().prop("v").and()
	    .begin().begin().beginExpr().prop("a").add().param("v").endExpr().eq().prop("c").end().end()
	    .and()
	    .expr(null).eq().expr(null).and()
	    .prop("a")
	    	.eq()
	    .beginExpr()
	    	.prop("1").sub().prop("2")
	    .endExpr()
	    	.and().anyOfModels(null, null).eq().beginExpr().prop("a").endExpr()
	    	.and()
	    .beginExpr().beginExpr().beginExpr().beginExpr().param("1").add().param("2").endExpr().add().beginExpr().param("1").div().prop("2").endExpr().endExpr().endExpr().endExpr().eq().val(false)
	    .and()
	    .notExists(null)
	    .where()
	    .countDays().between().prop("1").and().prop("2").eq().beginExpr().countDays().between().model(null).and().model(null).endExpr().and()
	    .prop("1").ne().param("a")
	.and().beginExpr().prop("1").add().beginExpr().prop("1").add().prop("2").add().beginExpr().beginExpr().param("1").div().param("2").endExpr().div().beginExpr().param("3").endExpr().endExpr().endExpr().sub().prop("2").endExpr().eq().prop("a")
	.and().beginExpr().prop("2").endExpr().eq().beginExpr().prop("1").sub().prop("2").endExpr()
	.and().begin().prop("asa").isNull().end()
	.and().beginExpr().prop("dsd").endExpr().eq().param("ss").and().prop("a").like().beginExpr().val(1).add().val(3).endExpr().and()
	.begin().beginExpr().prop("1").sub().prop("2").endExpr().eq().prop("3").and()
		.begin().beginExpr().prop("1").sub().val(2).endExpr().isNotNull().and()
			.begin().exists(null).and().beginExpr().prop("1").div().val(2).mult().val(3).endExpr().eq().beginExpr().prop("1").add().prop("2").endExpr()
			.end()
		.end()
	.end().and()
	.countDays().between().beginExpr().beginExpr().beginExpr().val(1).sub().val(2).endExpr().endExpr().endExpr()
	.and()
	.beginExpr().beginExpr().beginExpr().param("1").div().param("2").endExpr().endExpr().endExpr()
	.eq()
	.countDays().between().prop("1").and().now()
	.and()
	.beginExpr().param("1").sub().upperCase().prop("1").endExpr().gt().all(null).and().val(1).isNotNull()
	.and()
	.beginExpr().prop("1").add().prop("2").endExpr().in().model(null)
	.and()
	.ifNull().prop("1").then().upperCase().val("AaAv")
		.eq()
	.beginExpr().ifNull().beginExpr().prop("1").add().prop("2").endExpr().then().now().sub().val(1).endExpr()
	.and()
	.anyOfModels(null, null).eq().prop("a")
	.and()
	.begin().begin().prop("a").eq().beginExpr().beginExpr().beginExpr().beginExpr().now().endExpr().endExpr().endExpr().endExpr().and().begin().beginExpr().beginExpr().beginExpr().beginExpr().now().endExpr().endExpr().endExpr().endExpr().eq().beginExpr().beginExpr().beginExpr().beginExpr().param("a").endExpr().endExpr().endExpr().add().beginExpr().beginExpr().beginExpr().prop("a").endExpr().endExpr().endExpr().endExpr().end().end().end()
	.or()
	.caseWhen().prop("1").eq().prop("2").and().val(1).ge().val(2).then().val(1).end().ge().param("bb")
	.and()
//	.begin().begin().condition(null).and().condition(null).or().prop("1").isNotNull().and().condition(null).and().prop("2").ge().caseWhen().condition(null).then().val(1).end().end().end()
//	.and()
//	.condition(null)
//	.and()
	.beginExpr().prop("a").add().prop("b").endExpr().in().props()
	.groupBy().param("aaa")
	.groupBy().prop("a")
	.groupBy().beginExpr().param("11").add().prop("2").mult().val(100).endExpr()
	.yield().param("a").as("a").yield().param("b").as("b")
	.yield().countDays().between().model(null).and().beginExpr().beginExpr().beginExpr().beginExpr().param("1").endExpr().endExpr().endExpr().endExpr().as("myExp")
	.yield().beginExpr().beginExpr().ifNull().prop("1").then().now().endExpr().endExpr().as("mySecondExp")
	.yield().beginExpr().avgOf().beginExpr().prop("1").add().param("2").endExpr().add().val(3).endExpr().as("avg_from1+3")
	.yield().avgOf().beginExpr().beginExpr().prop("1").add().prop("2").endExpr().endExpr().as("avg_of_1+2")
	.yield().beginExpr().beginExpr().avgOf().beginExpr().beginExpr().beginExpr().beginExpr().countDays().between().prop("1").and().val(100).endExpr().endExpr().endExpr().endExpr().add().val(2).endExpr().div().val(100).endExpr().as("alias")
	.yield().beginExpr().beginExpr().sumOf().prop("1").add().avgOf().prop("2").add().beginExpr().avgOf().beginExpr().beginExpr().beginExpr().beginExpr().val(2).endExpr().endExpr().endExpr().endExpr().endExpr().endExpr().add().avgOf().beginExpr().val(2).add().prop("p1").endExpr().endExpr().as("alias")
	.yield().caseWhen().prop("1").eq().prop("2").and().val(1).ge().val(2).then().val(1).end().as("aaa")
	.yield().round().prop("a").to(10).as("1")
	.yield().param("aa").as("aaa")
	.yield().countOf().prop("1").as("a")
	.yield().yearOf().now().as("b")
	.yield().join("b").as("c")
	.yield().countAll().as("recCount")
	//.model()
	;
    }



//    @Test
//    public void test_simple_query_model_22a() {
//	final EntityResultQueryModel<TgVehicle> subQry0 = query.select(TgVehicle.class).where().val(1).isNotNull().model();
//	final EntityResultQueryModel<TgVehicle> subQry1 = query.select(TgVehicle.class).where().exists(subQry0).model();
//	final EntityResultQueryModel<TgWorkOrder> subQry2 = query.select(TgWorkOrder.class).model();
//	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).as("v").where().existsAnyOf(subQry1, subQry2).model();
//	final List<EntQuery> exp = Arrays.asList(new EntQuery[]{entQuery1(subQry0), entQuery1(subQry2)});
//	assertEquals("models are different", exp, entQuery1(qry).getLeafSubqueries());
//    }
}