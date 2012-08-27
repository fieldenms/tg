package ua.com.fielden.platform.expression;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.expression.ast.visitor.CollectionalContextVisitor.SUPER;
import static ua.com.fielden.platform.expression.ast.visitor.CollectionalContextVisitor.THIS;

import java.math.BigDecimal;

import org.junit.Test;

import ua.com.fielden.platform.associations.one2many.DetailsEntityForOneToManyAssociation;
import ua.com.fielden.platform.associations.one2many.MasterEntityWithOneToManyAssociation;
import ua.com.fielden.platform.associations.one2one.MasterEntityWithOneToOneAssociation;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.expression.ast.AstNode;
import ua.com.fielden.platform.expression.ast.visitor.entities.EntityLevel1;
import ua.com.fielden.platform.expression.exception.RecognitionException;
import ua.com.fielden.platform.expression.exception.semantic.IncompatibleOperandException;
import ua.com.fielden.platform.expression.exception.semantic.SemanticException;
import ua.com.fielden.platform.types.Money;

public class ExpressionText2ModelConverter4FunctionsMinMaxTest {

    @Test
    public void test_case_01() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "(MAX(collectional.intProperty) + intProperty + decimalProperty + moneyProperty) * 2");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Money.class, root.getType());

	final ExpressionModel max = expr().maxOf().prop("collectional.intProperty").model();
	final ExpressionModel plus3 = expr().prop("decimalProperty").add().prop("moneyProperty").model();
	final ExpressionModel plus2 = expr().prop("intProperty").add().expr(plus3).model();
	final ExpressionModel plus1 = expr().expr(max).add().expr(plus2).model();
	final ExpressionModel model = expr().expr(plus1).mult().val(2).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_02() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "(MIN(collectional.intProperty) + intProperty + decimalProperty + moneyProperty) * 2");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Money.class, root.getType());

	final ExpressionModel min = expr().minOf().prop("collectional.intProperty").model();
	final ExpressionModel plus3 = expr().prop("decimalProperty").add().prop("moneyProperty").model();
	final ExpressionModel plus2 = expr().prop("intProperty").add().expr(plus3).model();
	final ExpressionModel plus1 = expr().expr(min).add().expr(plus2).model();
	final ExpressionModel model = expr().expr(plus1).mult().val(2).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_03() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "(MAX(collectional.intProperty) + intProperty) * 2");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Integer.class, root.getType());

	final ExpressionModel max = expr().maxOf().prop("collectional.intProperty").model();
	final ExpressionModel plus1 = expr().expr(max).add().prop("intProperty").model();
	final ExpressionModel model = expr().expr(plus1).mult().val(2).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_04() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "(MIN(collectional.intProperty) + intProperty) * 2");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Integer.class, root.getType());

	final ExpressionModel min = expr().minOf().prop("collectional.intProperty").model();
	final ExpressionModel plus1 = expr().expr(min).add().prop("intProperty").model();
	final ExpressionModel model = expr().expr(plus1).mult().val(2).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_05() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "intProperty * intProperty / MAX(collectional.intProperty)");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", BigDecimal.class, root.getType());

	final ExpressionModel max = expr().maxOf().prop("collectional.intProperty").model();
	final ExpressionModel mult = expr().prop("intProperty").mult().prop("intProperty").model();
	final ExpressionModel model = expr().expr(mult).div().expr(max).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_06() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "intProperty * intProperty / MIN(collectional.intProperty)");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", BigDecimal.class, root.getType());

	final ExpressionModel min = expr().minOf().prop("collectional.intProperty").model();
	final ExpressionModel mult = expr().prop("intProperty").mult().prop("intProperty").model();
	final ExpressionModel model = expr().expr(mult).div().expr(min).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_07() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "intProperty * intProperty / (MAX(collectional.intProperty) * intProperty)");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", BigDecimal.class, root.getType());

	final ExpressionModel max = expr().maxOf().prop("collectional.intProperty").model();
	final ExpressionModel mult2 = expr().expr(max).mult().prop("intProperty").model();
	final ExpressionModel mult1 = expr().prop("intProperty").mult().prop("intProperty").model();
	final ExpressionModel model = expr().expr(mult1).div().expr(mult2).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_08() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "intProperty * intProperty / (MIN(collectional.intProperty) * intProperty)");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", BigDecimal.class, root.getType());

	final ExpressionModel min = expr().minOf().prop("collectional.intProperty").model();
	final ExpressionModel mult2 = expr().expr(min).mult().prop("intProperty").model();
	final ExpressionModel mult1 = expr().prop("intProperty").mult().prop("intProperty").model();
	final ExpressionModel model = expr().expr(mult1).div().expr(mult2).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_09() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "entityProperty.intProperty * intProperty / (MAX(collectional.intProperty) * intProperty)");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", BigDecimal.class, root.getType());

	final ExpressionModel max = expr().maxOf().prop("collectional.intProperty").model();
	final ExpressionModel mult2 = expr().expr(max).mult().prop("intProperty").model();
	final ExpressionModel mult1 = expr().prop("entityProperty.intProperty").mult().prop("intProperty").model();
	final ExpressionModel model = expr().expr(mult1).div().expr(mult2).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_10() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "entityProperty.intProperty * intProperty / (MIN(collectional.intProperty) * intProperty)");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", BigDecimal.class, root.getType());

	final ExpressionModel min = expr().minOf().prop("collectional.intProperty").model();
	final ExpressionModel mult2 = expr().expr(min).mult().prop("intProperty").model();
	final ExpressionModel mult1 = expr().prop("entityProperty.intProperty").mult().prop("intProperty").model();
	final ExpressionModel model = expr().expr(mult1).div().expr(mult2).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_11() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "entityProperty.intProperty * MAX(collectional.intProperty)");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Integer.class, root.getType());

	final ExpressionModel max = expr().maxOf().prop("collectional.intProperty").model();
	final ExpressionModel model = expr().prop("entityProperty.intProperty").mult().expr(max).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_12() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "entityProperty.intProperty * MIN(collectional.intProperty)");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Integer.class, root.getType());

	final ExpressionModel min = expr().minOf().prop("collectional.intProperty").model();
	final ExpressionModel model = expr().prop("entityProperty.intProperty").mult().expr(min).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_13() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "selfProperty.intProperty - MAX(entityProperty.collectional.moneyProperty)");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Money.class, root.getType());

	final ExpressionModel fun = expr().maxOf().prop("entityProperty.collectional.moneyProperty").model();
	final ExpressionModel model = expr().prop("selfProperty.intProperty").sub().expr(fun).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_14() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "selfProperty.intProperty - MIN(entityProperty.collectional.moneyProperty)");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Money.class, root.getType());

	final ExpressionModel fun = expr().minOf().prop("entityProperty.collectional.moneyProperty").model();
	final ExpressionModel model = expr().prop("selfProperty.intProperty").sub().expr(fun).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_15() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "MAX(entityProperty.collectional.moneyProperty) + MIN(collectional.intProperty)");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Money.class, root.getType());

	final ExpressionModel max = expr().maxOf().prop("entityProperty.collectional.moneyProperty").model();
	final ExpressionModel min = expr().minOf().prop("collectional.intProperty").model();
	final ExpressionModel model = expr().expr(max).add().expr(min).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_16() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "MIN(entityProperty.collectional.moneyProperty + moneyProperty) + SUM(collectional.intProperty)");
	try {
	    ev.convert();
	    fail("Should have failed due to incorrect tag.");
	} catch (final IncompatibleOperandException ex) {
	    assertEquals("Incorrect message", "Incompatible operand nesting level for operands of operation '+'.", ex.getMessage());
	}
    }

    @Test
    public void test_case_17() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "MAX(entityProperty.collectional.moneyProperty + moneyProperty) + SUM(collectional.intProperty)");
	try {
	    ev.convert();
	    fail("Should have failed due to incorrect tag.");
	} catch (final IncompatibleOperandException ex) {
	    assertEquals("Incorrect message", "Incompatible operand nesting level for operands of operation '+'.", ex.getMessage());
	}
    }

    @Test
    public void test_case_18() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "MIN(collectional.intProperty + selfProperty.collectional.intProperty)");
	try {
	    ev.convert();
	    fail("Should have failed due to incorrect tag.");
	} catch (final IncompatibleOperandException ex) {
	    assertEquals("Incorrect message", "Incompatible operand context for operation '+': 'selfProperty.collectional' is not compatible with 'collectional'.", ex.getMessage());
	}
    }

    @Test
    public void test_case_19() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "MAX(collectional.intProperty + selfProperty.collectional.intProperty)");
	try {
	    ev.convert();
	    fail("Should have failed due to incorrect tag.");
	} catch (final IncompatibleOperandException ex) {
	    assertEquals("Incorrect message", "Incompatible operand context for operation '+': 'selfProperty.collectional' is not compatible with 'collectional'.", ex.getMessage());
	}
    }

    @Test
    public void test_case_20() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "MIN(selfProperty.intProperty) + MAX(entityProperty.intProperty)");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Integer.class, root.getType());
	assertEquals("Incorrect expression tag", SUPER, root.getTag());

	final ExpressionModel min = expr().minOf().prop("selfProperty.intProperty").model();
	final ExpressionModel max = expr().maxOf().prop("entityProperty.intProperty").model();
	final ExpressionModel model = expr().expr(min).add().expr(max).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_21() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "MIN(selfProperty.collectional.intProperty) + MAX(entityProperty.collectional.moneyProperty)");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Money.class, root.getType());
	assertEquals("Incorrect expression tag", THIS, root.getTag());

	final ExpressionModel min = expr().minOf().prop("selfProperty.collectional.intProperty").model();
	final ExpressionModel max = expr().maxOf().prop("entityProperty.collectional.moneyProperty").model();
	final ExpressionModel model = expr().expr(min).add().expr(max).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_22() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "MIN(MAX(collectional.collectional.moneyProperty)) + intProperty");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Money.class, root.getType());
	assertEquals("Incorrect expression tag", THIS, root.getTag());

	final ExpressionModel max = expr().maxOf().prop("collectional.collectional.moneyProperty").model();
	final ExpressionModel min = expr().minOf().expr(max).model();
	final ExpressionModel model = expr().expr(min).add().prop("intProperty").model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_23() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "MAX(MIN(collectional.collectional.moneyProperty) + collectional.intProperty) + intProperty");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Money.class, root.getType());
	assertEquals("Incorrect expression tag", THIS, root.getTag());

	final ExpressionModel min = expr().minOf().prop("collectional.collectional.moneyProperty").model();
	final ExpressionModel plus = expr().expr(min).add().prop("collectional.intProperty").model();
	final ExpressionModel max = expr().maxOf().expr(plus).model();
	final ExpressionModel model = expr().expr(max).add().prop("intProperty").model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_24() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "MIN(MAX(collectional.collectional.moneyProperty) + intProperty)");
	try {
	    ev.convert();
	    fail("Should have failed due to incorrect tag.");
	} catch (final IncompatibleOperandException ex) {
	    assertEquals("Incorrect message", "Incompatible operand nesting level for operands of operation '+'.", ex.getMessage());
	}
    }

    @Test
    public void test_case_25() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "MIN(SUM(collectional.collectional.moneyProperty) + collectional.intProperty) + " +
		"AVG(MAX(selfProperty.collectional.collectional.moneyProperty))");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Money.class, root.getType());
	assertEquals("Incorrect expression tag", THIS, root.getTag());

	final ExpressionModel sum1 = expr().sumOf().prop("collectional.collectional.moneyProperty").model();
	final ExpressionModel plus = expr().expr(sum1).add().prop("collectional.intProperty").model();
	final ExpressionModel min = expr().minOf().expr(plus).model();
	final ExpressionModel max = expr().maxOf().prop("selfProperty.collectional.collectional.moneyProperty").model();
	final ExpressionModel avg = expr().avgOf().expr(max).model();
	final ExpressionModel model = expr().expr(min).add().expr(avg).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_26() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "MIN(MAX(collectional.collectional.moneyProperty) + entityProperty.collectional.moneyProperty)");
	try {
	    ev.convert();
	    fail("Should have failed due to incorrect tag.");
	} catch (final IncompatibleOperandException ex) {
	    assertEquals("Incorrect message", "Incompatible operand context for operation '+': 'entityProperty.collectional' is not compatible with 'collectional'.", ex.getMessage());
	}
    }

    @Test
    public void test_case_27() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "MIN(MAX(collectional.collectional.moneyProperty) + collectional.intProperty) / " +
		"AVG(SUM(selfProperty.collectional.collectional.moneyProperty))");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", BigDecimal.class, root.getType());
	assertEquals("Incorrect expression tag", THIS, root.getTag());

	final ExpressionModel max = expr().maxOf().prop("collectional.collectional.moneyProperty").model();
	final ExpressionModel plus = expr().expr(max).add().prop("collectional.intProperty").model();
	final ExpressionModel min = expr().minOf().expr(plus).model();
	final ExpressionModel sum3 = expr().sumOf().prop("selfProperty.collectional.collectional.moneyProperty").model();
	final ExpressionModel avg = expr().avgOf().expr(sum3).model();
	final ExpressionModel model = expr().expr(min).div().expr(avg).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_28() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "AVG(MAX(collectional.collectional.moneyProperty) + entityProperty.collectional.moneyProperty)");
	try {
	    ev.convert();
	    fail("Should have failed due to incorrect tag.");
	} catch (final IncompatibleOperandException ex) {
	    assertEquals("Incorrect message", "Incompatible operand context for operation '+': 'entityProperty.collectional' is not compatible with 'collectional'.", ex.getMessage());
	}
    }

    @Test
    public void test_case_29() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "AVG(MIN(collectional.collectional.moneyProperty) + entityProperty.collectional.moneyProperty)");
	try {
	    ev.convert();
	    fail("Should have failed due to incorrect tag.");
	} catch (final IncompatibleOperandException ex) {
	    assertEquals("Incorrect message", "Incompatible operand context for operation '+': 'entityProperty.collectional' is not compatible with 'collectional'.", ex.getMessage());
	}
    }

    @Test
    public void test_case_30() throws RecognitionException, SemanticException {
	final String expressionText = "MIN(collectional.collectional.moneyProperty) + entityProperty.collectional.moneyProperty";
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
    public void test_case_31() throws RecognitionException, SemanticException {
	final String expressionText = "MAX(collectional.collectional.moneyProperty) + entityProperty.collectional.moneyProperty";
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
    public void test_case_32() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "MIN(SUM(collectional.collectional.moneyProperty)) + AVG(entityProperty.collectional.moneyProperty)");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Money.class, root.getType());
	assertEquals("Incorrect expression tag", THIS, root.getTag());

	final ExpressionModel sum = expr().sumOf().prop("collectional.collectional.moneyProperty").model();
	final ExpressionModel min = expr().minOf().expr(sum).model();
	final ExpressionModel avg = expr().avgOf().prop("entityProperty.collectional.moneyProperty").model();
	final ExpressionModel model = expr().expr(min).add().expr(avg).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_33() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "MAX(SUM(collectional.collectional.moneyProperty)) + AVG(entityProperty.collectional.moneyProperty)");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Money.class, root.getType());
	assertEquals("Incorrect expression tag", THIS, root.getTag());

	final ExpressionModel sum = expr().sumOf().prop("collectional.collectional.moneyProperty").model();
	final ExpressionModel max = expr().maxOf().expr(sum).model();
	final ExpressionModel avg = expr().avgOf().prop("entityProperty.collectional.moneyProperty").model();
	final ExpressionModel model = expr().expr(max).add().expr(avg).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_34() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(MasterEntityWithOneToManyAssociation.class, "one2manyAssociationCollectional", "MIN(intProp - ←.moneyProp)");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Money.class, root.getType());
	assertEquals("Incorrect expression collectional context", THIS, root.getTag());

	final ExpressionModel minus = expr().prop("intProp").sub().prop("key1.moneyProp").model();
	final ExpressionModel model = expr().minOf().expr(minus).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_35() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter( //
		MasterEntityWithOneToManyAssociation.class, // higher-order type
		"one2manyAssociationCollectional", // expression context
		"intProp - ←.moneyProp + MAX(one2manyAssociationCollectional.moneyProp)");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Money.class, root.getType());
	assertEquals("Incorrect expression collectional context", "one2manyAssociationCollectional", root.getTag());

	final ExpressionModel minus = expr().prop("intProp").sub().prop("key1.moneyProp").model();
	final ExpressionModel max = expr().maxOf().prop("one2manyAssociationCollectional.moneyProp").model();
	final ExpressionModel plus = expr().expr(minus).add().expr(max).model();
	assertEquals("Incorrect model.", plus, root.getModel());
    }

    @Test
    public void test_case_36() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter( //
		MasterEntityWithOneToManyAssociation.class, // higher-order type
		"one2manyAssociationCollectional", // expression context
		"MIN(intProp - ←.moneyProp) + one2manyAssociationCollectional.moneyProp");
	try {
	    ev.convert();
	    fail("Conversion should have failed.");
	} catch (final IncompatibleOperandException ex) {
	    assertEquals("Incorrect error message.", "Incompatible operand nesting level for operands of operation '+'.", ex.getMessage());
	}
    }

    @Test
    public void test_case_37() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter( //
		MasterEntityWithOneToOneAssociation.class, // higher-order type
		"one2oneAssociation.one2ManyAssociation", // expression context
		"2 * decimalProp + ←.intProp");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", BigDecimal.class, root.getType());
	assertEquals("Incorrect expression collectional context", "one2oneAssociation.one2ManyAssociation", root.getTag());

	final ExpressionModel mult = expr().val(2).mult().prop("decimalProp").model();
	final ExpressionModel plus = expr().expr(mult).add().prop("key1.intProp").model();
	assertEquals("Incorrect model.", plus, root.getModel());
    }

    @Test
    public void test_level_calculation_integrity_1() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter( //
		MasterEntityWithOneToOneAssociation.class, // higher-order type
		"one2oneAssociation.one2ManyAssociation", // expression context
		"←.intProp + 2 * decimalProp");
	final AstNode root = ev.convert();
	assertEquals(Integer.valueOf(2), root.getLevel());
    }

    @Test
    public void test_level_calculation_integrity_2() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter( //
		MasterEntityWithOneToOneAssociation.class, // higher-order type
		"one2oneAssociation.one2ManyAssociation", // expression context
		"2 * decimalProp + ←.intProp");
	final AstNode root = ev.convert();
	assertEquals(Integer.valueOf(2), root.getLevel());
    }


    @Test
    public void conversion_of_expression_with_property_not_reachable_from_context_should_have_failed() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter( //
		EntityLevel1.class, // higher-order type
		"entityProperty", // expression context
		"intProperty - ←.intProperty");
	try {
	    ev.convert();
	    fail("Should have failed due to incorrect association");
	} catch (final Exception ex) {
	    assertEquals("Non-collectional property entityProperty in type ua.com.fielden.platform.expression.ast.visitor.entities.EntityLevel1 represents a Many-to-One association.", ex.getMessage());
	}
    }

    @Test
    public void calculated_property_should_be_able_to_contain_just_some_property_of_ordinary_type_only() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(MasterEntityWithOneToManyAssociation.class, "", "moneyProp");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Money.class, root.getType());

	final ExpressionModel model = expr().prop("moneyProp").model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void calculated_property_should_be_able_to_contain_just_some_property__of_non_ordinary_type_only() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(MasterEntityWithOneToManyAssociation.class, "", "one2manyAssociationSpecialCase");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", DetailsEntityForOneToManyAssociation.class, root.getType());

	final ExpressionModel model = expr().prop("one2manyAssociationSpecialCase").model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

}
