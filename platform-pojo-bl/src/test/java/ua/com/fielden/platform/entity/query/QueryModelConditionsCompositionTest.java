package ua.com.fielden.platform.entity.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

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
import static ua.com.fielden.platform.entity.query.fluent.query.expr;
import static ua.com.fielden.platform.entity.query.fluent.query.select;

public class QueryModelConditionsCompositionTest extends BaseEntQueryTCase {
    private final EntValue mercLikeValue = new EntValue("MERC%");
    private final EntValue mercValue = new EntValue("MERC");
    private final EntValue audiValue = new EntValue("AUDI");

    @Test
    public void test_like() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).where().prop("model.desc").like().val("MERC%").model();
	final ConditionsModel exp = new ConditionsModel(new LikeTestModel(new EntProp("model.desc"), mercLikeValue, false, false), new ArrayList<CompoundConditionModel>());
	assertEquals("models are different", exp, entQuery1(qry).getConditions());
    }

    @Test
    public void test_notLike() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).where().prop("model.desc").notLike().val("MERC%").model();
	final ConditionsModel exp = new ConditionsModel(new LikeTestModel(new EntProp("model.desc"), mercLikeValue, true, false), new ArrayList<CompoundConditionModel>());
	assertEquals("models are different", exp, entQuery1(qry).getConditions());
    }

    @Test
    public void test_multiple_vs_single_comparison_test() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).where().anyOfProps("key", "desc").eq().val("MERC").model();
	final List<CompoundConditionModel> others = new ArrayList<CompoundConditionModel>();
	others.add(new CompoundConditionModel(LogicalOperator.OR, new ComparisonTestModel(new EntProp("desc"), ComparisonOperator.EQ, mercValue)));
	final GroupedConditionsModel exp = new GroupedConditionsModel(false, new ComparisonTestModel(new EntProp("key"), ComparisonOperator.EQ, mercValue), others);
	final List<CompoundConditionModel> others2 = new ArrayList<CompoundConditionModel>();
	final ConditionsModel exp2 = new ConditionsModel(exp, others2);
	assertEquals("models are different", exp2, entQuery1(qry).getConditions());
    }

    @Test
    public void test_multiple_vs_single_comparison_test_using_equivalent_model() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).where().anyOfProps("key", "desc").eq().val("MERC").model();
	final EntityResultQueryModel<TgVehicle> qry2 = select(VEHICLE).where().begin().prop("key").eq().val("MERC").or().prop("desc").eq().val("MERC").end().model();
	assertEquals("models are different", entQuery1(qry2), entQuery1(qry));
    }

    @Test
    public void test_multiple_vs_single_like_test_using_equivalent_model() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).where().allOfProps("model.key", "model.make.key").like().anyOfValues("MERC%", "AUDI%", "BMW%").model();
	final EntityResultQueryModel<TgVehicle> qry2 = select(VEHICLE).where().begin().
		begin().prop("model.key").like().val("MERC%").or().prop("model.key").like().val("AUDI%").or().prop("model.key").like().val("BMW%").end().and().
		begin().prop("model.make.key").like().val("MERC%").or().prop("model.make.key").like().val("AUDI%").or().prop("model.make.key").like().val("BMW%").end().
		end().model();
	assertEquals("models are different", entQuery1(qry2), entQuery1(qry));
    }

    @Ignore
    @Test
    public void test_ignore_in_multiple_vs_single_like_test_using_equivalent_model() {
	// TODO implement anyOfIValues
	final String [] values = new String[]{null, null, null};
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).where().allOfProps("model.key", "model.make.key").like().anyOfValues(values).model();
	final EntityResultQueryModel<TgVehicle> qry2 = select(VEHICLE).where().begin().
		begin().val(0).eq().val(0).or().val(0).eq().val(0).or().val(0).eq().val(0).end().and().
		begin().val(0).eq().val(0).or().val(0).eq().val(0).or().val(0).eq().val(0).end().
		end().model();
	assertEquals("models are different", entQuery1(qry2), entQuery1(qry));
    }

    @Test
    public void test_multiple_vs_single_comparison_test_with_only_one_operand() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).where().anyOfProps("model").eq().val("MERC").model();
	final List<CompoundConditionModel> others = new ArrayList<CompoundConditionModel>();
	final GroupedConditionsModel exp = new GroupedConditionsModel(false, new ComparisonTestModel(new EntProp("model"), ComparisonOperator.EQ, mercValue), others);
	final List<CompoundConditionModel> others2 = new ArrayList<CompoundConditionModel>();
	final ConditionsModel exp2 = new ConditionsModel(exp, others2);
	assertEquals("models are different", exp2, entQuery1(qry).getConditions());
    }

    @Test
    public void test_single_vs_multiple_comparison_test() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).where().prop("model").eq().anyOfValues("MERC", "AUDI").model();
	final List<CompoundConditionModel> others = new ArrayList<CompoundConditionModel>();
	others.add(new CompoundConditionModel(LogicalOperator.OR, new ComparisonTestModel(new EntProp("model"), ComparisonOperator.EQ, audiValue)));
	final GroupedConditionsModel exp = new GroupedConditionsModel(false, new ComparisonTestModel(new EntProp("model"), ComparisonOperator.EQ, mercValue), others);
	final List<CompoundConditionModel> others2 = new ArrayList<CompoundConditionModel>();
	final ConditionsModel exp2 = new ConditionsModel(exp, others2);
	assertEquals("models are different", exp2, entQuery1(qry).getConditions());
    }

    @Test
    public void test_single_vs_multiple_comparison_test_with_only_one_operand() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).where().prop("model").eq().anyOfValues("MERC").model();
	final List<CompoundConditionModel> others = new ArrayList<CompoundConditionModel>();
	final GroupedConditionsModel exp = new GroupedConditionsModel(false, new ComparisonTestModel(new EntProp("model"), ComparisonOperator.EQ, mercValue), others);
	final List<CompoundConditionModel> others2 = new ArrayList<CompoundConditionModel>();
	final ConditionsModel exp2 = new ConditionsModel(exp, others2);
	assertEquals("models are different", exp2, entQuery1(qry).getConditions());
    }

    @Test
    public void test_set_test_with_values() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).where().prop("model").in().values("MERC", "AUDI").model();
	final ConditionsModel exp2 = new ConditionsModel(new SetTestModel(new EntProp("model"), false, new EntSet(Arrays.asList(new ISingleOperand[]{mercValue, audiValue}))), new ArrayList<CompoundConditionModel>());
	assertEquals("models are different", exp2, entQuery1(qry).getConditions());
    }

    @Test
    public void test_set_test_with_values_and_multiple_operand() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).where().anyOfProps("key", "desc").in().values("sta1", "sta2").model();

	final List<CompoundConditionModel> others = new ArrayList<CompoundConditionModel>();
	others.add(new CompoundConditionModel(LogicalOperator.OR, new SetTestModel(new EntProp("desc"), false, new EntSet(Arrays.asList(new ISingleOperand[]{new EntValue("sta1"), new EntValue("sta2")})))));
	final GroupedConditionsModel exp = new GroupedConditionsModel(false, new SetTestModel(new EntProp("key"), false, new EntSet(Arrays.asList(new ISingleOperand[]{new EntValue("sta1"), new EntValue("sta2")}))), others);
	final List<CompoundConditionModel> others2 = new ArrayList<CompoundConditionModel>();
	final ConditionsModel exp2 = new ConditionsModel(exp, others2);
	assertEquals("models are different", exp2, entQuery1(qry).getConditions());
    }

    @Test
    public void test_set_test_with_query() {
	final EntityResultQueryModel<TgVehicleModel> vehModels = select(MODEL).model();
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).where().prop("model").in().model(vehModels).model();
	final ConditionsModel exp2 = new ConditionsModel(new SetTestModel(new EntProp("model"), false, new EntSetFromQryModel(entSubQuery1(vehModels))), new ArrayList<CompoundConditionModel>());
	assertEquals("models are different", exp2, entQuery1(qry).getConditions());
    }

    @Test
    public void test_plain_quantified_test() {
	final EntityResultQueryModel<TgVehicleModel> vehModels = select(MODEL).model();
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).where().prop("model").eq().any(vehModels).model();
	final ConditionsModel exp2 = new ConditionsModel(new QuantifiedTestModel(new EntProp("model"), ComparisonOperator.EQ, Quantifier.ANY, entSubQuery1(vehModels)), new ArrayList<CompoundConditionModel>());
	assertEquals("models are different", exp2, entQuery1(qry).getConditions());
    }

    @Test
    public void test_multiple_quantified_test() {
	final EntityResultQueryModel<TgVehicleModel> vehModels = select(MODEL).model();
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).where().anyOfProps("key", "desc").eq().any(vehModels).model();
	final List<CompoundConditionModel> others = new ArrayList<CompoundConditionModel>();
	others.add(new CompoundConditionModel(LogicalOperator.OR, new QuantifiedTestModel(new EntProp("desc"), ComparisonOperator.EQ, Quantifier.ANY, entSubQuery1(vehModels))));
	final GroupedConditionsModel exp = new GroupedConditionsModel(false, new QuantifiedTestModel(new EntProp("key"), ComparisonOperator.EQ, Quantifier.ANY, entSubQuery1(vehModels)), others);
	final List<CompoundConditionModel> others2 = new ArrayList<CompoundConditionModel>();
	final ConditionsModel exp2 = new ConditionsModel(exp, others2);
	assertEquals("models are different", exp2, entQuery1(qry).getConditions());
    }

    @Test
    public void test_simple_query_model_01() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).as("v").where().prop("model").isNotNull().and().prop("station").isNull().model();
	final List<CompoundConditionModel> others = new ArrayList<CompoundConditionModel>();
	others.add(new CompoundConditionModel(LogicalOperator.AND, new NullTestModel(new EntProp("station"), false)));
	final ConditionsModel exp = new ConditionsModel(new NullTestModel(new EntProp("model"), true), others);
	assertEquals("models are different", exp, entQuery1(qry).getConditions());
    }

    @Test
    public void test_simple_query_model_02() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).as("v").where().prop("price").gt().val(100).and().prop("purchasePrice").lt().prop("price").model();
	final List<CompoundConditionModel> others = new ArrayList<CompoundConditionModel>();
	others.add(new CompoundConditionModel(LogicalOperator.AND, new ComparisonTestModel(new EntProp("purchasePrice"), ComparisonOperator.LT, new EntProp("price"))));
	final ConditionsModel exp = new ConditionsModel(new ComparisonTestModel(new EntProp("price"), ComparisonOperator.GT, new EntValue(100)), others);
	assertEquals("models are different", exp, entQuery1(qry).getConditions());
    }

    @Test
    public void test_simple_query_model_03() {
	final Map<String, Object> paramValues = new HashMap<String, Object>();
	paramValues.put("eqclass", "class1");

	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).as("v").where().prop("model").isNotNull().and().param("eqclass").isNull().model();
	final List<CompoundConditionModel> others = new ArrayList<CompoundConditionModel>();
	others.add(new CompoundConditionModel(LogicalOperator.AND, new NullTestModel(new EntValue("class1"), false)));
	final ConditionsModel exp = new ConditionsModel(new NullTestModel(new EntProp("model"), true), others);
	assertEquals("models are different", exp, entQuery1(qry, paramValues).getConditions());
    }

    @Test
    public void test_simple_query_model_04() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).as("v").where().begin().prop("model").isNotNull().and().prop("station").isNull().end().model();
	final List<CompoundConditionModel> others = new ArrayList<CompoundConditionModel>();
	others.add(new CompoundConditionModel(LogicalOperator.AND, new NullTestModel(new EntProp("station"), false)));
	final GroupedConditionsModel exp = new GroupedConditionsModel(false, new NullTestModel(new EntProp("model"), true), others);
	final ConditionsModel exp2 = new ConditionsModel(exp, new ArrayList<CompoundConditionModel>());
	assertEquals("models are different", exp2, entQuery1(qry).getConditions());
    }

    @Test
    public void test_simple_query_model_05() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).as("v").where().begin().prop("model").isNotNull().and().prop("station").isNull().end().and().prop("price").isNotNull().model();
	final List<CompoundConditionModel> others = new ArrayList<CompoundConditionModel>();
	others.add(new CompoundConditionModel(LogicalOperator.AND, new NullTestModel(new EntProp("station"), false)));
	final GroupedConditionsModel exp = new GroupedConditionsModel(false, new NullTestModel(new EntProp("model"), true), others);
	final List<CompoundConditionModel> others2 = new ArrayList<CompoundConditionModel>();
	others2.add(new CompoundConditionModel(LogicalOperator.AND, new NullTestModel(new EntProp("price"), true)));
	final ConditionsModel exp2 = new ConditionsModel(exp, others2);
	assertEquals("models are different", exp2, entQuery1(qry).getConditions());
    }

    @Test
    public void test_simple_query_model_06() {
	final EntityResultQueryModel<TgVehicle> subQry = select(VEHICLE).model();
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).as("v").where().prop("model").isNotNull().and().exists(subQry).model();
	final List<CompoundConditionModel> others = new ArrayList<CompoundConditionModel>();
	others.add(new CompoundConditionModel(LogicalOperator.AND, new ExistenceTestModel(false, entSubQuery1(subQry))));
	final ConditionsModel exp = new ConditionsModel(new NullTestModel(new EntProp("model"), true), others);
	assertEquals("models are different", exp, entQuery1(qry).getConditions());
    }

    @Test
    public void test_simple_query_model_07() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).as("v").where().anyOfProps("model", "station").isNotNull().model();
	final List<CompoundConditionModel> others = new ArrayList<CompoundConditionModel>();
	others.add(new CompoundConditionModel(LogicalOperator.OR, new NullTestModel(new EntProp("station"), true)));
	final GroupedConditionsModel exp = new GroupedConditionsModel(false, new NullTestModel(new EntProp("model"), true), others);
	final List<CompoundConditionModel> others2 = new ArrayList<CompoundConditionModel>();
	final ConditionsModel exp2 = new ConditionsModel(exp, others2);
	assertEquals("models are different", exp2, entQuery1(qry).getConditions());
    }

    @Test
    public void test_simple_query_model_08() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).as("v").where().beginExpr().prop("price").add().prop("purchasePrice").endExpr().isNull().model();
	final List<CompoundConditionModel> others = new ArrayList<CompoundConditionModel>();
	final List<CompoundSingleOperand> compSingleOperands = new ArrayList<CompoundSingleOperand>();
	compSingleOperands.add(new CompoundSingleOperand(new EntProp("purchasePrice"), ArithmeticalOperator.ADD));
	final Expression expression = new Expression(new EntProp("price"), compSingleOperands);
	final ConditionsModel exp = new ConditionsModel(new NullTestModel(expression, false), others);
	assertEquals("models are different", exp, entQuery1(qry).getConditions());
    }

    @Test
    public void test_simple_query_model_09() {
	final ExpressionModel expr = expr().prop("price.amount").add().prop("purchasePrice.amount").model();
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).as("v").where().expr(expr).isNull().model();
	final List<CompoundConditionModel> others = new ArrayList<CompoundConditionModel>();
	final List<CompoundSingleOperand> compSingleOperands = new ArrayList<CompoundSingleOperand>();
	compSingleOperands.add(new CompoundSingleOperand(new EntProp("purchasePrice.amount"), ArithmeticalOperator.ADD));
	final Expression expression = new Expression(new EntProp("price.amount"), compSingleOperands);
	final ConditionsModel exp = new ConditionsModel(new NullTestModel(expression, false), others);
	assertEquals("models are different", exp, entQuery1(qry).getConditions());
    }

    @Test
    public void test_simple_query_model_10() {
	final ExpressionModel expr0 = expr().prop("model").mult().prop("station").model();
	final ExpressionModel expr = expr().prop("model").add().expr(expr0).model();
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).as("v").where().expr(expr).isNull().model();
	final List<CompoundConditionModel> others = new ArrayList<CompoundConditionModel>();

	final List<CompoundSingleOperand> compSingleOperands0 = new ArrayList<CompoundSingleOperand>();
	compSingleOperands0.add(new CompoundSingleOperand(new EntProp("station"), ArithmeticalOperator.MULT));
	final Expression expression0 = new Expression(new EntProp("model"), compSingleOperands0);


	final List<CompoundSingleOperand> compSingleOperands = new ArrayList<CompoundSingleOperand>();
	compSingleOperands.add(new CompoundSingleOperand(expression0, ArithmeticalOperator.ADD));
	final Expression expression = new Expression(new EntProp("model"), compSingleOperands);
	final ConditionsModel exp = new ConditionsModel(new NullTestModel(expression, false), others);
	assertEquals("models are different", exp, entQuery1(qry).getConditions());
    }

    @Test
    public void test_simple_query_model_11() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).as("v").where().dayOf().prop("initDate").gt().val(15).and().prop("price").lt().prop("purchasePrice").model();
	final List<CompoundConditionModel> others = new ArrayList<CompoundConditionModel>();
	others.add(new CompoundConditionModel(LogicalOperator.AND, new ComparisonTestModel(new EntProp("price"), ComparisonOperator.LT, new EntProp("purchasePrice"))));
	final ConditionsModel exp = new ConditionsModel(new ComparisonTestModel(new DayOfModel(new EntProp("initDate")), ComparisonOperator.GT, new EntValue(15)), others);
	assertEquals("models are different", exp, entQuery1(qry).getConditions());
    }

    @Test
    public void test_simple_query_model_12() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).as("v").where().monthOf().prop("initDate").gt().val(3).and().prop("price").lt().prop("purchasePrice").model();
	final List<CompoundConditionModel> others = new ArrayList<CompoundConditionModel>();
	others.add(new CompoundConditionModel(LogicalOperator.AND, new ComparisonTestModel(new EntProp("price"), ComparisonOperator.LT, new EntProp("purchasePrice"))));
	final ConditionsModel exp = new ConditionsModel(new ComparisonTestModel(new MonthOfModel(new EntProp("initDate")), ComparisonOperator.GT, new EntValue(3)), others);
	assertEquals("models are different", exp, entQuery1(qry).getConditions());
    }

    @Test
    public void test_ignore_of_null_value_in_condition1() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).where().prop("model.desc").like().iVal("MERC%").model();
	final ConditionsModel exp = new ConditionsModel(new LikeTestModel(new EntProp("model.desc"), new EntValue("MERC%"), false, false), new ArrayList<CompoundConditionModel>());
	assertEquals("models are different", exp, entQuery1(qry).getConditions());
    }

    @Test
    public void test_ignore_of_null_value_in_condition2() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).where().prop("model.desc").like().iVal(null).model();
	final ConditionsModel exp = new ConditionsModel(alwaysTrueCondition, new ArrayList<CompoundConditionModel>());
	assertEquals("models are different", exp, entQuery1(qry).getConditions());
    }

    @Test
    public void test_ignore_of_null_value_in_condition3() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).where().prop("model.desc").like().param("param").model();
	final ConditionsModel exp = new ConditionsModel(new LikeTestModel(new EntProp("model.desc"), new EntValue("MERC%"), false, false), new ArrayList<CompoundConditionModel>());
	final Map<String, Object> paramValues = new HashMap<String, Object>();
	paramValues.put("param", "MERC%");
	assertEquals("models are different", exp, entQuery1(qry, paramValues).getConditions());
    }

    @Test
    public void test_ignore_of_null_value_in_condition4() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).where().prop("model.desc").like().iParam("param").model();
	final ConditionsModel exp = new ConditionsModel(alwaysTrueCondition, new ArrayList<CompoundConditionModel>());
	final Map<String, Object> paramValues = new HashMap<String, Object>();
	paramValues.put("param", null);
	assertEquals("models are different", exp, entQuery1(qry, paramValues).getConditions());
    }

    @Test
    public void test_ignore_of_null_value_in_condition5() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).where().prop("model.desc").like().anyOfValues().model();
	final ConditionsModel exp = new ConditionsModel(new GroupedConditionsModel(false, alwaysTrueCondition, new ArrayList<CompoundConditionModel>()), new ArrayList<CompoundConditionModel>());
	assertEquals("models are different", exp, entQuery1(qry).getConditions());
    }

    @Test
    public void test_simple_query_model_22() {
	final EntityResultQueryModel<TgVehicle> subQry1 = select(VEHICLE).model();
	final EntityResultQueryModel<TgWorkOrder> subQry2 = select(TgWorkOrder.class).model();
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).as("v").where().existsAnyOf(subQry1, subQry2).model();
	final List<CompoundConditionModel> others = new ArrayList<CompoundConditionModel>();
	others.add(new CompoundConditionModel(LogicalOperator.OR, new ExistenceTestModel(false, entSubQuery1(subQry2))));
	final GroupedConditionsModel exp = new GroupedConditionsModel(false, new ExistenceTestModel(false, entSubQuery1(subQry1)), others);
	final List<CompoundConditionModel> others2 = new ArrayList<CompoundConditionModel>();
	final ConditionsModel exp2 = new ConditionsModel(exp, others2);
	assertEquals("models are different", exp2, entQuery1(qry).getConditions());
    }

    @Test
    public void test_expressions1() {
	final Map<String, Object> paramValues = new HashMap<String, Object>();
	paramValues.put("costMultiplier", 1);
	paramValues.put("costDivider", 2);

	final EntityResultQueryModel<TgWorkOrder> qry =  select(TgWorkOrder.class).as("wo").where(). //
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
	final Expression expressionModel1 = new Expression(new EntProp("wo.actCost.amount"), Arrays.asList(new CompoundSingleOperand[]{new CompoundSingleOperand(new EntValue(1), ArithmeticalOperator.MULT)}));
	final Expression expressionModel2 = new Expression(expressionModel1, Arrays.asList(new CompoundSingleOperand[]{new CompoundSingleOperand(new EntProp("wo.estCost.amount"), ArithmeticalOperator.ADD), new CompoundSingleOperand(new EntValue(2), ArithmeticalOperator.DIV)}));
	final Expression expressionModel3 = new Expression(expressionModel2, Arrays.asList(new CompoundSingleOperand[]{new CompoundSingleOperand(new EntProp("wo.yearlyCost.amount"), ArithmeticalOperator.ADD)}));
	final Expression expressionModel = new Expression(expressionModel3, Arrays.asList(new CompoundSingleOperand[]{new CompoundSingleOperand(new EntValue(12), ArithmeticalOperator.DIV)}));

	final ConditionsModel exp = new ConditionsModel(new ComparisonTestModel(expressionModel, ComparisonOperator.GT, new EntValue(1000)), new ArrayList<CompoundConditionModel>());
	assertEquals("models are different", exp, entQuery1(qry, paramValues).getConditions());
    }

    @Test
    public void test_query_sources3() {
	final EntityResultQueryModel<TgVehicle> qry = select(VEHICLE).as("v").join(TgWorkOrder.class).as("wo").on().prop("v").eq().prop("wo.vehicle").leftJoin(TgWorkOrder.class).as("wo2").on().prop("v").eq().prop("wo2.vehicle"). //
	where().dayOf().prop("initDate").gt().val(15).and().prop("price").lt().prop("purchasePrice").model();

	final ConditionsModel condition1 = new ConditionsModel(new ComparisonTestModel(new EntProp("v"), ComparisonOperator.EQ, new EntProp("wo.vehicle")), new ArrayList<CompoundConditionModel>());
	final ConditionsModel condition2 = new ConditionsModel(new ComparisonTestModel(new EntProp("v"), ComparisonOperator.EQ, new EntProp("wo2.vehicle")), new ArrayList<CompoundConditionModel>());

	final List<EntQueryCompoundSourceModel> others = new ArrayList<EntQueryCompoundSourceModel>();
	others.add(new EntQueryCompoundSourceModel(new EntQuerySourceAsEntity(TgWorkOrder.class, "wo"), JoinType.IJ, condition1));
	others.add(new EntQueryCompoundSourceModel(new EntQuerySourceAsEntity(TgWorkOrder.class, "wo2"), JoinType.LJ, condition2));

	final EntQuery act = entQuery1(qry);
	final EntQuerySourcesModel exp = new EntQuerySourcesModel(new EntQuerySourceAsEntity(VEHICLE, "v"), others);
	assertEquals("models are different", exp, act.getSources());

	final List<CompoundConditionModel> others2 = new ArrayList<CompoundConditionModel>();
	others2.add(new CompoundConditionModel(LogicalOperator.AND, new ComparisonTestModel(new EntProp("price"), ComparisonOperator.LT, new EntProp("purchasePrice"))));
	final ConditionsModel exp2 = new ConditionsModel(new ComparisonTestModel(new DayOfModel(new EntProp("initDate")), ComparisonOperator.GT, new EntValue(15)), others2);
	assertEquals("models are different", exp2, act.getConditions());
    }
}