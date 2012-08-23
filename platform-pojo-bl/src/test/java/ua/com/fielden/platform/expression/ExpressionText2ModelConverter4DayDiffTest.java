package ua.com.fielden.platform.expression;

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

public class ExpressionText2ModelConverter4DayDiffTest {

    @Test
    public void test_case_01() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "DAY_DIFF(dateProperty, dateProperty) + intProperty");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Integer.class, root.getType());

	final ExpressionModel func = expr().count().days().between().prop("dateProperty").and().prop("dateProperty").model();
	final ExpressionModel model = expr().expr(func).add().prop("intProperty").model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_02() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "DAY_DIFF(MAX(collectional.dateProperty), dateProperty)");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Integer.class, root.getType());

	final ExpressionModel max = expr().maxOf().prop("collectional.dateProperty").model();
	final ExpressionModel model = expr().count().days().between().expr(max).and().prop("dateProperty").model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_03() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "(COUNT(DAY_DIFF(MAX(collectional.dateProperty), dateProperty)) + SUM(intProperty)) * 2");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Integer.class, root.getType());

	final ExpressionModel max = expr().maxOf().prop("collectional.dateProperty").model();
	final ExpressionModel fun = expr().count().days().between().expr(max).and().prop("dateProperty").model();
	final ExpressionModel countOfDays = expr().countOfDistinct().expr(fun).model();
	final ExpressionModel sum = expr().sumOf().prop("intProperty").model();
	final ExpressionModel plus1 = expr().expr(countOfDays).add().expr(sum).model();
	final ExpressionModel model = expr().expr(plus1).mult().val(2).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void test_case_04() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "COUNT(DAY_DIFF(collectional.dateProperty, selfProperty.collectional.dateProperty))");
	try {
	    ev.convert();
	    fail("Should have failed due to incorrect tag.");
	} catch (final IncompatibleOperandException ex) {
	    assertEquals("Incorrect message", "Incompatible operand context for operation 'DAY_DIFF': 'selfProperty.collectional' is not compatible with 'collectional'.", ex.getMessage());
	}
    }
}
