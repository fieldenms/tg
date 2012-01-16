package ua.com.fielden.platform.entity.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.fluent.query;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
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
import ua.com.fielden.platform.entity.query.model.elements.EntQuerySourcesModel;
import ua.com.fielden.platform.entity.query.model.elements.EntSet;
import ua.com.fielden.platform.entity.query.model.elements.EntSetFromQryModel;
import ua.com.fielden.platform.entity.query.model.elements.EntValue;
import ua.com.fielden.platform.entity.query.model.elements.ExistenceTestModel;
import ua.com.fielden.platform.entity.query.model.elements.Expression;
import ua.com.fielden.platform.entity.query.model.elements.GroupedConditionsModel;
import ua.com.fielden.platform.entity.query.model.elements.ISingleOperand;
import ua.com.fielden.platform.entity.query.model.elements.JoinType;
import ua.com.fielden.platform.entity.query.model.elements.LikeTestModel;
import ua.com.fielden.platform.entity.query.model.elements.LogicalOperator;
import ua.com.fielden.platform.entity.query.model.elements.MonthOfModel;
import ua.com.fielden.platform.entity.query.model.elements.NullTestModel;
import ua.com.fielden.platform.entity.query.model.elements.QuantifiedTestModel;
import ua.com.fielden.platform.entity.query.model.elements.Quantifier;
import ua.com.fielden.platform.entity.query.model.elements.SetTestModel;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.sample.domain.TgWorkOrder;
import static org.junit.Assert.assertEquals;

public class QueryModelConditionsCompositionTest extends BaseEntQueryTCase {
    private final EntValue mercLikeValue = new EntValue("MERC%");
    private final EntValue mercValue = new EntValue("MERC");
    private final EntValue audiValue = new EntValue("AUDI");

    @Test
    public void test_like() {
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).where().prop("model.desc").like().val("MERC%").model();
	final ConditionsModel exp = new ConditionsModel(new LikeTestModel(new EntProp("model.desc"), mercLikeValue, false, false), new ArrayList<CompoundConditionModel>());
	assertEquals("models are different", exp, qb.generateEntQuery(qry).getConditions());
    }

    @Test
    public void test_notLike() {
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).where().prop("model.desc").notLike().val("MERC%").model();
	final ConditionsModel exp = new ConditionsModel(new LikeTestModel(new EntProp("model.desc"), mercLikeValue, true, false), new ArrayList<CompoundConditionModel>());
	assertEquals("models are different", exp, qb.generateEntQuery(qry).getConditions());
    }

    @Test
    public void test_multiple_vs_single_comparison_test() {
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).where().anyOfProps("model", "eqClass").eq().val("MERC").model();
	final List<CompoundConditionModel> others = new ArrayList<CompoundConditionModel>();
	others.add(new CompoundConditionModel(LogicalOperator.OR, new ComparisonTestModel(new EntProp("eqClass"), ComparisonOperator.EQ, mercValue)));
	final GroupedConditionsModel exp = new GroupedConditionsModel(false, new ComparisonTestModel(new EntProp("model"), ComparisonOperator.EQ, mercValue), others);
	final List<CompoundConditionModel> others2 = new ArrayList<CompoundConditionModel>();
	final ConditionsModel exp2 = new ConditionsModel(exp, others2);
	assertEquals("models are different", exp2, qb.generateEntQuery(qry).getConditions());
    }

    @Test
    public void test_multiple_vs_single_comparison_test_with_only_one_operand() {
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).where().anyOfProps("model").eq().val("MERC").model();
	final List<CompoundConditionModel> others = new ArrayList<CompoundConditionModel>();
	final GroupedConditionsModel exp = new GroupedConditionsModel(false, new ComparisonTestModel(new EntProp("model"), ComparisonOperator.EQ, mercValue), others);
	final List<CompoundConditionModel> others2 = new ArrayList<CompoundConditionModel>();
	final ConditionsModel exp2 = new ConditionsModel(exp, others2);
	assertEquals("models are different", exp2, qb.generateEntQuery(qry).getConditions());
    }

    @Test
    public void test_single_vs_multiple_comparison_test() {
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).where().prop("model").eq().anyOfValues("MERC", "AUDI").model();
	final List<CompoundConditionModel> others = new ArrayList<CompoundConditionModel>();
	others.add(new CompoundConditionModel(LogicalOperator.OR, new ComparisonTestModel(new EntProp("model"), ComparisonOperator.EQ, audiValue)));
	final GroupedConditionsModel exp = new GroupedConditionsModel(false, new ComparisonTestModel(new EntProp("model"), ComparisonOperator.EQ, mercValue), others);
	final List<CompoundConditionModel> others2 = new ArrayList<CompoundConditionModel>();
	final ConditionsModel exp2 = new ConditionsModel(exp, others2);
	assertEquals("models are different", exp2, qb.generateEntQuery(qry).getConditions());
    }

    @Test
    public void test_single_vs_multiple_comparison_test_with_only_one_operand() {
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).where().prop("model").eq().anyOfValues("MERC").model();
	final List<CompoundConditionModel> others = new ArrayList<CompoundConditionModel>();
	final GroupedConditionsModel exp = new GroupedConditionsModel(false, new ComparisonTestModel(new EntProp("model"), ComparisonOperator.EQ, mercValue), others);
	final List<CompoundConditionModel> others2 = new ArrayList<CompoundConditionModel>();
	final ConditionsModel exp2 = new ConditionsModel(exp, others2);
	assertEquals("models are different", exp2, qb.generateEntQuery(qry).getConditions());
    }

    @Test
    public void test_set_test_with_values() {
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).where().prop("model").in().values("MERC", "AUDI").model();
	final ConditionsModel exp2 = new ConditionsModel(new SetTestModel(new EntProp("model"), false, new EntSet(Arrays.asList(new ISingleOperand[]{mercValue, audiValue}))), new ArrayList<CompoundConditionModel>());
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
    public void test_ignore_of_null_value_in_condition1() {
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).where().prop("model.desc").like().iVal("MERC%").model();
	final ConditionsModel exp = new ConditionsModel(new LikeTestModel(new EntProp("model.desc"), new EntValue("MERC%"), false, false), new ArrayList<CompoundConditionModel>());
	assertEquals("models are different", exp, qb.generateEntQuery(qry).getConditions());
    }

    @Test
    public void test_ignore_of_null_value_in_condition2() {
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).where().prop("model.desc").like().iVal(null).model();
	final ConditionsModel exp = new ConditionsModel(alwaysTrueCondition, new ArrayList<CompoundConditionModel>());
	assertEquals("models are different", exp, qb.generateEntQuery(qry).getConditions());
    }

    @Test
    public void test_ignore_of_null_value_in_condition3() {
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).where().prop("model.desc").like().param("param").model();
	final ConditionsModel exp = new ConditionsModel(new LikeTestModel(new EntProp("model.desc"), new EntValue("MERC%"), false, false), new ArrayList<CompoundConditionModel>());
	final Map<String, Object> paramValues = new HashMap<String, Object>();
	paramValues.put("param", "MERC%");
	assertEquals("models are different", exp, qb.generateEntQuery(qry, paramValues).getConditions());
    }

    @Test
    public void test_ignore_of_null_value_in_condition4() {
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).where().prop("model.desc").like().iParam("param").model();
	final ConditionsModel exp = new ConditionsModel(alwaysTrueCondition, new ArrayList<CompoundConditionModel>());
	final Map<String, Object> paramValues = new HashMap<String, Object>();
	paramValues.put("param", null);
	assertEquals("models are different", exp, qb.generateEntQuery(qry, paramValues).getConditions());
    }

    @Test
    public void test_ignore_of_null_value_in_condition5() {
	final EntityResultQueryModel<TgVehicle> qry = query.select(TgVehicle.class).where().prop("model.desc").like().anyOfValues().model();
	final ConditionsModel exp = new ConditionsModel(new GroupedConditionsModel(false, alwaysTrueCondition, new ArrayList<CompoundConditionModel>()), new ArrayList<CompoundConditionModel>());
	assertEquals("models are different", exp, qb.generateEntQuery(qry).getConditions());
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
}