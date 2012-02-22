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

public class ExpressionText2ModelConverter4PropertiesAndFunctionsAvgSumTest {

    @Test
    public void test_case_01() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "intProperty + decimalProperty + moneyProperty * 2");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Money.class, root.getType());

	final ExpressionModel model = expr().prop("intProperty").add().expr(expr().prop("decimalProperty").add().expr(expr().prop("moneyProperty").mult().val(2).model()).model()).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_02() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "(intProperty + decimalProperty + moneyProperty) * 2");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Money.class, root.getType());

	final ExpressionModel model = expr().expr(expr().prop("intProperty").add().expr(expr().prop("decimalProperty").add().prop("moneyProperty").model()).model()).mult().val(2).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_03() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "(selfProperty.moneyProperty + intProperty + decimalProperty + moneyProperty) * 2");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Money.class, root.getType());

	final ExpressionModel model1 = expr().prop("selfProperty.moneyProperty").add().expr(expr().prop("intProperty").add().expr(expr().prop("decimalProperty").add().prop("moneyProperty").model()).model()).model();
	final ExpressionModel model = expr().expr(model1).mult().val(2).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_04() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "(SUM(collectional.intProperty) + intProperty + decimalProperty + moneyProperty) * 2");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Money.class, root.getType());

	final ExpressionModel sum = expr().sumOf().prop("collectional.intProperty").model();
	final ExpressionModel plus3 = expr().prop("decimalProperty").add().prop("moneyProperty").model();
	final ExpressionModel plus2 = expr().prop("intProperty").add().expr(plus3).model();
	final ExpressionModel plus1 = expr().expr(sum).add().expr(plus2).model();
	final ExpressionModel model = expr().expr(plus1).mult().val(2).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_05() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "(SUM(collectional.intProperty) + intProperty) * 2");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Integer.class, root.getType());

	final ExpressionModel sum = expr().sumOf().prop("collectional.intProperty").model();
	final ExpressionModel plus1 = expr().expr(sum).add().prop("intProperty").model();
	final ExpressionModel model = expr().expr(plus1).mult().val(2).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_06() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "intProperty * intProperty / SUM(collectional.intProperty)");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", BigDecimal.class, root.getType());

	final ExpressionModel sum = expr().sumOf().prop("collectional.intProperty").model();
	final ExpressionModel mult = expr().prop("intProperty").mult().prop("intProperty").model();
	final ExpressionModel model = expr().expr(mult).div().expr(sum).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_07() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "intProperty * intProperty / (SUM(collectional.intProperty) * intProperty)");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", BigDecimal.class, root.getType());

	final ExpressionModel sum = expr().sumOf().prop("collectional.intProperty").model();
	final ExpressionModel mult2 = expr().expr(sum).mult().prop("intProperty").model();
	final ExpressionModel mult1 = expr().prop("intProperty").mult().prop("intProperty").model();
	final ExpressionModel model = expr().expr(mult1).div().expr(mult2).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_08() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "entityProperty.intProperty * intProperty / (SUM(collectional.intProperty) * intProperty)");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", BigDecimal.class, root.getType());

	final ExpressionModel sum = expr().sumOf().prop("collectional.intProperty").model();
	final ExpressionModel mult2 = expr().expr(sum).mult().prop("intProperty").model();
	final ExpressionModel mult1 = expr().prop("entityProperty.intProperty").mult().prop("intProperty").model();
	final ExpressionModel model = expr().expr(mult1).div().expr(mult2).model();
	assertEquals("Incorrect model.", model, root.getModel());

    }

    @Test
    public void test_case_09() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "entityProperty.intProperty * intProperty");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Integer.class, root.getType());

	final ExpressionModel model = expr().prop("entityProperty.intProperty").mult().prop("intProperty").model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_10() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "entityProperty.intProperty * SUM(collectional.intProperty)");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Integer.class, root.getType());

	final ExpressionModel sum = expr().sumOf().prop("collectional.intProperty").model();
	final ExpressionModel model = expr().prop("entityProperty.intProperty").mult().expr(sum).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_11() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "selfProperty.intProperty - SUM(entityProperty.collectional.moneyProperty)");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Money.class, root.getType());

	final ExpressionModel sum = expr().sumOf().prop("entityProperty.collectional.moneyProperty").model();
	final ExpressionModel model = expr().prop("selfProperty.intProperty").sub().expr(sum).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_12() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "collectional.intProperty");
	try {
	    ev.convert();
	    fail("Should have failed due to incorrect tag.");
	} catch (final IncompatibleOperandException ex) {
	    assertEquals("Incorrect message", "Resultant expression level is incompatible with the context.", ex.getMessage());
	}
    }

    @Test
    public void test_case_13() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "collectional.collectional.moneyProperty");
	try {
	    ev.convert();
	    fail("Should have failed due to incorrect tag.");
	} catch (final IncompatibleOperandException ex) {
	    assertEquals("Incorrect message", "Resultant expression level is incompatible with the context.", ex.getMessage());
	}
    }

    @Test
    public void test_case_14() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "SUM(entityProperty.collectional.moneyProperty) + SUM(collectional.intProperty)");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Money.class, root.getType());

	final ExpressionModel sum1 = expr().sumOf().prop("entityProperty.collectional.moneyProperty").model();
	final ExpressionModel sum2 = expr().sumOf().prop("collectional.intProperty").model();
	final ExpressionModel model = expr().expr(sum1).add().expr(sum2).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_15() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "SUM(entityProperty.collectional.moneyProperty + moneyProperty) + SUM(collectional.intProperty)");
	try {
	    ev.convert();
	    fail("Should have failed due to incorrect tag.");
	} catch (final IncompatibleOperandException ex) {
	    assertEquals("Incorrect message", "Incompatible operand nesting level for operands of operation '+'.", ex.getMessage());
	}
    }

    @Test
    public void test_case_16() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "SUM(collectional.intProperty + selfProperty.collectional.intProperty)");
	try {
	    ev.convert();
	    fail("Should have failed due to incorrect tag.");
	} catch (final IncompatibleOperandException ex) {
	    assertEquals("Incorrect message", "Incompatible operand context for operation '+': 'selfProperty.collectional' is not compatible with 'collectional'.", ex.getMessage());
	}
    }

    @Test
    public void test_case_17() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "SUM(selfProperty.intProperty) + AVG(entityProperty.intProperty)");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", BigDecimal.class, root.getType());
	assertEquals("Incorrect expression tag", SUPER, root.getTag());

	final ExpressionModel sum = expr().sumOf().prop("selfProperty.intProperty").model();
	final ExpressionModel avg = expr().avgOf().prop("entityProperty.intProperty").model();
	final ExpressionModel model = expr().expr(sum).add().expr(avg).model();
	assertEquals("Incorrect model.", model, root.getModel());

    }

    @Test
    public void test_case_18() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "SUM(selfProperty.collectional.intProperty) + AVG(entityProperty.collectional.moneyProperty)");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Money.class, root.getType());
	assertEquals("Incorrect expression tag", THIS, root.getTag());

	final ExpressionModel sum = expr().sumOf().prop("selfProperty.collectional.intProperty").model();
	final ExpressionModel avg = expr().avgOf().prop("entityProperty.collectional.moneyProperty").model();
	final ExpressionModel model = expr().expr(sum).add().expr(avg).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }


    @Test
    public void test_case_19() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "SUM(SUM(collectional.collectional.moneyProperty)) + intProperty");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Money.class, root.getType());
	assertEquals("Incorrect expression tag", THIS, root.getTag());

	final ExpressionModel sum1 = expr().sumOf().prop("collectional.collectional.moneyProperty").model();
	final ExpressionModel sum2 = expr().sumOf().expr(sum1).model();
	final ExpressionModel model = expr().expr(sum2).add().prop("intProperty").model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_20() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "SUM(SUM(collectional.collectional.moneyProperty) + collectional.intProperty) + intProperty");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Money.class, root.getType());
	assertEquals("Incorrect expression tag", THIS, root.getTag());

	final ExpressionModel sum1 = expr().sumOf().prop("collectional.collectional.moneyProperty").model();
	final ExpressionModel plus = expr().expr(sum1).add().prop("collectional.intProperty").model();
	final ExpressionModel sum2 = expr().sumOf().expr(plus).model();
	final ExpressionModel model = expr().expr(sum2).add().prop("intProperty").model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_21() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "SUM(SUM(collectional.collectional.moneyProperty) + intProperty)");
	try {
	    ev.convert();
	    fail("Should have failed due to incorrect tag.");
	} catch (final IncompatibleOperandException ex) {
	    assertEquals("Incorrect message", "Incompatible operand nesting level for operands of operation '+'.", ex.getMessage());
	}
    }

    @Test
    public void test_case_22() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "SUM(SUM(collectional.collectional.moneyProperty) + collectional.intProperty) + " +
			"AVG(SUM(selfProperty.collectional.collectional.moneyProperty))");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Money.class, root.getType());
	assertEquals("Incorrect expression tag", THIS, root.getTag());

	final ExpressionModel sum1 = expr().sumOf().prop("collectional.collectional.moneyProperty").model();
	final ExpressionModel plus = expr().expr(sum1).add().prop("collectional.intProperty").model();
	final ExpressionModel sum2 = expr().sumOf().expr(plus).model();
	final ExpressionModel sum3 = expr().sumOf().prop("selfProperty.collectional.collectional.moneyProperty").model();
	final ExpressionModel avg = expr().avgOf().expr(sum3).model();
	final ExpressionModel model = expr().expr(sum2).add().expr(avg).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_23() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "SUM(SUM(collectional.collectional.moneyProperty) + entityProperty.collectional.moneyProperty)");
	try {
	    ev.convert();
	    fail("Should have failed due to incorrect tag.");
	} catch (final IncompatibleOperandException ex) {
	    assertEquals("Incorrect message", "Incompatible operand context for operation '+': 'entityProperty.collectional' is not compatible with 'collectional'.", ex.getMessage());
	}
    }

    @Test
    public void test_case_24() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "SUM(SUM(collectional.collectional.moneyProperty) + collectional.intProperty) / " +
			"AVG(SUM(selfProperty.collectional.collectional.moneyProperty))");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", BigDecimal.class, root.getType());
	assertEquals("Incorrect expression tag", THIS, root.getTag());

	final ExpressionModel sum1 = expr().sumOf().prop("collectional.collectional.moneyProperty").model();
	final ExpressionModel plus = expr().expr(sum1).add().prop("collectional.intProperty").model();
	final ExpressionModel sum2 = expr().sumOf().expr(plus).model();
	final ExpressionModel sum3 = expr().sumOf().prop("selfProperty.collectional.collectional.moneyProperty").model();
	final ExpressionModel avg = expr().avgOf().expr(sum3).model();
	final ExpressionModel model = expr().expr(sum2).div().expr(avg).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_25() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "collectional.collectional.moneyProperty + entityProperty.collectional.moneyProperty");
	try {
	    ev.convert();
	    fail("Should have failed due to incorrect tag.");
	} catch (final IncompatibleOperandException ex) {
	    assertEquals("Incorrect message", "Incompatible operand nesting level for operands of operation '+'.", ex.getMessage());
	}
    }

    @Test
    public void test_case_26() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "AVG(SUM(collectional.collectional.moneyProperty) + entityProperty.collectional.moneyProperty)");
	try {
	    ev.convert();
	    fail("Should have failed due to incorrect tag.");
	} catch (final IncompatibleOperandException ex) {
	    assertEquals("Incorrect message", "Incompatible operand context for operation '+': 'entityProperty.collectional' is not compatible with 'collectional'.", ex.getMessage());
	}
    }

    @Test
    public void test_case_27() throws RecognitionException, SemanticException {
	final String expressionText = "SUM(collectional.collectional.moneyProperty) + entityProperty.collectional.moneyProperty";
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, expressionText);
	try {
	    ev.convert();
	    fail("Should have failed due to incorrect tag.");
	} catch (final IncompatibleOperandException ex) {
	    assertEquals("Incorrect message", "Resultant expression level is incompatible with the context.", ex.getMessage());
	    assertEquals("Incorrect operand in error.", "+", expressionText.substring(ex.token().beginIndex, ex.token().endIndex).trim());
	}
    }

    @Test
    public void test_case_28() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "AVG(SUM(collectional.collectional.moneyProperty)) + AVG(entityProperty.collectional.moneyProperty)");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Money.class, root.getType());
	assertEquals("Incorrect expression tag", THIS, root.getTag());

	final ExpressionModel sum1 = expr().sumOf().prop("collectional.collectional.moneyProperty").model();
	final ExpressionModel avg1 = expr().avgOf().expr(sum1).model();
	final ExpressionModel avg2 = expr().avgOf().prop("entityProperty.collectional.moneyProperty").model();
	final ExpressionModel model = expr().expr(avg1).add().expr(avg2).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }


}
