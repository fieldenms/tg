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
import ua.com.fielden.platform.types.Money;

public class ExpressionText2ModelConverter4CountTest {

    @Test
    public void test_case_01() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "(COUNT(collectional.intProperty) + intProperty + decimalProperty + moneyProperty) * 2");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Money.class, root.getType());

	final ExpressionModel func = expr().countOf().prop("collectional.intProperty").model();
	final ExpressionModel plus3 = expr().prop("decimalProperty").add().prop("moneyProperty").model();
	final ExpressionModel plus2 = expr().prop("intProperty").add().expr(plus3).model();
	final ExpressionModel plus1 = expr().expr(func).add().expr(plus2).model();
	final ExpressionModel model = expr().expr(plus1).mult().val(2).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_02() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "(COUNT(collectional.intProperty) + intProperty) * 2");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Integer.class, root.getType());

	final ExpressionModel func = expr().countOf().prop("collectional.intProperty").model();
	final ExpressionModel plus1 = expr().expr(func).add().prop("intProperty").model();
	final ExpressionModel model = expr().expr(plus1).mult().val(2).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_03() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "(COUNT(collectional.intProperty) + intProperty) * 2");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Integer.class, root.getType());

	final ExpressionModel func = expr().countOf().prop("collectional.intProperty").model();
	final ExpressionModel plus1 = expr().expr(func).add().prop("intProperty").model();
	final ExpressionModel model = expr().expr(plus1).mult().val(2).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_04() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "intProperty * intProperty / COUNT(collectional.intProperty)");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", BigDecimal.class, root.getType());

	final ExpressionModel func = expr().countOf().prop("collectional.intProperty").model();
	final ExpressionModel mult = expr().prop("intProperty").mult().prop("intProperty").model();
	final ExpressionModel model = expr().expr(mult).div().expr(func).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_05() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "intProperty * intProperty / (COUNT(collectional.intProperty) * intProperty)");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", BigDecimal.class, root.getType());

	final ExpressionModel func = expr().countOf().prop("collectional.intProperty").model();
	final ExpressionModel mult2 = expr().expr(func).mult().prop("intProperty").model();
	final ExpressionModel mult1 = expr().prop("intProperty").mult().prop("intProperty").model();
	final ExpressionModel model = expr().expr(mult1).div().expr(mult2).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }


    @Test
    public void test_case_06() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "entityProperty.intProperty * intProperty / (COUNT(collectional.intProperty) * intProperty)");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", BigDecimal.class, root.getType());

	final ExpressionModel func = expr().countOf().prop("collectional.intProperty").model();
	final ExpressionModel mult2 = expr().expr(func).mult().prop("intProperty").model();
	final ExpressionModel mult1 = expr().prop("entityProperty.intProperty").mult().prop("intProperty").model();
	final ExpressionModel model = expr().expr(mult1).div().expr(mult2).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_07() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "entityProperty.intProperty * COUNT(collectional.intProperty)");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Integer.class, root.getType());

	final ExpressionModel func = expr().countOf().prop("collectional.intProperty").model();
	final ExpressionModel model = expr().prop("entityProperty.intProperty").mult().expr(func).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_08() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "entityProperty.intProperty * COUNT(collectional.intProperty)");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Integer.class, root.getType());

	final ExpressionModel func = expr().countOf().prop("collectional.intProperty").model();
	final ExpressionModel model = expr().prop("entityProperty.intProperty").mult().expr(func).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_09() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "selfProperty.intProperty - COUNT(entityProperty.collectional.moneyProperty)");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Integer.class, root.getType());

	final ExpressionModel fun = expr().countOf().prop("entityProperty.collectional.moneyProperty").model();
	final ExpressionModel model = expr().prop("selfProperty.intProperty").sub().expr(fun).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_10() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "COUNT(entityProperty.collectional.moneyProperty) + COUNT(collectional.intProperty)");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Integer.class, root.getType());

	final ExpressionModel count1 = expr().countOf().prop("entityProperty.collectional.moneyProperty").model();
	final ExpressionModel count2 = expr().countOf().prop("collectional.intProperty").model();
	final ExpressionModel model = expr().expr(count1).add().expr(count2).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_11() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "COUNT(entityProperty.collectional.moneyProperty + moneyProperty) + SUM(collectional.intProperty)");
	try {
	    ev.convert();
	    fail("Should have failed due to incorrect tag.");
	} catch (final IncompatibleOperandException ex) {
	    assertEquals("Incorrect message", "Incompatible operand nesting level for operands of operation '+'.", ex.getMessage());
	}
    }

    @Test
    public void test_case_12() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "COUNT(collectional.intProperty + selfProperty.collectional.intProperty)");
	try {
	    ev.convert();
	    fail("Should have failed due to incorrect tag.");
	} catch (final IncompatibleOperandException ex) {
	    assertEquals("Incorrect message", "Incompatible operand context for operation '+': 'selfProperty.collectional' is not compatible with 'collectional'.", ex.getMessage());
	}
    }

    @Test
    public void test_case_13() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "COUNT(selfProperty.intProperty) + COUNT(entityProperty.intProperty)");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Integer.class, root.getType());
	assertEquals("Incorrect expression tag", SUPER, root.getTag());

	final ExpressionModel count1 = expr().countOf().prop("selfProperty.intProperty").model();
	final ExpressionModel count2 = expr().countOf().prop("entityProperty.intProperty").model();
	final ExpressionModel model = expr().expr(count1).add().expr(count2).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_14() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "COUNT(selfProperty.collectional.intProperty) + COUNT(entityProperty.collectional.moneyProperty)");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Integer.class, root.getType());
	assertEquals("Incorrect expression tag", THIS, root.getTag());

	final ExpressionModel count1 = expr().countOf().prop("selfProperty.collectional.intProperty").model();
	final ExpressionModel count2 = expr().countOf().prop("entityProperty.collectional.moneyProperty").model();
	final ExpressionModel model = expr().expr(count1).add().expr(count2).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_15() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "COUNT(MAX(collectional.collectional.moneyProperty)) + intProperty");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Integer.class, root.getType());
	assertEquals("Incorrect expression tag", THIS, root.getTag());

	final ExpressionModel max = expr().maxOf().prop("collectional.collectional.moneyProperty").model();
	final ExpressionModel count = expr().countOf().expr(max).model();
	final ExpressionModel model = expr().expr(count).add().prop("intProperty").model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_16() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "COUNT(COUNT(collectional.collectional.moneyProperty) + collectional.intProperty) + intProperty");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Integer.class, root.getType());
	assertEquals("Incorrect expression tag", THIS, root.getTag());

	final ExpressionModel count1 = expr().countOf().prop("collectional.collectional.moneyProperty").model();
	final ExpressionModel plus = expr().expr(count1).add().prop("collectional.intProperty").model();
	final ExpressionModel count2 = expr().countOf().expr(plus).model();
	final ExpressionModel model = expr().expr(count2).add().prop("intProperty").model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_17() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "COUNT(COUNT(collectional.collectional.moneyProperty) + intProperty)");
	try {
	    ev.convert();
	    fail("Should have failed due to incorrect tag.");
	} catch (final IncompatibleOperandException ex) {
	    assertEquals("Incorrect message", "Incompatible operand nesting level for operands of operation '+'.", ex.getMessage());
	}
    }

    @Test
    public void test_case_18() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "COUNT(SUM(collectional.collectional.moneyProperty) + collectional.intProperty) + " +
			"AVG(COUNT(selfProperty.collectional.collectional.moneyProperty))");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", BigDecimal.class, root.getType());
	assertEquals("Incorrect expression tag", THIS, root.getTag());

	final ExpressionModel sum = expr().sumOf().prop("collectional.collectional.moneyProperty").model();
	final ExpressionModel plus = expr().expr(sum).add().prop("collectional.intProperty").model();
	final ExpressionModel count1 = expr().countOf().expr(plus).model();
	final ExpressionModel count2 = expr().countOf().prop("selfProperty.collectional.collectional.moneyProperty").model();
	final ExpressionModel avg = expr().avgOf().expr(count2).model();
	final ExpressionModel model = expr().expr(count1).add().expr(avg).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_19() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "MIN(COUNT(collectional.collectional.moneyProperty) + entityProperty.collectional.moneyProperty)");
	try {
	    ev.convert();
	    fail("Should have failed due to incorrect tag.");
	} catch (final IncompatibleOperandException ex) {
	    assertEquals("Incorrect message", "Incompatible operand context for operation '+': 'entityProperty.collectional' is not compatible with 'collectional'.", ex.getMessage());
	}
    }

    @Test
    public void test_case_20() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "MIN(COUNT(collectional.collectional.moneyProperty) + collectional.intProperty) / " +
			"COUNT(SUM(selfProperty.collectional.collectional.moneyProperty))");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", BigDecimal.class, root.getType());
	assertEquals("Incorrect expression tag", THIS, root.getTag());

	final ExpressionModel count1 = expr().countOf().prop("collectional.collectional.moneyProperty").model();
	final ExpressionModel plus = expr().expr(count1).add().prop("collectional.intProperty").model();
	final ExpressionModel min = expr().minOf().expr(plus).model();
	final ExpressionModel sum = expr().sumOf().prop("selfProperty.collectional.collectional.moneyProperty").model();
	final ExpressionModel count2 = expr().countOf().expr(sum).model();
	final ExpressionModel model = expr().expr(min).div().expr(count2).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_21() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "COUNT(COUNT(collectional.collectional.moneyProperty) + entityProperty.collectional.moneyProperty)");
	try {
	    ev.convert();
	    fail("Should have failed due to incorrect tag.");
	} catch (final IncompatibleOperandException ex) {
	    assertEquals("Incorrect message", "Incompatible operand context for operation '+': 'entityProperty.collectional' is not compatible with 'collectional'.", ex.getMessage());
	}
    }

    @Test
    public void test_case_22() throws RecognitionException, SemanticException {
	final String expressionText = "COUNT(collectional.collectional.moneyProperty) + entityProperty.collectional.moneyProperty";
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, expressionText);
	try {
	    ev.convert();
	    fail("Should have failed due to incorrect tag.");
	} catch (final IncompatibleOperandException ex) {
	    assertEquals("Incorrect message", "Resultant expression level is incompatible with the context.", ex.getMessage());
	    assertEquals("Incorrect operation in error.", "+", expressionText.substring(ex.token().beginIndex, ex.token().endIndex).trim());
	}
    }

    @Test
    public void test_case_23() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "COUNT(SUM(collectional.collectional.moneyProperty)) + COUNT(entityProperty.collectional.moneyProperty)");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Integer.class, root.getType());
	assertEquals("Incorrect expression tag", THIS, root.getTag());

	final ExpressionModel sum = expr().sumOf().prop("collectional.collectional.moneyProperty").model();
	final ExpressionModel count1 = expr().countOf().expr(sum).model();
	final ExpressionModel count2 = expr().countOf().prop("entityProperty.collectional.moneyProperty").model();
	final ExpressionModel model = expr().expr(count1).add().expr(count2).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

}
