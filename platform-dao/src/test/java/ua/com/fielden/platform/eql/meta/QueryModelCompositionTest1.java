package ua.com.fielden.platform.eql.meta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.ArithmeticalOperator;
import ua.com.fielden.platform.entity.query.fluent.ComparisonOperator;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere0;
import ua.com.fielden.platform.entity.query.fluent.JoinType;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel;
import ua.com.fielden.platform.eql.s1.elements.CaseWhen1;
import ua.com.fielden.platform.eql.s1.elements.ComparisonTest1;
import ua.com.fielden.platform.eql.s1.elements.CompoundCondition1;
import ua.com.fielden.platform.eql.s1.elements.CompoundSingleOperand1;
import ua.com.fielden.platform.eql.s1.elements.CompoundSource1;
import ua.com.fielden.platform.eql.s1.elements.Conditions1;
import ua.com.fielden.platform.eql.s1.elements.DayOf1;
import ua.com.fielden.platform.eql.s1.elements.EntProp1;
import ua.com.fielden.platform.eql.s1.elements.EntQuery1;
import ua.com.fielden.platform.eql.s1.elements.ExistenceTest1;
import ua.com.fielden.platform.eql.s1.elements.Expression1;
import ua.com.fielden.platform.eql.s1.elements.GroupBy1;
import ua.com.fielden.platform.eql.s1.elements.GroupBys1;
import ua.com.fielden.platform.eql.s1.elements.ICondition1;
import ua.com.fielden.platform.eql.s1.elements.ISingleOperand1;
import ua.com.fielden.platform.eql.s1.elements.LikeTest1;
import ua.com.fielden.platform.eql.s1.elements.MonthOf1;
import ua.com.fielden.platform.eql.s1.elements.Now1;
import ua.com.fielden.platform.eql.s1.elements.NullTest1;
import ua.com.fielden.platform.eql.s1.elements.OrderBy1;
import ua.com.fielden.platform.eql.s1.elements.OrderBys1;
import ua.com.fielden.platform.eql.s1.elements.QuantifiedTest1;
import ua.com.fielden.platform.eql.s1.elements.QueryBasedSet1;
import ua.com.fielden.platform.eql.s1.elements.QueryBasedSource1;
import ua.com.fielden.platform.eql.s1.elements.SetTest1;
import ua.com.fielden.platform.eql.s1.elements.Sources1;
import ua.com.fielden.platform.eql.s1.elements.TypeBasedSource1;
import ua.com.fielden.platform.eql.s1.elements.YearOf1;
import ua.com.fielden.platform.eql.s1.elements.Yield1;
import ua.com.fielden.platform.eql.s1.elements.Yields1;
import ua.com.fielden.platform.eql.s2.elements.ICondition2;
import ua.com.fielden.platform.eql.s2.elements.ISingleOperand2;
import ua.com.fielden.platform.sample.domain.TgFuelUsage;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.sample.domain.TgWorkOrder;
import ua.com.fielden.platform.utils.Pair;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;


public class QueryModelCompositionTest1 extends BaseEntQueryCompositionTCase1 {
    protected final IWhere0<TgVehicle> where_veh = select(VEHICLE).where();
    protected final IWhere0<TgWorkOrder> where_wo = select(WORK_ORDER).as("wo").where();


    @Test
    public void test_user_data_filtering() {
	assertModelsEqualsAccordingUserDataFiltering(//
		select(VEHICLE). //
		where().prop("model.make.key").eq().val("MERC").model(),

		select(VEHICLE). //
		where().begin().prop("key").notLike().val("A%").end().and().begin().prop("model.make.key").eq().val("MERC").end().model());
    }

    /////////////////////////////////////////// Conditions ////////////////////////////////////////////////////
    @Test
    public void test_like() {
	assertModelsEquals( //
		conditions(new LikeTest1(prop("model.desc"), val(mercLike), false, false)), //
		conditions(where_veh.prop("model.desc").like().val(mercLike)));
    }

    @Test
    public void test_notLike() {
	assertModelsEquals( //
		conditions(new LikeTest1(prop("model.desc"), val(mercLike), true, false)), //
		conditions(where_veh.prop("model.desc").notLike().val(mercLike)));
    }

    @Test
    public void test_set_test_with_values() {
	assertModelsEquals(//
		conditions(new SetTest1(prop("model"), false, set(val(merc), val(audi)))), //
		conditions(where_veh.prop("model").in().values(merc, audi)));
    }

    @Test
    public void test_set_test_with_query() {
	assertModelsEquals( //
		conditions(new SetTest1(prop("model"), false, new QueryBasedSet1(entSubQry(select(MODEL).model())))), //
		conditions(where_veh.prop("model").in().model(select(MODEL).model())));
    }

    @Test
    public void test_plain_quantified_test() {
	final EntityResultQueryModel<TgVehicleModel> vehModels = select(MODEL).model();
	assertModelsEquals( //
		conditions(new QuantifiedTest1(prop("model"), _eq, Quantifier.ANY, entSubQry(vehModels))), //
		conditions(where_veh.prop("model").eq().any(vehModels)));
    }

    @Test
    public void test_simple_query_model_01() {
	assertModelsEquals( //
		conditions(new NullTest1(prop("model"), true), //
			compound(_and, new NullTest1(prop("station"), false))), //
		conditions(where_veh.prop("model").isNotNull().and().prop("station").isNull()));
    }

    @Test
    public void test_simple_query_model_02() {
	assertModelsEquals(//
		conditions(new ComparisonTest1(prop("price"), _gt, val(100)), //
			compound(_and, new ComparisonTest1(prop("purchasePrice"), _lt, prop("price")))), //
		conditions(where_veh.prop("price").gt().val(100).and().prop("purchasePrice").lt().prop("price")));
    }

    @Test
    public void test_simple_query_model_02a() {
	final List<Pair<ICondition1<? extends ICondition2>, ISingleOperand1<? extends ISingleOperand2>>> whenThenPairs = new ArrayList<>();
	whenThenPairs.add(new Pair<ICondition1<? extends ICondition2>, ISingleOperand1<? extends ISingleOperand2>>(new ComparisonTest1(prop("price"), _gt, val(100)), prop("purchasePrice")));
	final CaseWhen1 caseWhen = new CaseWhen1(whenThenPairs, prop("price"));
	assertModelsEquals(//
		conditions(new ComparisonTest1(caseWhen, _eq, val(0))), //
		conditions(where_veh.caseWhen().prop("price").gt().val(100).then().prop("purchasePrice").otherwise().prop("price").end().eq().val(0)));
    }

    @Test
    public void test_simple_query_model_03() {
	final Map<String, Object> paramValues = new HashMap<String, Object>();
	paramValues.put("eqclass", "CL1");

	assertModelsEquals(//
		conditions(new NullTest1(prop("model"), true), //
			compound(_and, new NullTest1(param("eqclass"), false))), //
		conditions(where_veh.prop("model").isNotNull().and().param("eqclass").isNull(), paramValues));
    }

    @Test
    public void test_simple_query_model_04() {
	assertModelsEquals(//
		conditions(group(false, //
			new NullTest1(prop("model"), true), //
			compound(_and, new NullTest1(prop("station"), false)))), //
		conditions(where_veh.begin().prop("model").isNotNull().and().prop("station").isNull().end()));
    }

    @Test
    public void test_simple_query_model_05() {
	assertModelsEquals( //
	conditions(group(false, //
		new NullTest1(prop("model"), true), //
		compound(_and, new NullTest1(prop("station"), false))), //
		compound(_and, new NullTest1(prop("price"), true))), //
		conditions(where_veh.begin().prop("model").isNotNull().and().prop("station").isNull().end().and().prop("price").isNotNull()));
    }

    @Test
    public void test_simple_query_model_06() {
	assertModelsEquals(//
	conditions(new NullTest1(prop("model"), true), //
		compound(_and, new ExistenceTest1(false, entSubQry(select(VEHICLE).model())))), //
		conditions(where_veh.prop("model").isNotNull().and().exists(select(VEHICLE).model())));
    }

    @Test
    public void test_simple_query_model_07() {
	assertModelsEquals(//
	conditions(group(false, new NullTest1(prop("model"), true), //
		compound(_or, new NullTest1(prop("station"), true)))), //
		conditions(where_veh.anyOfProps("model", "station").isNotNull()));
    }

    @Test
    public void test_simple_query_model_08() {
	assertModelsEquals(//
	conditions(new NullTest1(expression(prop("price"), compound(_add, prop("purchasePrice"))), false)), //
		conditions(where_veh.beginExpr().prop("price").add().prop("purchasePrice").endExpr().isNull()));
    }

    @Test
    public void test_simple_query_model_09() {
	assertModelsEquals(//
	conditions(new NullTest1(expression(prop("price.amount"), compound(_add, prop("purchasePrice.amount"))), false)), //
		conditions(where_veh.expr(expr().prop("price.amount").add().prop("purchasePrice.amount").model()).isNull()));
    }

    @Test
    public void test_simple_query_model_10() {
	assertModelsEquals(//
	conditions(new NullTest1(expression(prop("model"), compound(_add, expression(prop("model"), compound(_mult, prop("station"))))), false)), //
		conditions(where_veh.expr(expr().prop("model").add().expr(expr().prop("model").mult().prop("station").model()).model()).isNull()));
    }

    @Test
    public void test_simple_query_model_11() {
	assertModelsEquals(//
		conditions(new ComparisonTest1(new DayOf1(prop("initDate")), _gt, val(15)), compound(_and, new ComparisonTest1(prop("price"), _lt, prop("purchasePrice")))), //
		conditions(where_veh.dayOf().prop("initDate").gt().val(15).and().prop("price").lt().prop("purchasePrice")));
    }

    @Test
    public void test_simple_query_model_12() {
	assertModelsEquals(//
		conditions(new ComparisonTest1(new MonthOf1(prop("initDate")), _gt, val(3)), compound(_and, new ComparisonTest1(prop("price"), _lt, prop("purchasePrice")))), //
		conditions(where_veh.monthOf().prop("initDate").gt().val(3).and().prop("price").lt().prop("purchasePrice")));
    }

    @Test
    public void test_simple_query_model_13() {
	assertModelsEquals(//
		conditions(group(false, //
			new ExistenceTest1(false, entSubQry(select(VEHICLE).model())), //
			compound(_or, //
				new ExistenceTest1(false, entSubQry(select(WORK_ORDER).model()))))), //
		conditions(where_veh.existsAnyOf(select(VEHICLE).model(), select(WORK_ORDER).model())));
    }

    @Test
    public void test_ignore_of_null_value_in_condition1() {
	assertModelsEquals(//
		conditions(new LikeTest1(prop("model.desc"), val(mercLike), false, false)), //
		conditions(where_veh.prop("model.desc").like().iVal(mercLike)));
    }

    @Test
    public void test_ignore_of_null_value_in_condition2() {
	assertModelsEquals(//
		conditions(new LikeTest1(prop("model.desc"), iVal(null), false, false)), //
		conditions(where_veh.prop("model.desc").like().iVal(null)));
    }

    @Test
    public void test_ignore_of_null_value_in_condition3() {
	final Map<String, Object> paramValues = new HashMap<String, Object>();
	paramValues.put("param", "MERC%");
	assertModelsEquals(//
		conditions(new LikeTest1(prop("model.desc"), param("param"), false, false)), //
		conditions(where_veh.prop("model.desc").like().param("param"), paramValues));
    }

    @Test
    public void test_ignore_of_null_value_in_condition4() {
	final Map<String, Object> paramValues = new HashMap<String, Object>();
	paramValues.put("param", null);
	assertModelsEquals(//
		conditions(new LikeTest1(prop("model.desc"), iParam("param"), false, false)), //
		conditions(where_veh.prop("model.desc").like().iParam("param"), paramValues));
    }

    @Test
    public void test_ignore_of_null_value_in_condition5() {
	assertModelsEquals(//
		conditions(group(false, null)), //
		conditions(where_veh.prop("model.desc").like().anyOfValues()));
    }

    @Test
    public void test_expressions1() {
	final Map<String, Object> paramValues = new HashMap<String, Object>();
	paramValues.put("costMultiplier", 1);
	paramValues.put("costDivider", 2);

	assertModelsEquals(//
		conditions(new ComparisonTest1(
			expression(
				expression(
					expression(
						expression(prop("wo.actCost.amount"), compound( _mult, param("costMultiplier"))), //
						compound(_add, prop("wo.estCost.amount")), compound(_div, param("costDivider"))), //
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

	final Conditions1 condition1 = conditions(new ComparisonTest1(prop("v"), _eq, prop("wo.vehicle")));
	final Conditions1 condition2 = conditions(new ComparisonTest1(prop("v"), _eq, prop("wo2.vehicle")));

	final List<CompoundSource1> others = new ArrayList<CompoundSource1>();
	others.add(new CompoundSource1(new TypeBasedSource1(DOMAIN_METADATA_ANALYSER.getEntityMetadata(WORK_ORDER), "wo"), JoinType.IJ, condition1));
	others.add(new CompoundSource1(new TypeBasedSource1(DOMAIN_METADATA_ANALYSER.getEntityMetadata(WORK_ORDER), "wo2"), JoinType.LJ, condition2));

	final EntQuery1 act = entResultQry(qry);
	final Sources1 exp = new Sources1(new TypeBasedSource1(DOMAIN_METADATA_ANALYSER.getEntityMetadata(VEHICLE), "v"), others);
	assertEquals("models are different", exp, act.getSources());

	final List<CompoundCondition1> others2 = new ArrayList<CompoundCondition1>();
	others2.add(new CompoundCondition1(_and, new ComparisonTest1(prop("price"), _lt, prop("purchasePrice"))));
	final Conditions1 exp2 = conditions(new ComparisonTest1(new DayOf1(prop("initDate")), _gt, val(15)), others2.toArray(new CompoundCondition1[]{}));
	assertEquals(exp2, act.getConditions());
    }

    //////////////////////////////////////////////////// Grouping & Ordering //////////////////////////////////////////////////////////////
    @Test
    public void test_query_with_one_group() {
	final EntityResultQueryModel<TgVehicleModel> qry = select(VEHICLE).groupBy().prop("model").yield().prop("model").modelAsEntity(MODEL);
	final EntQuery1 act = entResultQry(qry);

	final Yields1 exp = new Yields1();
	exp.addYield(new Yield1(prop("model"), ""));

	assertEquals("models are different", exp, act.getYields());

	final List<GroupBy1> groups = new ArrayList<>();
	groups.add(new GroupBy1(prop("model")));
	final GroupBys1 exp2 = new GroupBys1(groups);
	assertEquals("models are different", exp2, act.getGroups());
    }

    @Test
    public void test_query_with_several_groups() {
	final AggregatedResultQueryModel qry = select(VEHICLE).groupBy().prop("model").groupBy().yearOf().prop("initDate").yield().prop("model").as("model").yield().yearOf().prop("initDate").as("initYear").modelAsAggregate();
	final EntQuery1 act = entResultQry(qry);
	final YearOf1 yearOfModel = new YearOf1(prop("initDate"));
	final EntProp1 eqClassProp = prop("model");

	final Yields1 exp = new Yields1();
	exp.addYield(new Yield1(eqClassProp, "model"));
	exp.addYield(new Yield1(yearOfModel, "initYear"));
	assertEquals("models are different", exp, act.getYields());

	final List<GroupBy1> groups = new ArrayList<>();
	groups.add(new GroupBy1(eqClassProp));
	groups.add(new GroupBy1(yearOfModel));
	final GroupBys1 exp2 = new GroupBys1(groups);
	assertEquals("models are different", exp2, act.getGroups());
    }

    @Test
    public void test_query_ordering() {
	final EntityResultQueryModel<TgVehicleModel> qry = select(VEHICLE).groupBy().prop("model").yield().prop("model").modelAsEntity(MODEL);
	final OrderingModel orderModel = orderBy().beginExpr().prop("model").add().now().endExpr().asc().prop("key").desc().model();
	final EntQuery1 act = entResultQry(qry, orderModel);

	final List<OrderBy1> orderings = new ArrayList<>();
	orderings.add(new OrderBy1(expression(prop("model"), compound(_add, new Now1())), false));
	orderings.add(new OrderBy1(prop("key"), true));
	final OrderBys1 exp2 = new OrderBys1(orderings);
	assertEquals("models are different", exp2, act.getOrderings());
    }


    //////////////////////////////////////////////////////// Yielding ///////////////////////////////////////////////////////////////////
    @Test
    public void test1() {
	final Map<String, Object> paramValues = new HashMap<String, Object>();
	paramValues.put("param", 20);
	final AggregatedResultQueryModel qry = select(VEHICLE).yield().prop("station").as("st").yield().beginExpr().prop("model").add().param("param").endExpr().as("m").modelAsAggregate();
	final Yields1 exp = new Yields1();
	exp.addYield(new Yield1(prop("station"), "st"));
	final List<CompoundSingleOperand1> compSingleOperands = new ArrayList<>();
	compSingleOperands.add(new CompoundSingleOperand1(param("param"), ArithmeticalOperator.ADD));
	final Expression1 expression = expression(prop("model"), compSingleOperands.toArray(new CompoundSingleOperand1[]{}));
	exp.addYield(new Yield1(expression, "m"));
	assertEquals("models are different", exp, entResultQry(qry, paramValues).getYields());
    }

    @Test
    public void test2() {
	final EntityResultQueryModel<TgVehicleModel> qry = select(VEHICLE).yield().prop("model").modelAsEntity(MODEL);
	final Yields1 exp = new Yields1();
	exp.addYield(new Yield1(prop("model"), ""));
	assertEquals("models are different", exp, entResultQry(qry).getYields());
    }

    @Test
    public void test3() {
	final AggregatedResultQueryModel qry = select(VEHICLE).yield().prop("station").as("st").yield().prop("model").as("m").modelAsAggregate();
	final Yields1 exp = new Yields1();
	exp.addYield(new Yield1(prop("station"), "st"));
	exp.addYield(new Yield1(prop("model"), "m"));
	assertEquals("models are different", exp, entResultQry(qry).getYields());
    }

    @Test
    public void test4() {
	final EntityResultQueryModel<TgVehicleModel> qry = select(VEHICLE).as("v").yield().prop("v.model").modelAsEntity(MODEL);
	final Yields1 exp = new Yields1();
	exp.addYield(new Yield1(prop("v.model"), ""));
	assertEquals("models are different", exp, entResultQry(qry).getYields());
    }

    @Test
    public void test5() {
	final PrimitiveResultQueryModel qry = select(VEHICLE).as("v").yield().prop("v.model").modelAsPrimitive();
	final Yields1 exp = new Yields1();
	exp.addYield(new Yield1(prop("v.model"), ""));
	assertEquals("models are different", exp, entSubQry(qry).getYields());
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

	final Conditions1 condition = conditions(new ComparisonTest1(prop("v"), ComparisonOperator.EQ, prop("vehicle")));

	final List<CompoundSource1> others = new ArrayList<>();
	others.add(new CompoundSource1(new TypeBasedSource1(DOMAIN_METADATA_ANALYSER.getEntityMetadata(WORK_ORDER), "wo"), JoinType.IJ, condition));

	final Sources1 exp = new Sources1(new TypeBasedSource1(DOMAIN_METADATA_ANALYSER.getEntityMetadata(VEHICLE), "v"), others);
	assertEquals("models are different", exp, entResultQry(qry).getSources());
    }

    @Test
    public void test_query_sources1a() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).as("v").join(WORK_ORDER).as("wo").on().prop("v").eq().prop("wo.vehicle").where().val(1).isNotNull().model();

	final Conditions1 condition = conditions(new ComparisonTest1(prop("v"), ComparisonOperator.EQ, prop("wo.vehicle")));

	final List<CompoundSource1> others = new ArrayList<CompoundSource1>();
	others.add(new CompoundSource1(new TypeBasedSource1(DOMAIN_METADATA_ANALYSER.getEntityMetadata(WORK_ORDER), "wo"), JoinType.IJ, condition));

	final Sources1 exp = new Sources1(new TypeBasedSource1(DOMAIN_METADATA_ANALYSER.getEntityMetadata(VEHICLE), "v"), others);
	assertEquals("models are different", exp, entResultQry(qry).getSources());
    }

    @Test
    public void test_query_sources_with_explicit_join_and_without_aliases() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).as("v").join(WORK_ORDER).on().prop("v").eq().prop("vehicle").model();

	final Conditions1 condition = conditions(new ComparisonTest1(prop("v"), ComparisonOperator.EQ, prop("vehicle")));

	final List<CompoundSource1> others = new ArrayList<CompoundSource1>();
	others.add(new CompoundSource1(new TypeBasedSource1(DOMAIN_METADATA_ANALYSER.getEntityMetadata(WORK_ORDER), null), JoinType.IJ, condition));

	final Sources1 exp = new Sources1(new TypeBasedSource1(DOMAIN_METADATA_ANALYSER.getEntityMetadata(VEHICLE), "v"), others);
	assertEquals("models are different", exp, entResultQry(qry).getSources());
    }

    @Test
    public void test_query_sources2() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).as("v").join(WORK_ORDER).as("wo").on().prop("v").eq().prop("wo.vehicle").leftJoin(WORK_ORDER).as("wo2").on().prop("v").eq().prop("wo2.vehicle").model();

	final Conditions1 condition1 = conditions(new ComparisonTest1(prop("v"), ComparisonOperator.EQ, prop("wo.vehicle")));
	final Conditions1 condition2 = conditions(new ComparisonTest1(prop("v"), ComparisonOperator.EQ, prop("wo2.vehicle")));

	final List<CompoundSource1> others = new ArrayList<CompoundSource1>();
	others.add(new CompoundSource1(new TypeBasedSource1(DOMAIN_METADATA_ANALYSER.getEntityMetadata(WORK_ORDER), "wo"), JoinType.IJ, condition1));
	others.add(new CompoundSource1(new TypeBasedSource1(DOMAIN_METADATA_ANALYSER.getEntityMetadata(WORK_ORDER), "wo2"), JoinType.LJ, condition2));

	final Sources1 exp = new Sources1(new TypeBasedSource1(DOMAIN_METADATA_ANALYSER.getEntityMetadata(VEHICLE), "v"), others);
	assertEquals("models are different", exp, entResultQry(qry).getSources());
    }

    @Test
    public void test_query_with_derived_sources() {
	final EntityResultQueryModel<TgVehicle> sourceQry = select(VEHICLE).as("v").where().prop("v.model").isNotNull().model();
	final EntityResultQueryModel<TgVehicle> qry = select(sourceQry).as("v").where().prop("v.model").isNotNull().model();

	final Sources1 exp = new Sources1(new QueryBasedSource1("v", entSourceQry(sourceQry)), new ArrayList<CompoundSource1>());
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
	final List<CompoundSource1> others = new ArrayList<CompoundSource1>();
	final Sources1 exp = new Sources1(new TypeBasedSource1(DOMAIN_METADATA_ANALYSER.getEntityMetadata(VEHICLE), "v"), others);
	assertEquals("models are different", exp, entResultQry(qry).getSources());
    }

    @Test
    public void test_simple_query_model_14() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).model();
	final List<CompoundSource1> others = new ArrayList<CompoundSource1>();
	final Sources1 exp = new Sources1(new TypeBasedSource1(DOMAIN_METADATA_ANALYSER.getEntityMetadata(VEHICLE), null), others);
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
			.prop("aaa").eq().count().days().between().beginExpr().beginExpr().prop("Start1").add().prop("Start2").endExpr().endExpr().and().ifNull().prop("End").then().now()
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
	    .count().days().between().prop("1").and().prop("2").eq().beginExpr().count().days().between().model(null).and().model(null).endExpr().and()
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
	.count().days().between().beginExpr().beginExpr().beginExpr().val(1).sub().val(2).endExpr().endExpr().endExpr()
	.and()
	.beginExpr().beginExpr().beginExpr().param("1").div().param("2").endExpr().endExpr().endExpr()
	.eq()
	.count().days().between().prop("1").and().now()
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
	.yield().count().days().between().model(null).and().beginExpr().beginExpr().beginExpr().beginExpr().param("1").endExpr().endExpr().endExpr().endExpr().as("myExp")
	.yield().beginExpr().beginExpr().ifNull().prop("1").then().now().endExpr().endExpr().as("mySecondExp")
	.yield().beginExpr().avgOf().beginExpr().prop("1").add().param("2").endExpr().add().val(3).endExpr().as("avg_from1+3")
	.yield().avgOf().beginExpr().beginExpr().prop("1").add().prop("2").endExpr().endExpr().as("avg_of_1+2")
	.yield().beginExpr().beginExpr().avgOf().beginExpr().beginExpr().beginExpr().beginExpr().count().days().between().prop("1").and().val(100).endExpr().endExpr().endExpr().endExpr().add().val(2).endExpr().div().val(100).endExpr().as("alias")
	.yield().beginExpr().beginExpr().sumOf().prop("1").add().avgOf().prop("2").add().beginExpr().avgOf().beginExpr().beginExpr().beginExpr().beginExpr().val(2).endExpr().endExpr().endExpr().endExpr().endExpr().endExpr().add().avgOf().beginExpr().val(2).add().prop("p1").endExpr().endExpr().as("alias")
	.yield().caseWhen().prop("1").eq().prop("2").and().val(1).ge().val(2).then().val(1).end().as("aaa")
	.yield().round().prop("a").to(10).as("1")
	.yield().param("aa").as("aaa")
	.yield().countOf().prop("1").as("a")
	.yield().yearOf().now().as("b")
//	.yield().join("b").as("c")
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