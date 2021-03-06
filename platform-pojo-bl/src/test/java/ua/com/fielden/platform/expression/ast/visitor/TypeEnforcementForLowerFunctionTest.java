package ua.com.fielden.platform.expression.ast.visitor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.Test;

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

public class TypeEnforcementForLowerFunctionTest {

    @Test
    public void test_lower_with_literals_case_1() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("LOWER(2)").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
        try {
            new AstWalker(ast, visitor).walk();
        } catch (final UnsupportedTypeException ex) {
            assertEquals("Incorrect offending type.", Integer.class, ex.getOffendingType());
        }
    }

    @Test
    public void test_lower_with_literals_case_2() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("LOWER(2.6)").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
        try {
            new AstWalker(ast, visitor).walk();
        } catch (final UnsupportedTypeException ex) {
            assertEquals("Incorrect offending type.", BigDecimal.class, ex.getOffendingType());
        }
    }

    @Test
    public void test_lower_with_literals_case_3() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("LOWER(\"hello\")").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
        try {
            new AstWalker(ast, visitor).walk();
        } catch (final UnsupportedTypeException ex) {
            assertEquals("Incorrect offending type.", String.class, ex.getOffendingType());
        }
    }

    @Test
    public void test_lower_with_literals_case_4() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("LOWER(1d)").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
        try {
            new AstWalker(ast, visitor).walk();
        } catch (final UnsupportedTypeException ex) {
            assertEquals("Incorrect offending type.", Day.class, ex.getOffendingType());
        }
    }

    @Test
    public void test_lower_with_literals_case_5() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("LOWER(3m)").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
        try {
            new AstWalker(ast, visitor).walk();
        } catch (final UnsupportedTypeException ex) {
            assertEquals("Incorrect offending type.", Month.class, ex.getOffendingType());
        }
    }

    @Test
    public void test_lower_with_literals_case_6() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("LOWER(3y)").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
        try {
            new AstWalker(ast, visitor).walk();
        } catch (final UnsupportedTypeException ex) {
            assertEquals("Incorrect offending type.", Year.class, ex.getOffendingType());
        }
    }

    @Test
    public void test_lower_with_constant_expression() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("LOWER(2.6 + 6 / 2 - 4 * 7)").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
        try {
            new AstWalker(ast, visitor).walk();
        } catch (final UnsupportedTypeException ex) {
            assertEquals("Incorrect offending type.", BigDecimal.class, ex.getOffendingType());
        }
    }

    @Test
    public void test_lower_int_property() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("LOWER(intProperty)").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
        try {
            new AstWalker(ast, visitor).walk();
        } catch (final UnsupportedTypeException ex) {
            assertEquals("Incorrect offending type.", Integer.class, ex.getOffendingType());
        }
    }

    @Test
    public void test_lower_decimal_property() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("LOWER(decimalProperty)").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
        try {
            new AstWalker(ast, visitor).walk();
        } catch (final UnsupportedTypeException ex) {
            assertEquals("Incorrect offending type.", BigDecimal.class, ex.getOffendingType());
        }
    }

    @Test
    public void test_lower_money_property() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("LOWER(moneyProperty)").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
        try {
            new AstWalker(ast, visitor).walk();
        } catch (final UnsupportedTypeException ex) {
            assertEquals("Incorrect offending type.", Money.class, ex.getOffendingType());
        }
    }

    @Test
    public void test_lower_date_property() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("LOWER(dateProperty)").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
        try {
            new AstWalker(ast, visitor).walk();
        } catch (final UnsupportedTypeException ex) {
            assertEquals("Incorrect offending type.", Date.class, ex.getOffendingType());
        }
    }

    @Test
    public void test_lower_string_property() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("LOWER(strProperty)").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
        new AstWalker(ast, visitor).walk();
        assertEquals("Incorrect type.", String.class, ast.getType());
        assertNull("Incorrect value.", ast.getValue());
    }

    @Test
    public void test_complex_expression_with_multple_parties_1() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("LOWER(strProperty  + \"string value\")").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
        new AstWalker(ast, visitor).walk();
        assertEquals("Incorrect type.", String.class, ast.getType());
        assertNull("Incorrect value.", ast.getValue());
    }

    @Test
    public void test_complex_expression_with_multple_parties_2() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
        final Token[] tokens = new ExpressionLexer("\"string value\" + (LOWER(strProperty)) + strProperty").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
        new AstWalker(ast, visitor).walk();
        assertEquals("Incorrect type.", String.class, ast.getType());
        assertNull("Incorrect value.", ast.getValue());
    }

}
