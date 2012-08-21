package ua.com.fielden.platform.expression;

import java.math.BigDecimal;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.expression.ast.AstNode;
import ua.com.fielden.platform.expression.ast.visitor.entities.EntityLevel1;
import ua.com.fielden.platform.expression.exception.RecognitionException;
import ua.com.fielden.platform.expression.exception.semantic.IncompatibleOperandException;
import ua.com.fielden.platform.expression.exception.semantic.SemanticException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.expression.ast.visitor.CollectionalContextVisitor.SUPER;
import static ua.com.fielden.platform.expression.ast.visitor.CollectionalContextVisitor.THIS;

public class ExpressionText2ModelConverter4UpperTest {

    @Test
    public void test_case_01() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "UPPER(strProperty) + strProperty");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", String.class, root.getType());

	final ExpressionModel func = expr().upperCase().prop("strProperty").model();
	final ExpressionModel model = expr().expr(func).add().prop("strProperty").model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_02() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "UPPER(MAX(collectional.strProperty)) + strProperty");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", String.class, root.getType());

	final ExpressionModel max = expr().maxOf().prop("collectional.strProperty").model();
	final ExpressionModel func = expr().upperCase().expr(max).model();
	final ExpressionModel model = expr().expr(func).add().prop("strProperty").model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_03() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "(COUNT(UPPER(collectional.strProperty)) + intProperty) * 2");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Integer.class, root.getType());

	final ExpressionModel fun = expr().upperCase().prop("collectional.strProperty").model();
	final ExpressionModel countOfDays = expr().countOfDistinct().expr(fun).model();
	final ExpressionModel plus1 = expr().expr(countOfDays).add().prop("intProperty").model();
	final ExpressionModel model = expr().expr(plus1).mult().val(2).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_04() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "intProperty * intProperty / COUNT(UPPER(collectional.strProperty))");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", BigDecimal.class, root.getType());

	final ExpressionModel fun = expr().upperCase().prop("collectional.strProperty").model();
	final ExpressionModel count = expr().countOfDistinct().expr(fun).model();
	final ExpressionModel mult = expr().prop("intProperty").mult().prop("intProperty").model();
	final ExpressionModel model = expr().expr(mult).div().expr(count).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_05() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "entityProperty.intProperty * COUNT(UPPER(collectional.strProperty))");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Integer.class, root.getType());

	final ExpressionModel func = expr().upperCase().prop("collectional.strProperty").model();
	final ExpressionModel count = expr().countOfDistinct().expr(func).model();
	final ExpressionModel model = expr().prop("entityProperty.intProperty").mult().expr(count).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_06() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "UPPER(MAX(entityProperty.collectional.strProperty)) + MAX(collectional.strProperty)");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", String.class, root.getType());

	final ExpressionModel max1 = expr().maxOf().prop("entityProperty.collectional.strProperty").model();
	final ExpressionModel fun = expr().upperCase().expr(max1).model();
	final ExpressionModel max2 = expr().maxOf().prop("collectional.strProperty").model();
	final ExpressionModel model = expr().expr(fun).add().expr(max2).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_07() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "COUNT(UPPER(collectional.strProperty) + UPPER(selfProperty.collectional.dateProperty))");
	try {
	    ev.convert();
	    fail("Should have failed due to incorrect tag.");
	} catch (final IncompatibleOperandException ex) {
	    assertEquals("Incorrect message", "Incompatible operand context for operation '+': 'selfProperty.collectional' is not compatible with 'collectional'.", ex.getMessage());
	}
    }

    @Test
    public void test_case_08() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "COUNT(UPPER(selfProperty.strProperty) + UPPER(entityProperty.strProperty))");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Integer.class, root.getType());
	assertEquals("Incorrect expression tag", SUPER, root.getTag());

	final ExpressionModel func1 = expr().upperCase().prop("selfProperty.strProperty").model();
	final ExpressionModel func2 = expr().upperCase().prop("entityProperty.strProperty").model();
	final ExpressionModel plus = expr().expr(func1).add().expr(func2).model();
	final ExpressionModel model = expr().countOfDistinct().expr(plus).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_09() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "COUNT(UPPER(selfProperty.collectional.strProperty)) + COUNT(UPPER(entityProperty.collectional.strProperty))");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Integer.class, root.getType());
	assertEquals("Incorrect expression tag", THIS, root.getTag());

	final ExpressionModel fun1 = expr().upperCase().prop("selfProperty.collectional.strProperty").model();
	final ExpressionModel count1 = expr().countOfDistinct().expr(fun1).model();
	final ExpressionModel fun2 = expr().upperCase().prop("entityProperty.collectional.strProperty").model();
	final ExpressionModel count2 = expr().countOfDistinct().expr(fun2).model();
	final ExpressionModel model = expr().expr(count1).add().expr(count2).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

}
