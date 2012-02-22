package ua.com.fielden.platform.expression;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.entity.query.fluent.query.expr;
import static ua.com.fielden.platform.expression.ast.visitor.CollectionalContextVisitor.SUPER;
import static ua.com.fielden.platform.expression.ast.visitor.CollectionalContextVisitor.THIS;

import java.math.BigDecimal;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.expression.ast.AstNode;
import ua.com.fielden.platform.expression.ast.visitor.entities.EntityLevel1;
import ua.com.fielden.platform.expression.exception.RecognitionException;
import ua.com.fielden.platform.expression.exception.semantic.IncompatibleOperandException;
import ua.com.fielden.platform.expression.exception.semantic.SemanticException;

public class ExpressionText2ModelConverter4MonthTest {

    @Test
    public void test_case_01() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "(MONTH(dateProperty) + intProperty + decimalProperty) * 2");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", BigDecimal.class, root.getType());

	final ExpressionModel func = expr().monthOf().prop("dateProperty").model();
	final ExpressionModel plus2 = expr().prop("intProperty").add().prop("decimalProperty").model();
	final ExpressionModel plus1 = expr().expr(func).add().expr(plus2).model();
	final ExpressionModel model = expr().expr(plus1).mult().val(2).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_02() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "(MONTH(MAX(collectional.dateProperty)) + intProperty) * 2");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Integer.class, root.getType());

	final ExpressionModel max = expr().maxOf().prop("collectional.dateProperty").model();
	final ExpressionModel func = expr().monthOf().expr(max).model();
	final ExpressionModel plus1 = expr().expr(func).add().prop("intProperty").model();
	final ExpressionModel model = expr().expr(plus1).mult().val(2).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_03() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "(SUM(MONTH(collectional.dateProperty)) + intProperty) * 2");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Integer.class, root.getType());

	final ExpressionModel days = expr().monthOf().prop("collectional.dateProperty").model();
	final ExpressionModel sumOfDays = expr().sumOf().expr(days).model();
	final ExpressionModel plus1 = expr().expr(sumOfDays).add().prop("intProperty").model();
	final ExpressionModel model = expr().expr(plus1).mult().val(2).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_04() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "intProperty * intProperty / MONTH(MIN(collectional.dateProperty))");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", BigDecimal.class, root.getType());

	final ExpressionModel min = expr().minOf().prop("collectional.dateProperty").model();
	final ExpressionModel dayOfMin = expr().monthOf().expr(min).model();
	final ExpressionModel mult = expr().prop("intProperty").mult().prop("intProperty").model();
	final ExpressionModel model = expr().expr(mult).div().expr(dayOfMin).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_05() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "intProperty * intProperty / (MONTH(MAX(collectional.dateProperty)) * intProperty)");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", BigDecimal.class, root.getType());

	final ExpressionModel max = expr().maxOf().prop("collectional.dateProperty").model();
	final ExpressionModel dayOfMax = expr().monthOf().expr(max).model();
	final ExpressionModel mult2 = expr().expr(dayOfMax).mult().prop("intProperty").model();
	final ExpressionModel mult1 = expr().prop("intProperty").mult().prop("intProperty").model();
	final ExpressionModel model = expr().expr(mult1).div().expr(mult2).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_06() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "entityProperty.intProperty * intProperty / (MONTH(dateProperty) * intProperty)");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", BigDecimal.class, root.getType());

	final ExpressionModel func = expr().monthOf().prop("dateProperty").model();
	final ExpressionModel mult2 = expr().expr(func).mult().prop("intProperty").model();
	final ExpressionModel mult1 = expr().prop("entityProperty.intProperty").mult().prop("intProperty").model();
	final ExpressionModel model = expr().expr(mult1).div().expr(mult2).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_07() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "entityProperty.intProperty * MONTH(entityProperty.dateProperty)");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Integer.class, root.getType());

	final ExpressionModel func = expr().monthOf().prop("entityProperty.dateProperty").model();
	final ExpressionModel model = expr().prop("entityProperty.intProperty").mult().expr(func).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_08() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "entityProperty.intProperty * SUM(MONTH(collectional.dateProperty))");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Integer.class, root.getType());

	final ExpressionModel func = expr().monthOf().prop("collectional.dateProperty").model();
	final ExpressionModel sum = expr().sumOf().expr(func).model();
	final ExpressionModel model = expr().prop("entityProperty.intProperty").mult().expr(sum).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_09() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "selfProperty.intProperty - COUNT(MONTH(entityProperty.collectional.dateProperty))");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Integer.class, root.getType());

	final ExpressionModel fun = expr().monthOf().prop("entityProperty.collectional.dateProperty").model();
	final ExpressionModel count = expr().countOf().expr(fun).model();
	final ExpressionModel model = expr().prop("selfProperty.intProperty").sub().expr(count).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_10() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "MONTH(MAX(entityProperty.collectional.dateProperty)) + COUNT(collectional.intProperty)");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Integer.class, root.getType());

	final ExpressionModel max = expr().maxOf().prop("entityProperty.collectional.dateProperty").model();
	final ExpressionModel fun = expr().monthOf().expr(max).model();
	final ExpressionModel count2 = expr().countOf().prop("collectional.intProperty").model();
	final ExpressionModel model = expr().expr(fun).add().expr(count2).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_11() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "COUNT(MONTH(collectional.dateProperty) + MONTH(selfProperty.collectional.dateProperty))");
	try {
	    final AstNode root = ev.convert();
	    System.out.println(root.getLevel());
	    fail("Should have failed due to incorrect tag.");
	} catch (final IncompatibleOperandException ex) {
	    assertEquals("Incorrect message", "Incompatible operand context for operation '+': 'selfProperty.collectional' is not compatible with 'collectional'.", ex.getMessage());
	}
    }

    @Test
    public void test_case_12() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "COUNT(MONTH(selfProperty.dateProperty) + MONTH(entityProperty.dateProperty))");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Integer.class, root.getType());
	assertEquals("Incorrect expression tag", SUPER, root.getTag());

	final ExpressionModel func1 = expr().monthOf().prop("selfProperty.dateProperty").model();
	final ExpressionModel func2 = expr().monthOf().prop("entityProperty.dateProperty").model();
	final ExpressionModel plus = expr().expr(func1).add().expr(func2).model();
	final ExpressionModel model = expr().countOf().expr(plus).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_13() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "COUNT(MONTH(selfProperty.collectional.dateProperty)) + COUNT(MONTH(entityProperty.collectional.dateProperty))");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Integer.class, root.getType());
	assertEquals("Incorrect expression tag", THIS, root.getTag());

	final ExpressionModel fun1 = expr().monthOf().prop("selfProperty.collectional.dateProperty").model();
	final ExpressionModel count1 = expr().countOf().expr(fun1).model();
	final ExpressionModel fun2 = expr().monthOf().prop("entityProperty.collectional.dateProperty").model();
	final ExpressionModel count2 = expr().countOf().expr(fun2).model();
	final ExpressionModel model = expr().expr(count1).add().expr(count2).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

}
