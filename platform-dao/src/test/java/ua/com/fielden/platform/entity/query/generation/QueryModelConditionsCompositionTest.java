package ua.com.fielden.platform.entity.query.generation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.fluent.ArithmeticalOperator;
import ua.com.fielden.platform.entity.query.fluent.ComparisonOperator;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere0;
import ua.com.fielden.platform.entity.query.fluent.JoinType;
import ua.com.fielden.platform.entity.query.fluent.LogicalOperator;
import ua.com.fielden.platform.entity.query.generation.elements.ComparisonTestModel;
import ua.com.fielden.platform.entity.query.generation.elements.CompoundConditionModel;
import ua.com.fielden.platform.entity.query.generation.elements.CompoundSingleOperand;
import ua.com.fielden.platform.entity.query.generation.elements.ConditionsModel;
import ua.com.fielden.platform.entity.query.generation.elements.DayOfModel;
import ua.com.fielden.platform.entity.query.generation.elements.EntQuery;
import ua.com.fielden.platform.entity.query.generation.elements.EntQueryCompoundSourceModel;
import ua.com.fielden.platform.entity.query.generation.elements.EntQuerySourceAsEntity;
import ua.com.fielden.platform.entity.query.generation.elements.EntQuerySourcesModel;
import ua.com.fielden.platform.entity.query.generation.elements.EntSetFromQryModel;
import ua.com.fielden.platform.entity.query.generation.elements.ExistenceTestModel;
import ua.com.fielden.platform.entity.query.generation.elements.Expression;
import ua.com.fielden.platform.entity.query.generation.elements.GroupedConditionsModel;
import ua.com.fielden.platform.entity.query.generation.elements.ICondition;
import ua.com.fielden.platform.entity.query.generation.elements.LikeTestModel;
import ua.com.fielden.platform.entity.query.generation.elements.MonthOfModel;
import ua.com.fielden.platform.entity.query.generation.elements.NullTestModel;
import ua.com.fielden.platform.entity.query.generation.elements.QuantifiedTestModel;
import ua.com.fielden.platform.entity.query.generation.elements.Quantifier;
import ua.com.fielden.platform.entity.query.generation.elements.SetTestModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.sample.domain.TgWorkOrder;
import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.query.fluent.query.expr;
import static ua.com.fielden.platform.entity.query.fluent.query.select;

public class QueryModelConditionsCompositionTest extends BaseEntQueryTCase {
    private final String mercLike = "MERC%";
    private final String merc = "MERC";
    private final String audi = "AUDI";
    private final static String message = "Condition models are different!";
    private final IWhere0 base = select(VEHICLE).where();

    private final static ComparisonOperator _eq = ComparisonOperator.EQ;
    private final static ComparisonOperator _gt = ComparisonOperator.GT;
    private final static ComparisonOperator _lt = ComparisonOperator.LT;
    private final static LogicalOperator _and = LogicalOperator.AND;
    private final static LogicalOperator _or = LogicalOperator.OR;
    private final static ArithmeticalOperator _mult = ArithmeticalOperator.MULT;

    private static GroupedConditionsModel group(final boolean negation, final ICondition firstCondition, final CompoundConditionModel... otherConditions) {
	return new GroupedConditionsModel(negation, firstCondition, Arrays.asList(otherConditions));
    }

    private static CompoundConditionModel compound(final LogicalOperator operator, final ICondition condition) {
	return new CompoundConditionModel(operator, condition);
    }

    private static ConditionsModel conditions(final ICompoundCondition0 condition) {
	return entQry(condition.model()).getConditions();
    }

    private static void assertModelsEquals(final ConditionsModel exp, final ConditionsModel act) {
	assertEquals((message + " exp: " + exp.toString() + " act: " + act.toString()), exp, act);
    }

    private static ConditionsModel conditions(final ICompoundCondition0 condition, final Map<String, Object> paramValues) {
	return entQry(condition.model(), paramValues).getConditions();
    }

    private static ConditionsModel conditions(final ICondition firstCondition, final CompoundConditionModel... otherConditions) {
	return new ConditionsModel(firstCondition, Arrays.asList(otherConditions));
    }

    @Test
    public void test_like() {
	assertModelsEquals( //
		conditions(new LikeTestModel(prop("model.desc"), val(mercLike), false, false)), //
		conditions(base.prop("model.desc").like().val(mercLike)));
    }

    @Test
    public void test_notLike() {
	assertModelsEquals( //
		conditions(new LikeTestModel(prop("model.desc"), val(mercLike), true, false)), //
		conditions(base.prop("model.desc").notLike().val(mercLike)));
    }

    @Test
    public void test_set_test_with_values() {
	assertModelsEquals(//
		conditions(new SetTestModel(prop("model"), false, set(val(merc), val(audi)))), //
		conditions(base.prop("model").in().values(merc, audi)));
    }

    @Test
    public void test_set_test_with_query() {
	assertModelsEquals( //
		conditions(new SetTestModel(prop("model"), false, new EntSetFromQryModel(entSubQry(select(MODEL).model())))), //
		conditions(base.prop("model").in().model(select(MODEL).model())));
    }

    @Test
    public void test_plain_quantified_test() {
	final EntityResultQueryModel<TgVehicleModel> vehModels = select(MODEL).model();
	assertModelsEquals( //
		conditions(new QuantifiedTestModel(prop("model"), _eq, Quantifier.ANY, entSubQry(vehModels))), //
		conditions(base.prop("model").eq().any(vehModels)));
    }

    @Test
    public void test_simple_query_model_01() {
	assertModelsEquals( //
		conditions(new NullTestModel(prop("model"), true), //
			compound(_and, new NullTestModel(prop("station"), false))), //
		conditions(base.prop("model").isNotNull().and().prop("station").isNull()));
    }

    @Test
    public void test_simple_query_model_02() {
	assertModelsEquals(//
		conditions(new ComparisonTestModel(prop("price"), _gt, val(100)), //
			compound(_and, new ComparisonTestModel(prop("purchasePrice"), _lt, prop("price")))), //
		conditions(base.prop("price").gt().val(100).and().prop("purchasePrice").lt().prop("price")));
    }

    @Test
    public void test_simple_query_model_03() {
	final Map<String, Object> paramValues = new HashMap<String, Object>();
	paramValues.put("eqclass", "CL1");

	assertModelsEquals(//
		conditions(new NullTestModel(prop("model"), true), //
			compound(_and, new NullTestModel(val("CL1"), false))), //
		conditions(base.prop("model").isNotNull().and().param("eqclass").isNull(), paramValues));
    }

    @Test
    public void test_simple_query_model_04() {
	assertModelsEquals(//
		conditions(group(false, //
			new NullTestModel(prop("model"), true), //
			compound(_and, new NullTestModel(prop("station"), false)))), //
		conditions(base.begin().prop("model").isNotNull().and().prop("station").isNull().end()));
    }

    @Test
    public void test_simple_query_model_05() {
	assertModelsEquals( //
	conditions(group(false, //
		new NullTestModel(prop("model"), true), //
		compound(_and, new NullTestModel(prop("station"), false))), //
		compound(_and, new NullTestModel(prop("price"), true))), //
		conditions(base.begin().prop("model").isNotNull().and().prop("station").isNull().end().and().prop("price").isNotNull()));
    }

    @Test
    public void test_simple_query_model_06() {
	final EntityResultQueryModel<TgVehicle> subQry = select(VEHICLE).model();
	final List<CompoundConditionModel> others = new ArrayList<CompoundConditionModel>();
	others.add(new CompoundConditionModel(_and, new ExistenceTestModel(false, entSubQry(subQry))));
	final ConditionsModel exp = new ConditionsModel(new NullTestModel(prop("model"), true), others);
	assertModelsEquals(//
	exp, //
		conditions(base.prop("model").isNotNull().and().exists(subQry)));
    }

    @Test
    public void test_simple_query_model_07() {
	final List<CompoundConditionModel> others = new ArrayList<CompoundConditionModel>();
	others.add(new CompoundConditionModel(LogicalOperator.OR, new NullTestModel(prop("station"), true)));
	final GroupedConditionsModel exp = new GroupedConditionsModel(false, new NullTestModel(prop("model"), true), others);
	final List<CompoundConditionModel> others2 = new ArrayList<CompoundConditionModel>();
	final ConditionsModel exp2 = new ConditionsModel(exp, others2);
	assertModelsEquals(//
	exp2, //
		conditions(base.anyOfProps("model", "station").isNotNull()));
    }

    @Test
    public void test_simple_query_model_08() {
	final List<CompoundConditionModel> others = new ArrayList<CompoundConditionModel>();
	final List<CompoundSingleOperand> compSingleOperands = new ArrayList<CompoundSingleOperand>();
	compSingleOperands.add(new CompoundSingleOperand(prop("purchasePrice"), ArithmeticalOperator.ADD));
	final Expression expression = new Expression(prop("price"), compSingleOperands);
	final ConditionsModel exp = new ConditionsModel(new NullTestModel(expression, false), others);
	assertModelsEquals(//
	exp, //
		conditions(base.beginExpr().prop("price").add().prop("purchasePrice").endExpr().isNull()));
    }

    @Test
    public void test_simple_query_model_09() {
	final ExpressionModel expr = expr().prop("price.amount").add().prop("purchasePrice.amount").model();
	final List<CompoundConditionModel> others = new ArrayList<CompoundConditionModel>();
	final List<CompoundSingleOperand> compSingleOperands = new ArrayList<CompoundSingleOperand>();
	compSingleOperands.add(new CompoundSingleOperand(prop("purchasePrice.amount"), ArithmeticalOperator.ADD));
	final Expression expression = new Expression(prop("price.amount"), compSingleOperands);
	final ConditionsModel exp = new ConditionsModel(new NullTestModel(expression, false), others);
	assertModelsEquals(//
	exp, //
		conditions(base.expr(expr).isNull()));
    }

    @Test
    public void test_simple_query_model_10() {
	final ExpressionModel expr0 = expr().prop("model").mult().prop("station").model();
	final ExpressionModel expr = expr().prop("model").add().expr(expr0).model();

	final List<CompoundConditionModel> others = new ArrayList<CompoundConditionModel>();

	final List<CompoundSingleOperand> compSingleOperands0 = new ArrayList<CompoundSingleOperand>();
	compSingleOperands0.add(new CompoundSingleOperand(prop("station"), _mult));
	final Expression expression0 = new Expression(prop("model"), compSingleOperands0);

	final List<CompoundSingleOperand> compSingleOperands = new ArrayList<CompoundSingleOperand>();
	compSingleOperands.add(new CompoundSingleOperand(expression0, ArithmeticalOperator.ADD));
	final Expression expression = new Expression(prop("model"), compSingleOperands);
	final ConditionsModel exp = new ConditionsModel(new NullTestModel(expression, false), others);
	assertModelsEquals(//
	exp, //
		conditions(base.expr(expr).isNull()));
    }

    @Test
    public void test_simple_query_model_11() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).as("v").where().dayOf().prop("initDate").gt().val(15).and().prop("price").lt().prop("purchasePrice").model();
	final List<CompoundConditionModel> others = new ArrayList<CompoundConditionModel>();
	others.add(new CompoundConditionModel(_and, new ComparisonTestModel(prop("price"), _lt, prop("purchasePrice"))));
	final ConditionsModel exp = new ConditionsModel(new ComparisonTestModel(new DayOfModel(prop("initDate")), _gt, val(15)), others);
	assertEquals(message, exp, entQry(qry).getConditions());
    }

    @Test
    public void test_simple_query_model_12() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).as("v").where().monthOf().prop("initDate").gt().val(3).and().prop("price").lt().prop("purchasePrice").model();
	final List<CompoundConditionModel> others = new ArrayList<CompoundConditionModel>();
	others.add(new CompoundConditionModel(_and, new ComparisonTestModel(prop("price"), _lt, prop("purchasePrice"))));
	final ConditionsModel exp = new ConditionsModel(new ComparisonTestModel(new MonthOfModel(prop("initDate")), _gt, val(3)), others);
	assertEquals(message, exp, entQry(qry).getConditions());
    }

    @Test
    public void test_ignore_of_null_value_in_condition1() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).where().prop("model.desc").like().iVal(mercLike).model();
	final ConditionsModel exp = new ConditionsModel(new LikeTestModel(prop("model.desc"), val(mercLike), false, false), new ArrayList<CompoundConditionModel>());
	assertEquals(message, exp, entQry(qry).getConditions());
    }

    @Test
    public void test_ignore_of_null_value_in_condition2() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).where().prop("model.desc").like().iVal(null).model();
	final ConditionsModel exp = new ConditionsModel(alwaysTrueCondition, new ArrayList<CompoundConditionModel>());
	assertEquals(message, exp, entQry(qry).getConditions());
    }

    @Test
    public void test_ignore_of_null_value_in_condition3() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).where().prop("model.desc").like().param("param").model();
	final ConditionsModel exp = new ConditionsModel(new LikeTestModel(prop("model.desc"), val("MERC%"), false, false), new ArrayList<CompoundConditionModel>());
	final Map<String, Object> paramValues = new HashMap<String, Object>();
	paramValues.put("param", "MERC%");
	assertEquals(message, exp, entQry(qry, paramValues).getConditions());
    }

    @Test
    public void test_ignore_of_null_value_in_condition4() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).where().prop("model.desc").like().iParam("param").model();
	final ConditionsModel exp = new ConditionsModel(alwaysTrueCondition, new ArrayList<CompoundConditionModel>());
	final Map<String, Object> paramValues = new HashMap<String, Object>();
	paramValues.put("param", null);
	assertEquals(message, exp, entQry(qry, paramValues).getConditions());
    }

    @Test
    public void test_ignore_of_null_value_in_condition5() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).where().prop("model.desc").like().anyOfValues().model();
	final ConditionsModel exp = new ConditionsModel(new GroupedConditionsModel(false, alwaysTrueCondition, new ArrayList<CompoundConditionModel>()), new ArrayList<CompoundConditionModel>());
	assertEquals(message, exp, entQry(qry).getConditions());
    }

    @Test
    public void test_simple_query_model_22() {
	final EntityResultQueryModel<TgVehicle> subQry1 = select(VEHICLE).model();
	final EntityResultQueryModel<TgWorkOrder> subQry2 = select(TgWorkOrder.class).model();
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).as("v").where().existsAnyOf(subQry1, subQry2).model();
	final List<CompoundConditionModel> others = new ArrayList<CompoundConditionModel>();
	others.add(new CompoundConditionModel(LogicalOperator.OR, new ExistenceTestModel(false, entSubQry(subQry2))));
	final GroupedConditionsModel exp = new GroupedConditionsModel(false, new ExistenceTestModel(false, entSubQry(subQry1)), others);
	final List<CompoundConditionModel> others2 = new ArrayList<CompoundConditionModel>();
	final ConditionsModel exp2 = new ConditionsModel(exp, others2);
	assertEquals(message, exp2, entQry(qry).getConditions());
    }

    @Test
    public void test_expressions1() {
	final Map<String, Object> paramValues = new HashMap<String, Object>();
	paramValues.put("costMultiplier", 1);
	paramValues.put("costDivider", 2);

	final EntityResultQueryModel<TgWorkOrder> qry = select(TgWorkOrder.class).as("wo").where(). //
	beginExpr(). //
	beginExpr(). //
	beginExpr(). //
	beginExpr().prop("wo.actCost.amount").mult().param("costMultiplier"). //
	endExpr(). //
	add().prop("wo.estCost.amount").div().param("costDivider"). //
	endExpr(). //
	add().prop("wo.yearlyCost.amount"). //
	endExpr(). //
	div().val(12).endExpr(). //
	gt().val(1000).model();
	// ((((wo.actCost.amount * :costMultiplier) + wo.estCost.amount / :costDivider) + wo.yearlyCost.amount) / 12 )
	final Expression expressionModel1 = new Expression(prop("wo.actCost.amount"), Arrays.asList(new CompoundSingleOperand[] { new CompoundSingleOperand(val(1), _mult) }));
	final Expression expressionModel2 = new Expression(expressionModel1, Arrays.asList(new CompoundSingleOperand[] {
		new CompoundSingleOperand(prop("wo.estCost.amount"), ArithmeticalOperator.ADD), new CompoundSingleOperand(val(2), ArithmeticalOperator.DIV) }));
	final Expression expressionModel3 = new Expression(expressionModel2, Arrays.asList(new CompoundSingleOperand[] { new CompoundSingleOperand(prop("wo.yearlyCost.amount"), ArithmeticalOperator.ADD) }));
	final Expression expressionModel = new Expression(expressionModel3, Arrays.asList(new CompoundSingleOperand[] { new CompoundSingleOperand(val(12), ArithmeticalOperator.DIV) }));

	final ConditionsModel exp = new ConditionsModel(new ComparisonTestModel(expressionModel, _gt, val(1000)), new ArrayList<CompoundConditionModel>());
	assertEquals(message, exp, entQry(qry, paramValues).getConditions());
    }

    @Test
    public void test_query_sources3() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).as("v").join(TgWorkOrder.class).as("wo").on().prop("v").eq().prop("wo.vehicle").leftJoin(TgWorkOrder.class).as("wo2").on().prop("v").eq().prop("wo2.vehicle"). //
	where().dayOf().prop("initDate").gt().val(15).and().prop("price").lt().prop("purchasePrice").model();

	final ConditionsModel condition1 = new ConditionsModel(new ComparisonTestModel(prop("v"), _eq, prop("wo.vehicle")), new ArrayList<CompoundConditionModel>());
	final ConditionsModel condition2 = new ConditionsModel(new ComparisonTestModel(prop("v"), _eq, prop("wo2.vehicle")), new ArrayList<CompoundConditionModel>());

	final List<EntQueryCompoundSourceModel> others = new ArrayList<EntQueryCompoundSourceModel>();
	others.add(new EntQueryCompoundSourceModel(new EntQuerySourceAsEntity(TgWorkOrder.class, "wo"), JoinType.IJ, condition1));
	others.add(new EntQueryCompoundSourceModel(new EntQuerySourceAsEntity(TgWorkOrder.class, "wo2"), JoinType.LJ, condition2));

	final EntQuery act = entQry(qry);
	final EntQuerySourcesModel exp = new EntQuerySourcesModel(new EntQuerySourceAsEntity(VEHICLE, "v"), others);
	assertEquals("models are different", exp, act.getSources());

	final List<CompoundConditionModel> others2 = new ArrayList<CompoundConditionModel>();
	others2.add(new CompoundConditionModel(_and, new ComparisonTestModel(prop("price"), _lt, prop("purchasePrice"))));
	final ConditionsModel exp2 = new ConditionsModel(new ComparisonTestModel(new DayOfModel(prop("initDate")), _gt, val(15)), others2);
	assertEquals(message, exp2, act.getConditions());
    }
}