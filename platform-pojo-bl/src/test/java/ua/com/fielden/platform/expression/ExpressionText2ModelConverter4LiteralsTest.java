package ua.com.fielden.platform.expression;

import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;

import java.math.BigDecimal;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.expression.ast.AstNode;
import ua.com.fielden.platform.expression.ast.visitor.entities.EntityLevel1;
import ua.com.fielden.platform.expression.exception.RecognitionException;
import ua.com.fielden.platform.expression.exception.semantic.SemanticException;

public class ExpressionText2ModelConverter4LiteralsTest {

    @Test
    public void expression_should_be_recognised_as_valmodel_for_string() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "\"string\"");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", String.class, root.getType());

	final ExpressionModel model = expr().val("string").model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void expression_should_be_recognised_as_valmodel_for_empty_string() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "\"\"");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", String.class, root.getType());

	final ExpressionModel model = expr().val("").model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void expression_should_be_recognised_as_valmodel_for_int() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "22");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", Integer.class, root.getType());

	final ExpressionModel model = expr().val(22).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

    @Test
    public void expression_should_be_recognised_as_valmodel_for_decimal() throws RecognitionException, SemanticException {
	final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "22.2");
	final AstNode root = ev.convert();
	assertEquals("Incorrect expression type", BigDecimal.class, root.getType());

	final ExpressionModel model = expr().val(new BigDecimal("22.2")).model();
	assertEquals("Incorrect model.", model, root.getModel());
    }

}
