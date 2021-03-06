package ua.com.fielden.platform.expression.ast.visitor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.math.BigDecimal;

import org.junit.Test;

import ua.com.fielden.platform.expression.EgTokenCategory;
import ua.com.fielden.platform.expression.ExpressionLexer;
import ua.com.fielden.platform.expression.ExpressionParser;
import ua.com.fielden.platform.expression.Token;
import ua.com.fielden.platform.expression.ast.AstNode;
import ua.com.fielden.platform.expression.ast.AstWalker;
import ua.com.fielden.platform.expression.ast.visitor.entities.EntityLevel1;
import ua.com.fielden.platform.expression.automata.SequenceRecognitionFailed;
import ua.com.fielden.platform.expression.exception.RecognitionException;
import ua.com.fielden.platform.expression.exception.semantic.SemanticException;
import ua.com.fielden.platform.expression.exception.semantic.UnsupportedTypeException;
import ua.com.fielden.platform.expression.type.Day;
import ua.com.fielden.platform.expression.type.Month;
import ua.com.fielden.platform.expression.type.Year;
import ua.com.fielden.platform.types.Money;

public class TypeEnforcementForPlusOperationTest {

    @Test
    public void test_plus_operation_with_literals_case_1() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("1 + 2").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
        new AstWalker(ast, visitor).walk();
        assertEquals("Incorrect type.", Integer.class, ast.getType());
        assertEquals("Incorrect value.", Integer.valueOf(3), ast.getValue());
    }

    @Test
    public void test_plus_operation_with_literals_case_2() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("1 + 2.6").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
        new AstWalker(ast, visitor).walk();
        assertEquals("Incorrect type.", BigDecimal.class, ast.getType());
        assertEquals("Incorrect value.", new BigDecimal("3.6"), ast.getValue());
    }

    @Test
    public void test_plus_operation_with_literals_case_3() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("2.6 + 1").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
        new AstWalker(ast, visitor).walk();
        assertEquals("Incorrect type.", BigDecimal.class, ast.getType());
        assertEquals("Incorrect value.", new BigDecimal("3.6"), ast.getValue());
    }

    @Test
    public void test_plus_operation_with_literals_case_4() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("2.6 + 1.4").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
        new AstWalker(ast, visitor).walk();
        assertEquals("Incorrect type.", BigDecimal.class, ast.getType());
        assertEquals("Incorrect value.", new BigDecimal("4.0"), ast.getValue());
    }

    @Test
    public void test_plus_operation_with_literals_case_5() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("\"hello\" + \" world\"").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
        new AstWalker(ast, visitor).walk();
        assertEquals("Incorrect type.", String.class, ast.getType());
        assertEquals("Incorrect value.", "\"hello world\"", ast.getValue());
    }

    @Test
    public void test_plus_operation_with_literals_case_6() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("1d + 3d").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
        new AstWalker(ast, visitor).walk();
        assertEquals("Incorrect type.", Day.class, ast.getType());
        assertEquals("Incorrect value.", Integer.valueOf(4), ast.getValue());
    }

    @Test
    public void test_plus_operation_with_literals_case_7() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("3m + 2m").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
        new AstWalker(ast, visitor).walk();
        assertEquals("Incorrect type.", Month.class, ast.getType());
        assertEquals("Incorrect value.", Integer.valueOf(5), ast.getValue());
    }

    @Test
    public void test_plus_operation_with_literals_case_8() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("3y + 2y").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
        new AstWalker(ast, visitor).walk();
        assertEquals("Incorrect type.", Year.class, ast.getType());
        assertEquals("Incorrect value.", Integer.valueOf(5), ast.getValue());
    }

    @Test
    public void plus_operation_should_not_be_applicable_to_different_date_literal_types() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("3y + 2m").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
        try {
            new AstWalker(ast, visitor).walk();
            fail("plus operation with two differently typed date literals are not supported at this stage.");
        } catch (final Exception ex) {

        }
    }

    @Test
    public void plus_operation_should_not_be_applicable_to_date_literal_and_date_property() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("dateProperty + 1y").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
        try {
            new AstWalker(ast, visitor).walk();
            fail("plus operation applied to date property and a date literal are not supported at this stage.");
        } catch (final Exception ex) {

        }
    }

    @Test
    public void plus_operation_should_not_be_applicable_to_date_literal_and_int_property() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("intProperty + 1y").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
        try {
            new AstWalker(ast, visitor).walk();
            fail("plus operation applied to date property and a date literal are not supported at this stage.");
        } catch (final Exception ex) {

        }
    }

    @Test
    public void test_plus_operation_with_literals_case_9() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("2 + (2 + 6.5)").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
        new AstWalker(ast, visitor).walk();
        assertEquals("Incorrect type.", BigDecimal.class, ast.getType());
        assertEquals("Incorrect value.", new BigDecimal("10.5"), ast.getValue());
    }

    @Test
    public void test_plus_operation_with_literals_case_10() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("(2 + 2 + 1) + 5").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
        new AstWalker(ast, visitor).walk();
        assertEquals("Incorrect type.", Integer.class, ast.getType());
        assertEquals("Incorrect value.", Integer.valueOf("10"), ast.getValue());
    }

    @Test
    public void test_int_literal_plus_int_property() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("2 + intProperty").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
        new AstWalker(ast, visitor).walk();
        assertEquals("Incorrect type.", Integer.class, ast.getType());
        assertNull("Incorrect value.", ast.getValue());
    }

    @Test
    public void test_int_property_plus_int_literal() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("intProperty + 2").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
        new AstWalker(ast, visitor).walk();
        assertEquals("Incorrect type.", Integer.class, ast.getType());
        assertNull("Incorrect value.", ast.getValue());
    }

    @Test
    public void test_decimal_literal_plus_int_property() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("2.5 + intProperty").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
        new AstWalker(ast, visitor).walk();
        assertEquals("Incorrect type.", BigDecimal.class, ast.getType());
        assertNull("Incorrect value.", ast.getValue());
    }

    @Test
    public void test_int_property_plus_decimal_literal() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("intProperty + 2.5").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
        new AstWalker(ast, visitor).walk();
        assertEquals("Incorrect type.", BigDecimal.class, ast.getType());
        assertNull("Incorrect value.", ast.getValue());
    }

    @Test
    public void test_decimal_literal_plus_decimal_property() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("2.5 + decimalProperty").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
        new AstWalker(ast, visitor).walk();
        assertEquals("Incorrect type.", BigDecimal.class, ast.getType());
        assertNull("Incorrect value.", ast.getValue());
    }

    @Test
    public void test_decimal_property_plus_decimal_literal() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("decimalProperty + 2.5").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
        new AstWalker(ast, visitor).walk();
        assertEquals("Incorrect type.", BigDecimal.class, ast.getType());
        assertNull("Incorrect value.", ast.getValue());
    }

    @Test
    public void test_int_literal_plus_money_property() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("2 + moneyProperty").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
        new AstWalker(ast, visitor).walk();
        assertEquals("Incorrect type.", Money.class, ast.getType());
        assertNull("Incorrect value.", ast.getValue());
    }

    @Test
    public void test_money_property_plus_int_literal() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("moneyProperty + 2").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
        new AstWalker(ast, visitor).walk();
        assertEquals("Incorrect type.", Money.class, ast.getType());
        assertNull("Incorrect value.", ast.getValue());
    }

    @Test
    public void test_decimal_literal_plus_money_property() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("2.5 + moneyProperty").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
        new AstWalker(ast, visitor).walk();
        assertEquals("Incorrect type.", Money.class, ast.getType());
        assertNull("Incorrect value.", ast.getValue());
    }

    @Test
    public void test_money_property_plus_decimal_literal() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("moneyProperty + 2.5").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
        new AstWalker(ast, visitor).walk();
        assertEquals("Incorrect type.", Money.class, ast.getType());
        assertNull("Incorrect value.", ast.getValue());
    }

    @Test
    public void test_money_property_plus_money_property() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("moneyProperty + selfProperty.moneyProperty").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
        new AstWalker(ast, visitor).walk();
        assertEquals("Incorrect type.", Money.class, ast.getType());
        assertNull("Incorrect value.", ast.getValue());
    }

    @Test
    public void test_string_literal_plus_string_property() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("\"word\" + strProperty").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
        new AstWalker(ast, visitor).walk();
        assertEquals("Incorrect type.", String.class, ast.getType());
        assertNull("Incorrect value.", ast.getValue());
    }

    @Test
    public void test_string_property_plus_string_literal() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("strProperty + \"word\"").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
        new AstWalker(ast, visitor).walk();
        assertEquals("Incorrect type.", String.class, ast.getType());
        assertNull("Incorrect value.", ast.getValue());
    }

    @Test
    public void test_date_property_plus_date_property() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("dateProperty + dateProperty").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
        try {
            new AstWalker(ast, visitor).walk();
        } catch (final UnsupportedTypeException ex) {
            assertEquals("Incorrect error message.", "Left operand of date type is not applicable to operation " + EgTokenCategory.PLUS, ex.getMessage());
        }
    }

    @Test
    public void test_complex_expression_with_multple_parties_1() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("2 + (moneyProperty + decimalProperty) + 3.5").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
        new AstWalker(ast, visitor).walk();
        assertEquals("Incorrect type.", Money.class, ast.getType());
        assertNull("Incorrect value.", ast.getValue());
    }

    @Test
    public void test_complex_expression_with_multple_parties_2() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("2.5 + (intProperty + decimalProperty) + 35").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
        new AstWalker(ast, visitor).walk();
        assertEquals("Incorrect type.", BigDecimal.class, ast.getType());
        assertNull("Incorrect value.", ast.getValue());
    }

}
