package ua.com.fielden.platform.expression;

import org.junit.Test;
import ua.com.fielden.platform.expression.ast.AstNode;
import ua.com.fielden.platform.expression.ast.visitor.entities.EntityLevel1;
import ua.com.fielden.platform.expression.exception.RecognitionException;
import ua.com.fielden.platform.expression.exception.semantic.SemanticException;
import ua.com.fielden.platform.expression.exception.semantic.UnsupportedTypeException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;

public class ExpressionBooleanComparisonTest {

    @Test
    public void boolean_property_comparison_with_true_literal() throws Exception {
        final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "boolProperty = true");
        final AstNode root = ev.convert();
        assertEquals("Should be boolean type", boolean.class, root.getType());
    }

    @Test
    public void boolean_property_comparison_with_false_literal() throws Exception {
        final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "boolProperty = false");
        final AstNode root = ev.convert();
        assertEquals("Should be boolean type", boolean.class, root.getType());
    }

    @Test
    public void boolean_property_cannot_be_compared_to_NULL_using_equality() {
        {
            final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "boolProperty = NULL");
            assertThatThrownBy(ev::convert)
                    .isInstanceOf(UnsupportedTypeException.class)
                    .hasMessage("Operands for operation EQ should have compatible types [boolean, class ua.com.fielden.platform.expression.type.Null].");
        }

        {
            final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "NULL = boolProperty");
            assertThatThrownBy(ev::convert)
                    .isInstanceOf(UnsupportedTypeException.class)
                    .hasMessage("Operands for operation EQ should have compatible types [class ua.com.fielden.platform.expression.type.Null, boolean].");
        }
    }

    @Test
    public void boolean_property_cannot_be_compared_to_NULL_using_unequality() {
        {
            final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "boolProperty <> NULL");
            assertThatThrownBy(ev::convert)
                    .isInstanceOf(UnsupportedTypeException.class)
                    .hasMessage("Operands for operation NE should have compatible types [boolean, class ua.com.fielden.platform.expression.type.Null].");
        }

        {
            final ExpressionText2ModelConverter ev = new ExpressionText2ModelConverter(EntityLevel1.class, "NULL <> boolProperty");
            assertThatThrownBy(ev::convert)
                    .isInstanceOf(UnsupportedTypeException.class)
                    .hasMessage("Operands for operation NE should have compatible types [class ua.com.fielden.platform.expression.type.Null, boolean].");
        }
    }

}
