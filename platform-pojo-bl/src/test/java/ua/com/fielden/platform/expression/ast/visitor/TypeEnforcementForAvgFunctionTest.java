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
import ua.com.fielden.platform.expression.exception.semantic.TypeCompatibilityException;
import ua.com.fielden.platform.expression.exception.semantic.UnsupportedTypeException;
import ua.com.fielden.platform.expression.type.Day;
import ua.com.fielden.platform.expression.type.Month;
import ua.com.fielden.platform.expression.type.Year;
import ua.com.fielden.platform.types.Money;

public class TypeEnforcementForAvgFunctionTest {

    @Test
    public void test_avg_with_literals_case_1() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
	final Token[] tokens = new ExpressionLexer("AVG(2)").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
	try {
	    new AstWalker(ast, visitor).walk();
	} catch (final TypeCompatibilityException ex) {
	    assertEquals("Incorrect error message.", "Constant value is not applicable to aggregation functions.", ex.getMessage());
	}
    }

    @Test
    public void test_avg_with_literals_case_2() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
	final Token[] tokens = new ExpressionLexer("AVG(2.6)").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
	try {
	    new AstWalker(ast, visitor).walk();
	} catch (final TypeCompatibilityException ex) {
	    assertEquals("Incorrect error message.", "Constant value is not applicable to aggregation functions.", ex.getMessage());
	}
    }

    @Test
    public void test_avg_with_literals_case_3() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
	final Token[] tokens = new ExpressionLexer("AVG(\"hello\")").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
	try {
	    new AstWalker(ast, visitor).walk();
	} catch (final UnsupportedTypeException ex) {
	    assertEquals("Incorrect error message.", "Operand type " + String.class.getName() + " is not supported.", ex.getMessage());
	}
    }

    @Test
    public void test_avg_with_literals_case_4() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
	final Token[] tokens = new ExpressionLexer("AVG(1d)").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
	try {
	    new AstWalker(ast, visitor).walk();
	} catch (final UnsupportedTypeException ex) {
	    assertEquals("Incorrect error message.", "Operand type " + Day.class.getName() + " is not supported.", ex.getMessage());
	}
    }

    @Test
    public void test_avg_with_literals_case_5() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
	final Token[] tokens = new ExpressionLexer("AVG(3m)").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
	try {
	    new AstWalker(ast, visitor).walk();
	} catch (final UnsupportedTypeException ex) {
	    assertEquals("Incorrect error message.", "Operand type " + Month.class.getName() + " is not supported.", ex.getMessage());
	}
    }

    @Test
    public void test_avg_with_literals_case_6() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
	final Token[] tokens = new ExpressionLexer("AVG(3y)").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
	try {
	    new AstWalker(ast, visitor).walk();
	} catch (final UnsupportedTypeException ex) {
	    assertEquals("Incorrect error message.", "Operand type " + Year.class.getName() + " is not supported.", ex.getMessage());
	}
    }

    @Test
    public void test_avg_with_constant_expression() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
	final Token[] tokens = new ExpressionLexer("AVG(2.6 + 6 / 2 - 4 * 7)").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
	try {
	    new AstWalker(ast, visitor).walk();
	} catch (final TypeCompatibilityException ex) {
	    assertEquals("Incorrect error message.", "Constant value is not applicable to aggregation functions.", ex.getMessage());
	}
    }


    @Test
    public void test_avg_int_property() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
	final Token[] tokens = new ExpressionLexer("AVG(intProperty)").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
	new AstWalker(ast, visitor).walk();
	assertEquals("Incorrect type.", BigDecimal.class, ast.getType());
	assertNull("Incorrect value.", ast.getValue());
    }

    @Test
    public void test_avg_decimal_property() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
	final Token[] tokens = new ExpressionLexer("AVG(decimalProperty)").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
	new AstWalker(ast, visitor).walk();
	assertEquals("Incorrect type.", BigDecimal.class, ast.getType());
	assertNull("Incorrect value.", ast.getValue());
    }

    @Test
    public void test_avg_money_property() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
	final Token[] tokens = new ExpressionLexer("AVG(moneyProperty)").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
	new AstWalker(ast, visitor).walk();
	assertEquals("Incorrect type.", Money.class, ast.getType());
	assertNull("Incorrect value.", ast.getValue());
    }

    @Test
    public void test_avg_string_property() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
	final Token[] tokens = new ExpressionLexer("AVG(strProperty)").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
	try {
	    new AstWalker(ast, visitor).walk();
	} catch (final UnsupportedTypeException ex) {
	    assertEquals("Incorrect error message.", "Operand type " + String.class.getName() + " is not supported.", ex.getMessage());
	}
    }

    @Test
    public void test_avg_date_property() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
	final Token[] tokens = new ExpressionLexer("AVG(dateProperty)").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
	try {
	    new AstWalker(ast, visitor).walk();
	} catch (final UnsupportedTypeException ex) {
	    assertEquals("Incorrect error message.", "Operand type " + Date.class.getName() + " is not supported.", ex.getMessage());
	}
    }


    @Test
    public void test_complex_expression_with_multple_parties_1() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
	final Token[] tokens = new ExpressionLexer("AVG(2 / (moneyProperty / decimalProperty) / 3.5)").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
	new AstWalker(ast, visitor).walk();
	assertEquals("Incorrect type.", Money.class, ast.getType());
	assertNull("Incorrect value.", ast.getValue());
    }

    @Test
    public void test_complex_expression_with_multple_parties_2() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
	final Token[] tokens = new ExpressionLexer("2.5 / (intProperty / AVG(decimalProperty)) / 35").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
	new AstWalker(ast, visitor).walk();
	assertEquals("Incorrect type.", BigDecimal.class, ast.getType());
	assertNull("Incorrect value.", ast.getValue());
    }

    @Test
    public void test_complex_expression_with_multple_parties_3() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
	final Token[] tokens = new ExpressionLexer("2.5 / AVG(intProperty / moneyProperty) / 35").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
	new AstWalker(ast, visitor).walk();
	assertEquals("Incorrect type.", Money.class, ast.getType());
	assertNull("Incorrect value.", ast.getValue());
    }

}
