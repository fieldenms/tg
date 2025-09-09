package ua.com.fielden.platform.expression;

import org.junit.Test;
import ua.com.fielden.platform.expression.ast.AstNode;
import ua.com.fielden.platform.expression.ast.visitor.entities.EntityLevel1;
import ua.com.fielden.platform.expression.exception.RecognitionException;
import ua.com.fielden.platform.expression.exception.semantic.SemanticException;

import static org.junit.Assert.assertEquals;

public class ExpressionBooleanComparisonTest {

    @Test
    public void boolean_property_comparison_with_true_literal() throws RecognitionException, SemanticException {
        final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "boolProperty = true");
        final AstNode root = ev.convert();
        assertEquals("Should be boolean type", boolean.class, root.getType());
    }

    @Test
    public void boolean_property_comparison_with_false_literal() throws RecognitionException, SemanticException {
        final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "boolProperty = false");
        final AstNode root = ev.convert();
        assertEquals("Should be boolean type", boolean.class, root.getType());
    }

}