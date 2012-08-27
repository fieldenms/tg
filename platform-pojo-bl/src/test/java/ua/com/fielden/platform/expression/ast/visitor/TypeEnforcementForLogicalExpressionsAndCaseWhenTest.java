package ua.com.fielden.platform.expression.ast.visitor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

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

public class TypeEnforcementForLogicalExpressionsAndCaseWhenTest {

    @Test
    public void test_type_determination_for_logical_expression_case1() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
	final Token[] tokens = new ExpressionLexer("SUM(intProperty) > AVG(intProperty) * 10").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
	new AstWalker(ast, visitor).walk();
	assertEquals("Incorrect type.", boolean.class, ast.getType());
	assertNull("Incorrect value.", ast.getValue());
    }

    @Test
    public void test_type_determination_for_logical_expression_case2() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
	final Token[] tokens = new ExpressionLexer("CASE WHEN AVG(decimalProperty) <> 100 THEN \"word\" ELSE \"word\" END").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
	new AstWalker(ast, visitor).walk();
	assertEquals("Incorrect type.", String.class, ast.getType());
	assertNull("Incorrect value.", ast.getValue());
    }

    @Test
    public void test_type_determination_for_logical_expression_case3() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
	final Token[] tokens = new ExpressionLexer("AVG(moneyProperty) < 12").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
	new AstWalker(ast, visitor).walk();
	assertEquals("Incorrect type.", boolean.class, ast.getType());
	assertNull("Incorrect value.", ast.getValue());
    }

    @Test
    public void test_type_determination_for_logical_expression_case4() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
	final Token[] tokens = new ExpressionLexer("12.4 >= AVG(moneyProperty)").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
	new AstWalker(ast, visitor).walk();
	assertEquals("Incorrect type.", boolean.class, ast.getType());
	assertNull("Incorrect value.", ast.getValue());
    }

    @Test
    public void test_type_determination_for_logical_expression_case5() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
	final Token[] tokens = new ExpressionLexer("AVG(2 / (moneyProperty / decimalProperty) / 3.5) > moneyProperty || (23 > 5)").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
	new AstWalker(ast, visitor).walk();
	assertEquals("Incorrect type.", boolean.class, ast.getType());
	assertNull("Incorrect value.", ast.getValue());
    }

    @Test
    public void test_type_determination_for_logical_expression_case6() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
	final Token[] tokens = new ExpressionLexer("AVG(2 / (moneyProperty / decimalProperty) / 3.5) > moneyProperty || (\"word\" > 5)").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	final TypeEnforcementVisitor visitor = new TypeEnforcementVisitor(EntityLevel1.class);
	try {
	    new AstWalker(ast, visitor).walk();
	    fail("Should have detected incompatible operand types.");
	} catch (final Exception ex) {

	}
    }

}
