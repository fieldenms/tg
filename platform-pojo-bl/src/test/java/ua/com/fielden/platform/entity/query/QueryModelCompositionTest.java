package ua.com.fielden.platform.entity.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.query;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.entity.query.model.builders.DbVersion;
import ua.com.fielden.platform.entity.query.model.builders.EntQueryGenerator;
import ua.com.fielden.platform.entity.query.model.elements.ArithmeticalOperator;
import ua.com.fielden.platform.entity.query.model.elements.ComparisonOperator;
import ua.com.fielden.platform.entity.query.model.elements.ComparisonTestModel;
import ua.com.fielden.platform.entity.query.model.elements.CompoundConditionModel;
import ua.com.fielden.platform.entity.query.model.elements.CompoundSingleOperand;
import ua.com.fielden.platform.entity.query.model.elements.ConditionsModel;
import ua.com.fielden.platform.entity.query.model.elements.DayOfModel;
import ua.com.fielden.platform.entity.query.model.elements.EntProp;
import ua.com.fielden.platform.entity.query.model.elements.EntQuery;
import ua.com.fielden.platform.entity.query.model.elements.EntQueryCompoundSourceModel;
import ua.com.fielden.platform.entity.query.model.elements.EntQuerySourceAsEntity;
import ua.com.fielden.platform.entity.query.model.elements.EntQuerySourceAsModel;
import ua.com.fielden.platform.entity.query.model.elements.EntQuerySourcesModel;
import ua.com.fielden.platform.entity.query.model.elements.EntSet;
import ua.com.fielden.platform.entity.query.model.elements.EntSetFromQryModel;
import ua.com.fielden.platform.entity.query.model.elements.EntValue;
import ua.com.fielden.platform.entity.query.model.elements.ExistenceTestModel;
import ua.com.fielden.platform.entity.query.model.elements.Expression;
import ua.com.fielden.platform.entity.query.model.elements.GroupModel;
import ua.com.fielden.platform.entity.query.model.elements.GroupedConditionsModel;
import ua.com.fielden.platform.entity.query.model.elements.GroupsModel;
import ua.com.fielden.platform.entity.query.model.elements.ISingleOperand;
import ua.com.fielden.platform.entity.query.model.elements.JoinType;
import ua.com.fielden.platform.entity.query.model.elements.LikeTestModel;
import ua.com.fielden.platform.entity.query.model.elements.LogicalOperator;
import ua.com.fielden.platform.entity.query.model.elements.MonthOfModel;
import ua.com.fielden.platform.entity.query.model.elements.NullTestModel;
import ua.com.fielden.platform.entity.query.model.elements.QuantifiedTestModel;
import ua.com.fielden.platform.entity.query.model.elements.Quantifier;
import ua.com.fielden.platform.entity.query.model.elements.SetTestModel;
import ua.com.fielden.platform.entity.query.model.elements.YieldModel;
import ua.com.fielden.platform.entity.query.model.elements.YieldsModel;
import ua.com.fielden.platform.entity.query.model.transformation.QueryModelEnhancer;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.sample.domain.TgWorkOrder;
import static org.junit.Assert.assertEquals;

public class QueryModelCompositionTest {
    private final EntQueryGenerator qb = new EntQueryGenerator(DbVersion.H2);

    @Test
    public void test_query_model1() {
	final AggregatedResultQueryModel a =
		query.select(TgWorkOrder.class).as("wo")
		.where().beginExpr().beginExpr().beginExpr().beginExpr().prop("wo.actCost.amount").mult().param("costMultiplier").endExpr().add().prop("wo.estCost.amount").div().param("costDivider").endExpr().add().prop("wo.yearlyCost.amount").endExpr().div().val(12).endExpr().gt().val(1000)
		.and()
		.begin()
		.beginExpr().param("param1").mult().beginExpr().param("v").add().prop("wo.vehicle.initPrice").endExpr().endExpr().eq().prop("wo.insuranceAmount")
		.and()
		.prop("wo.insuranceAmount").isNotNull()
		.end()
		.modelAsAggregate();
	qb.generateEntQuery(a);//.getPropNames();

	query.select(TgWorkOrder.class).where()
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

	final Object b = query.select(a).as("a_alias").where().prop("a_alias.p1").eq().val(100).and().prop("a_alias.p2").like().anyOfValues("%AC", "%AB", "DD%").model();
    }

    @Ignore
    @Test
    public void test_expressions() {
	assertEquals("Incorrect expression model string representation", "1 + :a + 2", query.expr().val(1).add().param("a").add().val(2).model().toString());
	assertEquals("Incorrect expression model string representation", "1", query.expr().val(1).model().toString());
	assertEquals("Incorrect expression model string representation", "1 + :a + AVG(SUM($fuelCost)) + NOW() * SECOND(NOW())", query.expr().val(1).add().param("a").add().avgOf().expr(query.expr().sumOf().prop("fuelCost").model()).add().now().mult().expr(query.expr().secondOf().now().model()).model().toString());
    }

    @Ignore
    @Test
    public void test_query_model3() {
	final Object a = query.select(AbstractEntity.class).as("a")
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
	.orderBy().prop("1").asc()
	.orderBy().model(null).asc()
	//.model()
	;
    }

    @Test
    public void test_prop_collector() {
	final AggregatedResultQueryModel qry = query.select(TgVehicle.class).where().prop("model.desc").like().val("MERC%").
	groupBy().prop("eqClass.desc").
	yield().beginExpr().prop("volume").add().prop("weight").endExpr().as("calc").modelAsAggregate();
	final Set<String> exp = new HashSet<String>();
	exp.add("model.desc");
	exp.add("eqClass.desc");
	exp.add("weight");
	exp.add("volume");
	assertEquals("models are different", exp, qb.generateEntQuery(qry).getImmediatePropNames());
    }

    @Test
    public void test_prop_collector_with_subquery() {
	final EntityResultQueryModel<TgVehicleModel> vehModelsQry = query.select(TgVehicleModel.class).where().prop("make").isNotNull().model();
	final AggregatedResultQueryModel qry = query.select(TgVehicle.class).where().prop("model").in().model(vehModelsQry).
	groupBy().prop("eqClass.desc").
	yield().beginExpr().prop("volume").add().prop("weight").endExpr().as("calc").modelAsAggregate();
	final Set<String> exp = new HashSet<String>();
	exp.add("model");
	exp.add("eqClass.desc");
	exp.add("weight");
	exp.add("volume");
	//exp.add("make");
	assertEquals("models are different", exp, qb.generateEntQuery(qry).getImmediatePropNames());
    }

//    @Test
//    public void test_source_names_collector() {
//	final AggregatedResultQueryModel qry = query.select(TgVehicle.class).modelAsAggregate();
//	final Set<String> exp = new HashSet<String>();
//	assertEquals("models are different", exp, qb.generateEntQuery(qry).getQrySourcesNames());
//    }
//
//    @Test
//    public void test_source_names_collector_with_joins() {
//	final AggregatedResultQueryModel qry = query.select(TgVehicle.class).as("v").where().prop("v.model.desc").like().val("MERC%").
//	groupBy().prop("v.eqClass.desc").
//	yield().beginExpr().prop("v.volume").add().prop("v.weight").endExpr().as("calc").modelAsAggregate();
//	final Set<String> exp = new HashSet<String>();
//	exp.add("v");
//	assertEquals("models are different", exp, qb.generateEntQuery(qry).getQrySourcesNames());
//    }
//
//    @Test
//    public void test_source_names_collector2() {
//	final AggregatedResultQueryModel qry = query.select(TgVehicle.class).as("v").leftJoin(TgVehicleModel.class).as("v.model").on().prop("v.model").eq().prop("v.model.id").
//	where().prop("v.model.desc").like().val("MERC%").
//	groupBy().prop("v.eqClass.desc").
//	yield().beginExpr().prop("v.volume").add().prop("v.weight").endExpr().as("calc").modelAsAggregate();
//	final Set<String> exp = new HashSet<String>();
//	exp.add("v");
//	exp.add("v.model");
//	assertEquals("models are different", exp, qb.generateEntQuery(qry).getQrySourcesNames());
//    }

    @Test
    public void test_query_sources1() {
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).as("v").join(TgWorkOrder.class).as("wo").on().prop("a").eq().prop("b").model();

	final ConditionsModel condition = new ConditionsModel(new ComparisonTestModel(new EntProp("a"), ComparisonOperator.EQ, new EntProp("b")), new ArrayList<CompoundConditionModel>());

	final List<EntQueryCompoundSourceModel> others = new ArrayList<EntQueryCompoundSourceModel>();
	others.add(new EntQueryCompoundSourceModel(new EntQuerySourceAsEntity(TgWorkOrder.class, "wo"), JoinType.IJ, condition));

	final EntQuerySourcesModel exp = new EntQuerySourcesModel(new EntQuerySourceAsEntity(TgVehicle.class, "v"), others);
	assertEquals("models are different", exp, qb.generateEntQuery(qry).getSources());
    }

    @Test
    public void test_query_sources_with_explicit_join_and_without_aliases() {
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).as("v").join(TgWorkOrder.class).on().prop("a").eq().prop("b").model();

	final ConditionsModel condition = new ConditionsModel(new ComparisonTestModel(new EntProp("a"), ComparisonOperator.EQ, new EntProp("b")), new ArrayList<CompoundConditionModel>());

	final List<EntQueryCompoundSourceModel> others = new ArrayList<EntQueryCompoundSourceModel>();
	others.add(new EntQueryCompoundSourceModel(new EntQuerySourceAsEntity(TgWorkOrder.class, null), JoinType.IJ, condition));

	final EntQuerySourcesModel exp = new EntQuerySourcesModel(new EntQuerySourceAsEntity(TgVehicle.class, "v"), others);
	assertEquals("models are different", exp, qb.generateEntQuery(qry).getSources());
    }

    @Test
    public void test_query_sources2() {
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).as("v").join(TgWorkOrder.class).as("wo").on().prop("a").eq().prop("b").leftJoin(TgWorkOrder.class).as("wo2").on().prop("a2").eq().prop("b2").model();

	final ConditionsModel condition1 = new ConditionsModel(new ComparisonTestModel(new EntProp("a"), ComparisonOperator.EQ, new EntProp("b")), new ArrayList<CompoundConditionModel>());
	final ConditionsModel condition2 = new ConditionsModel(new ComparisonTestModel(new EntProp("a2"), ComparisonOperator.EQ, new EntProp("b2")), new ArrayList<CompoundConditionModel>());

	final List<EntQueryCompoundSourceModel> others = new ArrayList<EntQueryCompoundSourceModel>();
	others.add(new EntQueryCompoundSourceModel(new EntQuerySourceAsEntity(TgWorkOrder.class, "wo"), JoinType.IJ, condition1));
	others.add(new EntQueryCompoundSourceModel(new EntQuerySourceAsEntity(TgWorkOrder.class, "wo2"), JoinType.LJ, condition2));

	final EntQuerySourcesModel exp = new EntQuerySourcesModel(new EntQuerySourceAsEntity(TgVehicle.class, "v"), others);
	assertEquals("models are different", exp, qb.generateEntQuery(qry).getSources());
    }

    @Test
    public void test_query_sources3() {
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).as("v").join(TgWorkOrder.class).as("wo").on().prop("a").eq().prop("b").leftJoin(TgWorkOrder.class).as("wo2").on().prop("a2").eq().prop("b2")
	.where().dayOf().prop("initDate").gt().val(15).and().prop("eqClass").lt().prop("limit").model();

	final ConditionsModel condition1 = new ConditionsModel(new ComparisonTestModel(new EntProp("a"), ComparisonOperator.EQ, new EntProp("b")), new ArrayList<CompoundConditionModel>());
	final ConditionsModel condition2 = new ConditionsModel(new ComparisonTestModel(new EntProp("a2"), ComparisonOperator.EQ, new EntProp("b2")), new ArrayList<CompoundConditionModel>());

	final List<EntQueryCompoundSourceModel> others = new ArrayList<EntQueryCompoundSourceModel>();
	others.add(new EntQueryCompoundSourceModel(new EntQuerySourceAsEntity(TgWorkOrder.class, "wo"), JoinType.IJ, condition1));
	others.add(new EntQueryCompoundSourceModel(new EntQuerySourceAsEntity(TgWorkOrder.class, "wo2"), JoinType.LJ, condition2));

	final EntQuery act = qb.generateEntQuery(qry);
	final EntQuerySourcesModel exp = new EntQuerySourcesModel(new EntQuerySourceAsEntity(TgVehicle.class, "v"), others);
	assertEquals("models are different", exp, act.getSources());

	final List<CompoundConditionModel> others2 = new ArrayList<CompoundConditionModel>();
	others2.add(new CompoundConditionModel(LogicalOperator.AND, new ComparisonTestModel(new EntProp("eqClass"), ComparisonOperator.LT, new EntProp("limit"))));
	final ConditionsModel exp2 = new ConditionsModel(new ComparisonTestModel(new DayOfModel(new EntProp("initDate")), ComparisonOperator.GT, new EntValue(15)), others2);
	assertEquals("models are different", exp2, act.getConditions());
    }

    @Test
    public void test_query_with_derived_sources() {
	final EntityResultQueryModel<TgVehicle> subQry = query.select(TgVehicle.class).as("v").where().prop("v.model").isNotNull().model();
	final EntityResultQueryModel<TgVehicle> qry = query.select(subQry).as("v").where().prop("v.model").isNotNull().model();

	final EntQuerySourcesModel exp = new EntQuerySourcesModel(new EntQuerySourceAsModel("v", qb.generateEntQuery(subQry)), new ArrayList<EntQueryCompoundSourceModel>());
	assertEquals("models are different", exp, qb.generateEntQuery(qry).getSources());
    }


    @Test
    public void test_like() {
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).where().prop("model.desc").like().val("MERC%").model();
	final ConditionsModel exp = new ConditionsModel(new LikeTestModel(new EntProp("model.desc"), new EntValue("MERC%"), false, false), new ArrayList<CompoundConditionModel>());
	assertEquals("models are different", exp, qb.generateEntQuery(qry).getConditions());
    }

    @Test
    public void test_notLike() {
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).where().prop("model.desc").notLike().val("MERC%").model();
	final ConditionsModel exp = new ConditionsModel(new LikeTestModel(new EntProp("model.desc"), new EntValue("MERC%"), true, false), new ArrayList<CompoundConditionModel>());
	assertEquals("models are different", exp, qb.generateEntQuery(qry).getConditions());
    }

    @Test
    public void test_multiple_vs_single_comparison_test() {
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).where().anyOfProps("model", "eqClass").eq().val("MERC").model();
	final List<CompoundConditionModel> others = new ArrayList<CompoundConditionModel>();
	others.add(new CompoundConditionModel(LogicalOperator.OR, new ComparisonTestModel(new EntProp("eqClass"), ComparisonOperator.EQ, new EntValue("MERC"))));
	final GroupedConditionsModel exp = new GroupedConditionsModel(false, new ComparisonTestModel(new EntProp("model"), ComparisonOperator.EQ, new EntValue("MERC")), others);
	final List<CompoundConditionModel> others2 = new ArrayList<CompoundConditionModel>();
	final ConditionsModel exp2 = new ConditionsModel(exp, others2);
	assertEquals("models are different", exp2, qb.generateEntQuery(qry).getConditions());
    }

    @Test
    public void test_multiple_vs_single_comparison_test_with_only_one_operand() {
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).where().anyOfProps("model").eq().val("MERC").model();
	final List<CompoundConditionModel> others = new ArrayList<CompoundConditionModel>();
	final GroupedConditionsModel exp = new GroupedConditionsModel(false, new ComparisonTestModel(new EntProp("model"), ComparisonOperator.EQ, new EntValue("MERC")), others);
	final List<CompoundConditionModel> others2 = new ArrayList<CompoundConditionModel>();
	final ConditionsModel exp2 = new ConditionsModel(exp, others2);
	assertEquals("models are different", exp2, qb.generateEntQuery(qry).getConditions());
    }

    @Test
    public void test_single_vs_multiple_comparison_test() {
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).where().prop("model").eq().anyOfValues("MERC", "AUDI").model();
	final List<CompoundConditionModel> others = new ArrayList<CompoundConditionModel>();
	others.add(new CompoundConditionModel(LogicalOperator.OR, new ComparisonTestModel(new EntProp("model"), ComparisonOperator.EQ, new EntValue("AUDI"))));
	final GroupedConditionsModel exp = new GroupedConditionsModel(false, new ComparisonTestModel(new EntProp("model"), ComparisonOperator.EQ, new EntValue("MERC")), others);
	final List<CompoundConditionModel> others2 = new ArrayList<CompoundConditionModel>();
	final ConditionsModel exp2 = new ConditionsModel(exp, others2);
	assertEquals("models are different", exp2, qb.generateEntQuery(qry).getConditions());
    }

    @Test
    public void test_single_vs_multiple_comparison_test_with_only_one_operand() {
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).where().prop("model").eq().anyOfValues("MERC").model();
	final List<CompoundConditionModel> others = new ArrayList<CompoundConditionModel>();
	final GroupedConditionsModel exp = new GroupedConditionsModel(false, new ComparisonTestModel(new EntProp("model"), ComparisonOperator.EQ, new EntValue("MERC")), others);
	final List<CompoundConditionModel> others2 = new ArrayList<CompoundConditionModel>();
	final ConditionsModel exp2 = new ConditionsModel(exp, others2);
	assertEquals("models are different", exp2, qb.generateEntQuery(qry).getConditions());
    }

    @Test
    public void test_set_test_with_values() {
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).where().prop("model").in().values("MERC", "AUDI").model();
	final ConditionsModel exp2 = new ConditionsModel(new SetTestModel(new EntProp("model"), false, new EntSet(Arrays.asList(new ISingleOperand[]{new EntValue("MERC"), new EntValue("AUDI")}))), new ArrayList<CompoundConditionModel>());
	assertEquals("models are different", exp2, qb.generateEntQuery(qry).getConditions());
    }

    @Test
    public void test_set_test_with_values_and_multiple_operand() {
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).where().anyOfProps("station", "currStation").in().values("sta1", "sta2").model();

	final List<CompoundConditionModel> others = new ArrayList<CompoundConditionModel>();
	others.add(new CompoundConditionModel(LogicalOperator.OR, new SetTestModel(new EntProp("currStation"), false, new EntSet(Arrays.asList(new ISingleOperand[]{new EntValue("sta1"), new EntValue("sta2")})))));
	final GroupedConditionsModel exp = new GroupedConditionsModel(false, new SetTestModel(new EntProp("station"), false, new EntSet(Arrays.asList(new ISingleOperand[]{new EntValue("sta1"), new EntValue("sta2")}))), others);
	final List<CompoundConditionModel> others2 = new ArrayList<CompoundConditionModel>();
	final ConditionsModel exp2 = new ConditionsModel(exp, others2);
	assertEquals("models are different", exp2, qb.generateEntQuery(qry).getConditions());
    }

    @Test
    public void test_set_test_with_query() {
	final EntityResultQueryModel<TgVehicleModel> vehModels = query.select(TgVehicleModel.class).model();
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).where().prop("model").in().model(vehModels).model();
	final ConditionsModel exp2 = new ConditionsModel(new SetTestModel(new EntProp("model"), false, new EntSetFromQryModel(qb.generateEntQuery(vehModels))), new ArrayList<CompoundConditionModel>());
	assertEquals("models are different", exp2, qb.generateEntQuery(qry).getConditions());
    }

    @Test
    public void test_plain_quantified_test() {
	final EntityResultQueryModel<TgVehicleModel> vehModels = query.select(TgVehicleModel.class).model();
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).where().prop("model").eq().any(vehModels).model();
	final ConditionsModel exp2 = new ConditionsModel(new QuantifiedTestModel(new EntProp("model"), ComparisonOperator.EQ, Quantifier.ANY, qb.generateEntQuery(vehModels)), new ArrayList<CompoundConditionModel>());
	assertEquals("models are different", exp2, qb.generateEntQuery(qry).getConditions());
    }

    @Test
    public void test_multiple_quantified_test() {
	final EntityResultQueryModel<TgVehicleModel> vehModels = query.select(TgVehicleModel.class).model();
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).where().anyOfProps("model", "eqClass").eq().any(vehModels).model();
	final List<CompoundConditionModel> others = new ArrayList<CompoundConditionModel>();
	others.add(new CompoundConditionModel(LogicalOperator.OR, new QuantifiedTestModel(new EntProp("eqClass"), ComparisonOperator.EQ, Quantifier.ANY, qb.generateEntQuery(vehModels))));
	final GroupedConditionsModel exp = new GroupedConditionsModel(false, new QuantifiedTestModel(new EntProp("model"), ComparisonOperator.EQ, Quantifier.ANY, qb.generateEntQuery(vehModels)), others);
	final List<CompoundConditionModel> others2 = new ArrayList<CompoundConditionModel>();
	final ConditionsModel exp2 = new ConditionsModel(exp, others2);
	assertEquals("models are different", exp2, qb.generateEntQuery(qry).getConditions());
    }

    @Test
    public void test_simple_query_model_01() {
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).as("v").where().prop("model").isNotNull().and().prop("eqclass").isNull().model();
	final List<CompoundConditionModel> others = new ArrayList<CompoundConditionModel>();
	others.add(new CompoundConditionModel(LogicalOperator.AND, new NullTestModel(new EntProp("eqclass"), false)));
	final ConditionsModel exp = new ConditionsModel(new NullTestModel(new EntProp("model"), true), others);
	assertEquals("models are different", exp, qb.generateEntQuery(qry).getConditions());
    }

    @Test
    public void test_simple_query_model_02() {
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).as("v").where().prop("model").gt().val(100).and().prop("eqClass").lt().prop("limit").model();
	final List<CompoundConditionModel> others = new ArrayList<CompoundConditionModel>();
	others.add(new CompoundConditionModel(LogicalOperator.AND, new ComparisonTestModel(new EntProp("eqClass"), ComparisonOperator.LT, new EntProp("limit"))));
	final ConditionsModel exp = new ConditionsModel(new ComparisonTestModel(new EntProp("model"), ComparisonOperator.GT, new EntValue(100)), others);
	assertEquals("models are different", exp, qb.generateEntQuery(qry).getConditions());
    }

    @Test
    public void test_simple_query_model_03() {
	final Map<String, Object> paramValues = new HashMap<String, Object>();
	paramValues.put("eqclass", "class1");

	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).as("v").where().prop("model").isNotNull().and().param("eqclass").isNull().model();
	final List<CompoundConditionModel> others = new ArrayList<CompoundConditionModel>();
	others.add(new CompoundConditionModel(LogicalOperator.AND, new NullTestModel(new EntValue("class1"), false)));
	final ConditionsModel exp = new ConditionsModel(new NullTestModel(new EntProp("model"), true), others);
	assertEquals("models are different", exp, qb.generateEntQuery(qry, paramValues).getConditions());
    }

    @Test
    public void test_simple_query_model_04() {
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).as("v").where().begin().prop("model").isNotNull().and().prop("eqclass").isNull().end().model();
	final List<CompoundConditionModel> others = new ArrayList<CompoundConditionModel>();
	others.add(new CompoundConditionModel(LogicalOperator.AND, new NullTestModel(new EntProp("eqclass"), false)));
	final GroupedConditionsModel exp = new GroupedConditionsModel(false, new NullTestModel(new EntProp("model"), true), others);
	final ConditionsModel exp2 = new ConditionsModel(exp, new ArrayList<CompoundConditionModel>());
	assertEquals("models are different", exp2, qb.generateEntQuery(qry).getConditions());
    }

    @Test
    public void test_simple_query_model_05() {
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).as("v").where().begin().prop("model").isNotNull().and().prop("eqclass").isNull().end().and().prop("currStation").isNotNull().model();
	final List<CompoundConditionModel> others = new ArrayList<CompoundConditionModel>();
	others.add(new CompoundConditionModel(LogicalOperator.AND, new NullTestModel(new EntProp("eqclass"), false)));
	final GroupedConditionsModel exp = new GroupedConditionsModel(false, new NullTestModel(new EntProp("model"), true), others);
	final List<CompoundConditionModel> others2 = new ArrayList<CompoundConditionModel>();
	others2.add(new CompoundConditionModel(LogicalOperator.AND, new NullTestModel(new EntProp("currStation"), true)));
	final ConditionsModel exp2 = new ConditionsModel(exp, others2);
	assertEquals("models are different", exp2, qb.generateEntQuery(qry).getConditions());
    }

    @Test
    public void test_simple_query_model_06() {
	final EntityResultQueryModel<TgVehicle> subQry = query.select(TgVehicle.class).model();
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).as("v").where().prop("model").isNotNull().and().exists(subQry).model();
	final List<CompoundConditionModel> others = new ArrayList<CompoundConditionModel>();
	others.add(new CompoundConditionModel(LogicalOperator.AND, new ExistenceTestModel(false, qb.generateEntQuery(subQry))));
	final ConditionsModel exp = new ConditionsModel(new NullTestModel(new EntProp("model"), true), others);
	assertEquals("models are different", exp, qb.generateEntQuery(qry).getConditions());
    }

    @Test
    public void test_simple_query_model_07() {
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).as("v").where().anyOfProps("model", "eqClass").isNotNull().model();
	final List<CompoundConditionModel> others = new ArrayList<CompoundConditionModel>();
	others.add(new CompoundConditionModel(LogicalOperator.OR, new NullTestModel(new EntProp("eqClass"), true)));
	final GroupedConditionsModel exp = new GroupedConditionsModel(false, new NullTestModel(new EntProp("model"), true), others);
	final List<CompoundConditionModel> others2 = new ArrayList<CompoundConditionModel>();
	final ConditionsModel exp2 = new ConditionsModel(exp, others2);
	assertEquals("models are different", exp2, qb.generateEntQuery(qry).getConditions());
    }

    @Test
    public void test_simple_query_model_08() {
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).as("v").where().beginExpr().prop("model").add().prop("eqClass").endExpr().isNull().model();
	final List<CompoundConditionModel> others = new ArrayList<CompoundConditionModel>();
	final List<CompoundSingleOperand> compSingleOperands = new ArrayList<CompoundSingleOperand>();
	compSingleOperands.add(new CompoundSingleOperand(new EntProp("eqClass"), ArithmeticalOperator.ADD));
	final Expression expression = new Expression(new EntProp("model"), compSingleOperands);
	final ConditionsModel exp = new ConditionsModel(new NullTestModel(expression, false), others);
	assertEquals("models are different", exp, qb.generateEntQuery(qry).getConditions());
    }

    @Test
    public void test_simple_query_model_09() {
	final ExpressionModel expr = query.expr().prop("model").add().prop("eqClass").model();
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).as("v").where().expr(expr).isNull().model();
	final List<CompoundConditionModel> others = new ArrayList<CompoundConditionModel>();
	final List<CompoundSingleOperand> compSingleOperands = new ArrayList<CompoundSingleOperand>();
	compSingleOperands.add(new CompoundSingleOperand(new EntProp("eqClass"), ArithmeticalOperator.ADD));
	final Expression expression = new Expression(new EntProp("model"), compSingleOperands);
	final ConditionsModel exp = new ConditionsModel(new NullTestModel(expression, false), others);
	assertEquals("models are different", exp, qb.generateEntQuery(qry).getConditions());
    }

    @Test
    public void test_simple_query_model_10() {
	final ExpressionModel expr0 = query.expr().prop("model").mult().prop("eqClass").model();
	final ExpressionModel expr = query.expr().prop("model").add().expr(expr0).model();
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).as("v").where().expr(expr).isNull().model();
	final List<CompoundConditionModel> others = new ArrayList<CompoundConditionModel>();

	final List<CompoundSingleOperand> compSingleOperands0 = new ArrayList<CompoundSingleOperand>();
	compSingleOperands0.add(new CompoundSingleOperand(new EntProp("eqClass"), ArithmeticalOperator.MULT));
	final Expression expression0 = new Expression(new EntProp("model"), compSingleOperands0);


	final List<CompoundSingleOperand> compSingleOperands = new ArrayList<CompoundSingleOperand>();
	compSingleOperands.add(new CompoundSingleOperand(expression0, ArithmeticalOperator.ADD));
	final Expression expression = new Expression(new EntProp("model"), compSingleOperands);
	final ConditionsModel exp = new ConditionsModel(new NullTestModel(expression, false), others);
	assertEquals("models are different", exp, qb.generateEntQuery(qry).getConditions());
    }

    @Test
    public void test_simple_query_model_11() {
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).as("v").where().dayOf().prop("initDate").gt().val(15).and().prop("eqClass").lt().prop("limit").model();
	final List<CompoundConditionModel> others = new ArrayList<CompoundConditionModel>();
	others.add(new CompoundConditionModel(LogicalOperator.AND, new ComparisonTestModel(new EntProp("eqClass"), ComparisonOperator.LT, new EntProp("limit"))));
	final ConditionsModel exp = new ConditionsModel(new ComparisonTestModel(new DayOfModel(new EntProp("initDate")), ComparisonOperator.GT, new EntValue(15)), others);
	assertEquals("models are different", exp, qb.generateEntQuery(qry).getConditions());
    }

    @Test
    public void test_simple_query_model_12() {
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).as("v").where().monthOf().prop("initDate").gt().val(3).and().prop("eqClass").lt().prop("limit").model();
	final List<CompoundConditionModel> others = new ArrayList<CompoundConditionModel>();
	others.add(new CompoundConditionModel(LogicalOperator.AND, new ComparisonTestModel(new EntProp("eqClass"), ComparisonOperator.LT, new EntProp("limit"))));
	final ConditionsModel exp = new ConditionsModel(new ComparisonTestModel(new MonthOfModel(new EntProp("initDate")), ComparisonOperator.GT, new EntValue(3)), others);
	assertEquals("models are different", exp, qb.generateEntQuery(qry).getConditions());
    }

    @Test
    public void test_simple_query_model_13() {
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).as("v").model();
	final List<EntQueryCompoundSourceModel> others = new ArrayList<EntQueryCompoundSourceModel>();
	final EntQuerySourcesModel exp = new EntQuerySourcesModel(new EntQuerySourceAsEntity(TgVehicle.class, "v"), others);
	assertEquals("models are different", exp, qb.generateEntQuery(qry).getSources());
    }

    @Test
    public void test_simple_query_model_14() {
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).model();
	final List<EntQueryCompoundSourceModel> others = new ArrayList<EntQueryCompoundSourceModel>();
	final EntQuerySourcesModel exp = new EntQuerySourcesModel(new EntQuerySourceAsEntity(TgVehicle.class, null), others);
	assertEquals("models are different", exp, qb.generateEntQuery(qry).getSources());
    }

    @Test
    public void test_simple_query_model_18() {
	final EntityResultQueryModel<TgWorkOrder> qry = query.select(TgVehicle.class).yield().prop("eqClass").modelAsEntity(TgWorkOrder.class);
	final List<YieldModel> yields = new ArrayList<YieldModel>();
	yields.add(new YieldModel(new EntProp("eqClass"), "id"));
	final YieldsModel exp = new YieldsModel(yields);
	assertEquals("models are different", exp, qb.generateEntQuery(qry).getYields());
    }

    @Test
    public void test_simple_query_model_19() {
	final EntityResultQueryModel<TgWorkOrder> qry = query.select(TgVehicle.class).yield().prop("eqClass").as("ec").yield().prop("model").as("m").modelAsEntity(TgWorkOrder.class);
	final List<YieldModel> yields = new ArrayList<YieldModel>();
	yields.add(new YieldModel(new EntProp("eqClass"), "ec"));
	yields.add(new YieldModel(new EntProp("model"), "m"));
	final YieldsModel exp = new YieldsModel(yields);
	assertEquals("models are different", exp, qb.generateEntQuery(qry).getYields());
    }

    @Test
    public void test_simple_query_model_20() {
	final Map<String, Object> paramValues = new HashMap<String, Object>();
	paramValues.put("param", 20);
	final EntityResultQueryModel<TgWorkOrder> qry = query.select(TgVehicle.class).yield().prop("eqClass").as("ec").yield().beginExpr().prop("model").add().param("param").endExpr().as("m").modelAsEntity(TgWorkOrder.class);
	final List<YieldModel> yields = new ArrayList<YieldModel>();
	yields.add(new YieldModel(new EntProp("eqClass"), "ec"));
	final List<CompoundSingleOperand> compSingleOperands = new ArrayList<CompoundSingleOperand>();
	compSingleOperands.add(new CompoundSingleOperand(new EntValue(20), ArithmeticalOperator.ADD));
	final Expression expression = new Expression(new EntProp("model"), compSingleOperands);
	yields.add(new YieldModel(expression, "m"));
	final YieldsModel exp = new YieldsModel(yields);
	assertEquals("models are different", exp, qb.generateEntQuery(qry, paramValues).getYields());
    }

    @Test
    public void test_simple_query_model_21() {
	final EntityResultQueryModel<TgWorkOrder> qry = query.select(TgVehicle.class).groupBy().prop("eqClass").yield().prop("eqClass").modelAsEntity(TgWorkOrder.class);
	final EntQuery act = qb.generateEntQuery(qry);

	final List<YieldModel> yields = new ArrayList<YieldModel>();
	yields.add(new YieldModel(new EntProp("eqClass"), "id"));
	final YieldsModel exp = new YieldsModel(yields);

	assertEquals("models are different", exp, act.getYields());
	final List<GroupModel> groups = new ArrayList<GroupModel>();
	groups.add(new GroupModel(new EntProp("eqClass")));
	final GroupsModel exp2 = new GroupsModel(groups);
	assertEquals("models are different", exp2, act.getGroups());
    }

    @Test
    public void test_simple_query_model_22() {
	final EntityResultQueryModel<TgVehicle> subQry1 = query.select(TgVehicle.class).model();
	final EntityResultQueryModel<TgWorkOrder> subQry2 = query.select(TgWorkOrder.class).model();
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).as("v").where().existsAnyOf(subQry1, subQry2).model();
	final List<CompoundConditionModel> others = new ArrayList<CompoundConditionModel>();
	others.add(new CompoundConditionModel(LogicalOperator.OR, new ExistenceTestModel(false, qb.generateEntQuery(subQry2))));
	final GroupedConditionsModel exp = new GroupedConditionsModel(false, new ExistenceTestModel(false, qb.generateEntQuery(subQry1)), others);
	final List<CompoundConditionModel> others2 = new ArrayList<CompoundConditionModel>();
	final ConditionsModel exp2 = new ConditionsModel(exp, others2);
	assertEquals("models are different", exp2, qb.generateEntQuery(qry).getConditions());
    }

    @Test
    public void test_prop_names_grouping_by_source_name() {
	final QueryModelEnhancer qme = new QueryModelEnhancer();
    }

    @Test
    public void test_expressions1() {
	final Map<String, Object> paramValues = new HashMap<String, Object>();
	paramValues.put("costMultiplier", 1);
	paramValues.put("costDivider", 2);

	final EntityResultQueryModel<TgWorkOrder> qry =  query.select(TgWorkOrder.class).as("wo").where().beginExpr().beginExpr().beginExpr().beginExpr().prop("wo.actCost.amount").mult().param("costMultiplier").endExpr().add().prop("wo.estCost.amount").div().param("costDivider").endExpr().add().prop("wo.yearlyCost.amount").endExpr().div().val(12).endExpr().gt().val(1000).model();
	// ((((wo.actCost.amount * :costMultiplier) + wo.estCost.amount / :costDivider) + wo.yearlyCost.amount) / 12 )
	final Expression expressionModel1 = new Expression(new EntProp("wo.actCost.amount"), Arrays.asList(new CompoundSingleOperand[]{new CompoundSingleOperand(new EntValue(1), ArithmeticalOperator.MULT)}));
	final Expression expressionModel2 = new Expression(expressionModel1, Arrays.asList(new CompoundSingleOperand[]{new CompoundSingleOperand(new EntProp("wo.estCost.amount"), ArithmeticalOperator.ADD), new CompoundSingleOperand(new EntValue(2), ArithmeticalOperator.DIV)}));
	final Expression expressionModel3 = new Expression(expressionModel2, Arrays.asList(new CompoundSingleOperand[]{new CompoundSingleOperand(new EntProp("wo.yearlyCost.amount"), ArithmeticalOperator.ADD)}));
	final Expression expressionModel = new Expression(expressionModel3, Arrays.asList(new CompoundSingleOperand[]{new CompoundSingleOperand(new EntValue(12), ArithmeticalOperator.DIV)}));

	final ConditionsModel exp = new ConditionsModel(new ComparisonTestModel(expressionModel, ComparisonOperator.GT, new EntValue(1000)), new ArrayList<CompoundConditionModel>());
	assertEquals("models are different", exp, qb.generateEntQuery(qry, paramValues).getConditions());
    }

    @Test
    public void test_simple_query_model_22a() {
	final EntityResultQueryModel<TgVehicle> subQry0 = query.select(TgVehicle.class).where().val(1).isNotNull().model();
	final EntityResultQueryModel<TgVehicle> subQry1 = query.select(TgVehicle.class).where().exists(subQry0).model();
	final EntityResultQueryModel<TgWorkOrder> subQry2 = query.select(TgWorkOrder.class).model();
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).as("v").where().existsAnyOf(subQry1, subQry2).model();
	final List<EntQuery> exp = Arrays.asList(new EntQuery[]{qb.generateEntQuery(subQry0), qb.generateEntQuery(subQry2)});
	assertEquals("models are different", exp, qb.generateEntQuery(qry).getLeafSubqueries());
    }
}