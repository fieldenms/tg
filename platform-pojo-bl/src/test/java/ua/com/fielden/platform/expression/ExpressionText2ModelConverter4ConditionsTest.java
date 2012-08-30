package ua.com.fielden.platform.expression;

import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.cond;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.expression.ast.AstNode;
import ua.com.fielden.platform.expression.ast.visitor.entities.EntityLevel1;
import ua.com.fielden.platform.expression.exception.RecognitionException;
import ua.com.fielden.platform.expression.exception.semantic.SemanticException;
import ua.com.fielden.platform.expression.type.Month;

public class ExpressionText2ModelConverter4ConditionsTest {

    @Test
    public void model_generation_for_trivial_comparison_failed() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "\"string\" = strProperty");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", boolean.class, root.getType());

	final ConditionModel condition = cond().val("string").eq().expr(expr().prop("strProperty").model()) .model();
	final ExpressionModel model = expr().caseWhen().condition(condition).then().val(true).otherwise().val(false).end().model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void model_generation_for_property_comparison__of_entity_type_failed() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "selfProperty = selfProperty");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", boolean.class, root.getType());

	final ConditionModel condition = cond().expr(expr().prop("selfProperty").model()).eq().expr(expr().prop("selfProperty").model()) .model();
	final ExpressionModel model = expr().caseWhen().condition(condition).then().val(true).otherwise().val(false).end().model();
	assertEquals("Incorrect model.", model, root.getModel());
    }


    @Test
    public void model_generation_for_days_functions_and_comparison_failed() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "DAYS(dateProperty, NOW) > 2");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", boolean.class, root.getType());

	final ExpressionModel func = expr().count().days().between().prop("dateProperty").and().expr(expr().now().model()).model();
	final ConditionModel condition = cond().expr(func).gt().val(2).model();
	final ExpressionModel model = expr().caseWhen().condition(condition).then().val(true).otherwise().val(false).end().model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void model_generation_for_complex_comparison_expression_failed() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, //
		"(DAYS(dateProperty, NOW) > 2 || moneyProperty < 100) &&" +
		"1000 >= SUM(collectional.intProperty) / 10 ");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", boolean.class, root.getType());

	final ExpressionModel days = expr().count().days().between().prop("dateProperty").and().expr(expr().now().model()).model();
	final ConditionModel cond1 = cond().expr(days).gt().val(2).model();
	final ConditionModel cond2 = cond().expr(expr().prop("moneyProperty").model()).lt().val(100).model();
	final ConditionModel orCond = cond().condition(cond1).or().condition(cond2).model();
	final ExpressionModel sum = expr().expr(expr().sumOf().prop("collectional.intProperty").model()).div().val(10).model();
	final ConditionModel cond3 = cond().val(1000).ge().expr(sum).model();
	final ConditionModel condition = cond().condition(orCond).and().condition(cond3).model();
	final ExpressionModel model = expr().caseWhen().condition(condition).then().val(true).otherwise().val(false).end().model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void model_generation_for_now_based_comparison_failed() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "dateProperty > NOW");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", boolean.class, root.getType());

	final ExpressionModel func = expr().prop("dateProperty").model();
	final ConditionModel condition = cond().expr(func).gt().expr(expr().now().model()).model();
	final ExpressionModel model = expr().caseWhen().condition(condition).then().val(true).otherwise().val(false).end().model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void model_generation_for_dashboard_case_when_failed() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, //
		"CASE\n" +
		"WHEN MONTHS(dateProperty, NOW) > 2m THEN \"Green\"\n" +
		"WHEN MONTHS(dateProperty, NOW) <= 2m && MONTHS(dateProperty, NOW) > 1m THEN \"Yellow\"\n" +
		"ELSE \"Red\" END");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", String.class, root.getType());

	final ExpressionModel months1 = expr().count().months().between().prop("dateProperty").and().expr(expr().now().model()).model();
	final ConditionModel cond1 = cond().expr(months1).gt().val(new Month(2)).model();
	final ExpressionModel months2 = expr().count().months().between().prop("dateProperty").and().expr(expr().now().model()).model();
	final ExpressionModel months3 = expr().count().months().between().prop("dateProperty").and().expr(expr().now().model()).model();
	final ConditionModel cond2 = cond().expr(months2).le().val(new Month(2)).model();
	final ConditionModel cond3 = cond().expr(months3).gt().val(new Month(1)).model();
	final ConditionModel andCond = cond().condition(cond2).and().condition(cond3).model();
	final ExpressionModel model = expr().
		caseWhen().condition(cond1).then().val("Green").
		when().condition(andCond).then().val("Yellow").
		otherwise().val("Red").end().model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void model_generation_for_simple_dashboard_case_when_ans_now_failed() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, //
		"CASE\n" +
		"WHEN dateProperty > NOW THEN \"Green\"\n" +
		"ELSE \"Red\" END");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", String.class, root.getType());

	final ExpressionModel prop = expr().prop("dateProperty").model();
	final ConditionModel cond = cond().expr(prop).gt().expr(expr().now().model()).model();
	final ExpressionModel model = expr().
		caseWhen().condition(cond).then().val("Green").
		otherwise().val("Red").end().model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

}
