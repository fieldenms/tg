package ua.com.fielden.platform.expression.ast;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ua.com.fielden.platform.expression.ExpressionLexer;
import ua.com.fielden.platform.expression.ExpressionParser;
import ua.com.fielden.platform.expression.Token;
import ua.com.fielden.platform.expression.ast.visitor.ExpressionToStringVisitor;
import ua.com.fielden.platform.expression.automata.SequenceRecognitionFailed;
import ua.com.fielden.platform.expression.exception.RecognitionException;
import ua.com.fielden.platform.expression.exception.semantic.SemanticException;

public class AstWakingTest {

    @Test
    public void test_post_order_walking_of_expression_with_single_application_of_rules_term_op_term() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
	final Token[] tokens = new ExpressionLexer("1 + 2").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	final ExpressionToStringVisitor visitor = new ExpressionToStringVisitor();
	new AstWalker(ast, visitor).walk();
	final String expression = visitor.expression();
	assertEquals("Incorrectly walked AST", "1 2 + ", expression);
    }

    @Test
    public void test_post_order_walking_of_expression_with_application_of_rules_term_op_term_op_term() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
	final Token[] tokens = new ExpressionLexer("1 + 2.3 + property").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	final ExpressionToStringVisitor visitor = new ExpressionToStringVisitor();
	new AstWalker(ast, visitor).walk();
	final String expression = visitor.expression();
	assertEquals("Incorrectly walked AST", "1 2.3 property + + ", expression);
    }

    @Test
    public void test_post_order_walking_of_expression_with_application_of_rules_term_op_term_op_term_with_lparen_sentense_rparen() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
	final Token[] tokens = new ExpressionLexer("1 + (2.3 + property)").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	final ExpressionToStringVisitor visitor = new ExpressionToStringVisitor();
	new AstWalker(ast, visitor).walk();
	final String expression = visitor.expression();
	assertEquals("Incorrectly walked AST", "1 2.3 property + + ", expression);

    }

    @Test
    public void test_post_order_walking_of_expression_with_application_of_rules_term_op_term_op_term_with_lparen_sentense_rparen_nested() throws RecognitionException, SequenceRecognitionFailed, SemanticException {
	final Token[] tokens = new ExpressionLexer("1 + (2.3 + (property + 1) / (23.6 * property))").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	final ExpressionToStringVisitor visitor = new ExpressionToStringVisitor();
	new AstWalker(ast, visitor).walk();
	final String expression = visitor.expression();
	assertEquals("Incorrectly walked AST", "1 2.3 property 1 + 23.6 property * / + + ", expression);
    }

    @Test
    public void test_post_order_walking_of_expression_with_parsing_with_wider_range_of_literals() throws SequenceRecognitionFailed, RecognitionException, SemanticException {
	final Token[] tokens = new ExpressionLexer("1d + (2.3 + (property + 1m) / (23y * property))").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	final ExpressionToStringVisitor visitor = new ExpressionToStringVisitor();
	new AstWalker(ast, visitor).walk();
	final String expression = visitor.expression();
	assertEquals("Incorrectly walked AST", "1d 2.3 property 1m + 23y property * / + + ", expression);
    }

    @Test
    public void test_post_order_walking_of_expression_with_parsing_of_functions() throws SequenceRecognitionFailed, RecognitionException, SemanticException {
	final Token[] tokens = new ExpressionLexer("AVG(property)").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	final ExpressionToStringVisitor visitor = new ExpressionToStringVisitor();
	new AstWalker(ast, visitor).walk();
	final String expression = visitor.expression();
	assertEquals("Incorrectly walked AST", "property AVG ", expression);
    }

    @Test
    public void test_post_order_walking_of_expression_with_parsing_of_functions_in_complex_expressions() throws SequenceRecognitionFailed, RecognitionException, SemanticException {
	final Token[] tokens = new ExpressionLexer("23 + AVG(property * 5) + (SUM (AVG(property.subProp1) / 3) - 10d)").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	final ExpressionToStringVisitor visitor = new ExpressionToStringVisitor();
	new AstWalker(ast, visitor).walk();
	final String expression = visitor.expression();
	assertEquals("Incorrectly walked AST", "23 property 5 * AVG property.subProp1 AVG 3 / SUM 10d - + + ", expression);
    }

}
