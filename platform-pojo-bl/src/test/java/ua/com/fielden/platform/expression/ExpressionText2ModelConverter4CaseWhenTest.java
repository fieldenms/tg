package ua.com.fielden.platform.expression;

import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.cond;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;

import java.math.BigDecimal;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.expression.ast.AstNode;
import ua.com.fielden.platform.expression.ast.visitor.entities.EntityLevel1;
import ua.com.fielden.platform.expression.automata.SequenceRecognitionFailed;
import ua.com.fielden.platform.expression.exception.RecognitionException;
import ua.com.fielden.platform.expression.exception.semantic.SemanticException;
import ua.com.fielden.platform.types.Money;

public class ExpressionText2ModelConverter4CaseWhenTest {
    
    @Test
    public void model_generation_for_CASE_without_ELSE_is_supported() throws RecognitionException, SemanticException {
        final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, //
        "CASE WHEN intProperty > 2 THEN \"Over two\"\n END");
        final AstNode root = ev.convert();
        assertEquals("Incorrect expression type", String.class, root.getType());

        final ExpressionModel prop = expr().prop("intProperty").model();
        final ConditionModel cond = cond().expr(prop).gt().val(2).model();
        final ExpressionModel model = expr().caseWhen().condition(cond).then().expr(expr().val("Over two").model()).end().model();
        assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void model_generation_for_CASE_with_int_literals_in_THEN_is_supported() throws RecognitionException, SemanticException {
        final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, //
        "CASE WHEN intProperty > 2 THEN 42\n END");
        final AstNode root = ev.convert();
        assertEquals("Incorrect expression type", Integer.class, root.getType());

        final ExpressionModel prop = expr().prop("intProperty").model();
        final ConditionModel cond = cond().expr(prop).gt().val(2).model();
        final ExpressionModel model = expr().caseWhen().condition(cond).then().expr(expr().val(42).model()).end().model();
        assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void model_generation_for_CASE_with_arithmetic_expression_in_THEN_and_ELSE_is_supported() throws RecognitionException, SemanticException {
        final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, //
        "CASE WHEN intProperty > 2 THEN 42\n ELSE 2 + 3 END");
        final AstNode root = ev.convert();
        assertEquals("Incorrect expression type", Integer.class, root.getType());

        final ExpressionModel prop = expr().prop("intProperty").model();
        final ConditionModel cond = cond().expr(prop).gt().val(2).model();
        final ExpressionModel model = expr()
                                        .caseWhen().condition(cond)
                                        .then().expr(expr().val(42).model())
                                        .otherwise().expr(expr().val(2).add().val(3).model())
                                        .end().model();
        assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void this_CASE_with_arithmetic_expression_resolves_to_type_BigDecimal() throws RecognitionException, SemanticException {
        final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, //
        "CASE WHEN intProperty > 2 THEN 42\n ELSE 2.3 + 3 END");
        final AstNode root = ev.convert();
        assertEquals("Incorrect expression type", BigDecimal.class, root.getType());

        final ExpressionModel prop = expr().prop("intProperty").model();
        final ConditionModel cond = cond().expr(prop).gt().val(2).model();
        final ExpressionModel model = expr()
                                        .caseWhen().condition(cond)
                                        .then().expr(expr().val(42).model())
                                        .otherwise().expr(expr().val(new BigDecimal("2.3")).add().val(3).model())
                                        .end().model();
        assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void model_generation_for_dashboard_CASE_with_multiple_complex_WHEN_conditions() throws RecognitionException, SemanticException {
        final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, //
        "CASE\n" + "WHEN MONTHS(dateProperty, NOW) > 2m THEN \"Green\"\n" + "WHEN MONTHS(dateProperty, NOW) <= 2m && MONTHS(dateProperty, NOW) > 1m THEN \"Yellow\"\n"
                + "ELSE \"Red\" END");
        final AstNode root = ev.convert();
        assertEquals("Incorrect expression type", String.class, root.getType());

        final ExpressionModel months1 = expr().count().months().between().prop("dateProperty").and().expr(expr().now().model()).model();
        final ConditionModel cond1 = cond().expr(months1).gt().val(2).model();
        final ExpressionModel months2 = expr().count().months().between().prop("dateProperty").and().expr(expr().now().model()).model();
        final ExpressionModel months3 = expr().count().months().between().prop("dateProperty").and().expr(expr().now().model()).model();
        final ConditionModel cond2 = cond().expr(months2).le().val(2).model();
        final ConditionModel cond3 = cond().expr(months3).gt().val(1).model();
        final ConditionModel andCond = cond().condition(cond2).and().condition(cond3).model();
        
        final ExpressionModel model = expr().caseWhen().condition(cond1)
                .then().expr(expr().val("Green").model())
                .when().condition(andCond)
                .then().expr(expr().val("Yellow").model())
                .otherwise().expr(expr().val("Red").model())
                .end().model();
        assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void model_generation_for_simple_dashboard_CASE_with_NOW_in_WHEN_condition() throws RecognitionException, SemanticException {
        final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, //
        "CASE\nWHEN dateProperty > NOW THEN \"Green\"\n" + "ELSE \"Red\" END");
        final AstNode root = ev.convert();
        assertEquals("Incorrect expression type", String.class, root.getType());

        final ExpressionModel prop = expr().prop("dateProperty").model();
        final ConditionModel cond = cond().expr(prop).gt().expr(expr().now().model()).model();
        final ExpressionModel model = expr().caseWhen().condition(cond)
                .then().expr(expr().val("Green").model())
                .otherwise().expr(expr().val("Red").model())
                .end().model();
        assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void model_for_CASE_WHEN_over_SUM_divided_by_SUM_is_supported() throws RecognitionException, SemanticException {
        final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, //
                "CASE WHEN SUM(intProperty) <> 0 THEN SUM(moneyProperty) / SUM(intProperty) END");
        final AstNode root = ev.convert();
        assertEquals("Incorrect expression type", Money.class, root.getType());

        final ExpressionModel SUM_of_intProperty = expr().sumOf().prop("intProperty").model();
        final ExpressionModel SUM_of_moneyProperty = expr().sumOf().prop("moneyProperty").model();
        final ConditionModel whenCond = cond().expr(SUM_of_intProperty).ne().val(0).model();
        final ExpressionModel thenResult = expr().expr(SUM_of_moneyProperty).div().expr(SUM_of_intProperty).model();

        final ExpressionModel model = expr()
                .caseWhen().condition(whenCond)
                .then().expr(thenResult)
                .end().model();
        assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void model_for_SUM_divided_by_SUM_under_CASE_WHEN_is_supported() throws RecognitionException, SemanticException {
         final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, //
                 "SUM(intProperty) / CASE WHEN SUM(decimalProperty) > 0 THEN SUM(decimalProperty) END");
        final AstNode root = ev.convert();
        assertEquals("Incorrect expression type", BigDecimal.class, root.getType());

        final ExpressionModel SUM_of_intProperty = expr().sumOf().prop("intProperty").model();
        final ExpressionModel SUM_of_decimalProperty = expr().sumOf().prop("decimalProperty").model();
        final ConditionModel whenCond = cond().expr(SUM_of_decimalProperty).gt().val(0).model();
        final ExpressionModel thenResult = SUM_of_decimalProperty;
        final ExpressionModel caseWhen = expr().caseWhen().condition(whenCond).then().expr(thenResult).end().model(); 

        final ExpressionModel model = expr()
                .expr(SUM_of_intProperty).div()
                .expr(caseWhen)
                .model();
        assertEquals("Incorrect model.", model, root.getModel());
    }

}
