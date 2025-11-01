package ua.com.fielden.platform.expression;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ua.com.fielden.platform.expression.ast.AstNode;
import ua.com.fielden.platform.expression.ast.visitor.entities.EntityLevel1;
import ua.com.fielden.platform.expression.exception.RecognitionException;
import ua.com.fielden.platform.expression.exception.semantic.SemanticException;

public class ExpressionSimpleLiteralTest {

    @Test
    public void integer_literal_recognition() throws RecognitionException, SemanticException {
        final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "42");
        final AstNode root = ev.convert();
        assertEquals(Integer.class, root.getType());
        assertEquals(42, root.getValue());
    }

    @Test
    public void string_literal_recognition() throws RecognitionException, SemanticException {
        final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "\"hello\"");
        final AstNode root = ev.convert();
        assertEquals(String.class, root.getType());
        assertEquals("hello", root.getValue());
    }

    @Test
    public void boolean_literal_true_is_recognised() throws RecognitionException, SemanticException {
        final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "true");
        final AstNode root = ev.convert();
        assertEquals(boolean.class, root.getType());
        assertEquals(true, root.getValue());
    }

    @Test
    public void boolean_literal_True_is_recognised() throws RecognitionException, SemanticException {
        final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "True");
        final AstNode root = ev.convert();
        assertEquals(boolean.class, root.getType());
        assertEquals(true, root.getValue());
    }

    @Test
    public void boolean_literal_false_is_recognised() throws RecognitionException, SemanticException {
        final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "false");
        final AstNode root = ev.convert();
        assertEquals(boolean.class, root.getType());
        assertEquals(false, root.getValue());
    }

    @Test
    public void boolean_literal_False_is_recognised() throws RecognitionException, SemanticException {
        final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "False");
        final AstNode root = ev.convert();
        assertEquals(boolean.class, root.getType());
        assertEquals(false, root.getValue());
    }

}