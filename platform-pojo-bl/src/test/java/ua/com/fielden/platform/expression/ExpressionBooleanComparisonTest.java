package ua.com.fielden.platform.expression;

import org.junit.Test;
import ua.com.fielden.platform.expression.ast.AstNode;
import ua.com.fielden.platform.expression.ast.visitor.entities.EntityLevel1;
import ua.com.fielden.platform.expression.exception.RecognitionException;
import ua.com.fielden.platform.expression.exception.semantic.SemanticException;
import ua.com.fielden.platform.expression.exception.semantic.UnsupportedTypeException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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

    @Test
    public void boolean_property_cannot_be_equal_to_NULL() throws RecognitionException, SemanticException {
        try {
            final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "boolProperty = NULL");
            final AstNode root = ev.convert();
            fail();
        } catch (final UnsupportedTypeException ex) {
            assertEquals("Operands for operation EQ should have compatible types [boolean, class ua.com.fielden.platform.expression.type.Null].", ex.getMessage());
        }

        try {
            final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "NULL = boolProperty");
            final AstNode root = ev.convert();
            fail();
        } catch (final UnsupportedTypeException ex) {
            assertEquals("Operands for operation EQ should have compatible types [class ua.com.fielden.platform.expression.type.Null, boolean].", ex.getMessage());
        }
    }

    @Test
    public void boolean_property_cannot_be_not_equal_to_NULL() throws RecognitionException, SemanticException {
        try {
            final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "boolProperty <> NULL");
            final AstNode root = ev.convert();
            fail();
        } catch (final UnsupportedTypeException ex) {
            assertEquals("Operands for operation NE should have compatible types [boolean, class ua.com.fielden.platform.expression.type.Null].", ex.getMessage());
        }

        try {
            final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "NULL <> boolProperty");
            final AstNode root = ev.convert();
            fail();
        } catch (final UnsupportedTypeException ex) {
            assertEquals("Operands for operation NE should have compatible types [class ua.com.fielden.platform.expression.type.Null, boolean].", ex.getMessage());
        }
    }


}