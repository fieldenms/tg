package ua.com.fielden.platform.expression.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import ua.com.fielden.platform.expression.EgTokenCategory;
import ua.com.fielden.platform.expression.ExpressionLexer;
import ua.com.fielden.platform.expression.ExpressionParser;
import ua.com.fielden.platform.expression.Token;
import ua.com.fielden.platform.expression.ast.AstNode;
import ua.com.fielden.platform.expression.automata.SequenceRecognitionFailed;
import ua.com.fielden.platform.expression.exception.MisplacedTokenException;
import ua.com.fielden.platform.expression.exception.MissingTokenException;
import ua.com.fielden.platform.expression.exception.NoViableAltException;
import ua.com.fielden.platform.expression.exception.RecognitionException;
import ua.com.fielden.platform.expression.exception.ReservedNameException;
import ua.com.fielden.platform.expression.exception.UnwantedTokenException;

public class ParsingArithmeticExpressionsWithAggreagationFunctionsTest {

    @Test
    public void test_single_application_of_rules_term_op_term() throws RecognitionException, SequenceRecognitionFailed {
	final Token[] tokens = new ExpressionLexer("1 + 2").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	assertEquals("Not all tokens have been parsed.", tokens.length, parser.getPosition());
	assertEquals("Incorrectly formed AST", "(+ 1 2)", ast.treeToString());
    }


    @Test
    public void parantheses_usage_case1() throws RecognitionException, SequenceRecognitionFailed {
	final Token[] tokens = new ExpressionLexer("(1 + 2)").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	assertEquals("Not all tokens have been parsed.", tokens.length, parser.getPosition());
	assertEquals("Incorrectly formed AST", "(+ 1 2)", ast.treeToString());
    }

    @Test
    public void parantheses_usage_case2() throws RecognitionException, SequenceRecognitionFailed {
	final Token[] tokens = new ExpressionLexer("(1 + 2) * 2").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	assertEquals("Not all tokens have been parsed.", tokens.length, parser.getPosition());
	assertEquals("Incorrectly formed AST", "(* (+ 1 2) 2)", ast.treeToString());
    }


    @Test
    public void parantheses_usage_case3() throws RecognitionException, SequenceRecognitionFailed {
	final Token[] tokens = new ExpressionLexer("1 * (2 + 3)").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	assertEquals("Not all tokens have been parsed.", tokens.length, parser.getPosition());
	assertEquals("Incorrectly formed AST", "(* 1 (+ 2 3))", ast.treeToString());
    }

    @Test
    public void parantheses_usage_case4() throws RecognitionException, SequenceRecognitionFailed {
	final Token[] tokens = new ExpressionLexer("(1 + 4) * (2 + 3)").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	assertEquals("Not all tokens have been parsed.", tokens.length, parser.getPosition());
	assertEquals("Incorrectly formed AST", "(* (+ 1 4) (+ 2 3))", ast.treeToString());
    }


    @Test
    public void test_application_of_rules_term_op_term_op_term() throws RecognitionException, SequenceRecognitionFailed {
	final Token[] tokens = new ExpressionLexer("1 + 2.3 + property").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	assertEquals("Not all tokens have been parsed.", tokens.length, parser.getPosition());
	assertEquals("Incorrectly formed AST", "(+ 1 (+ 2.3 property))", ast.treeToString());
    }

    @Test
    public void test_application_of_rules_term_op_term_op_term_with_lparen_sentense_rparen() throws RecognitionException, SequenceRecognitionFailed {
	final Token[] tokens = new ExpressionLexer("1 + (2.3 + property)").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	assertEquals("Not all tokens have been parsed.", tokens.length, parser.getPosition());
	assertEquals("Incorrectly formed AST", "(+ 1 (+ 2.3 property))", ast.treeToString());
    }

    @Test
    public void test_application_of_rules_term_op_term_op_term_with_lparen_sentense_rparen_nested() throws RecognitionException, SequenceRecognitionFailed {
	final Token[] tokens = new ExpressionLexer("1 + (2.3 + (property + 1) / (23.6 * property))").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	assertEquals("Not all tokens have been parsed.", tokens.length, parser.getPosition());
	assertEquals("Incorrectly formed AST", "(+ 1 (+ 2.3 (/ (+ property 1) (* 23.6 property))))", ast.treeToString());
    }

    @Test
    public void test_parsing_with_wider_range_of_literals() throws SequenceRecognitionFailed, RecognitionException {
	final Token[] tokens = new ExpressionLexer("1d + (2.3 + (property + 1m) / (23y * property))").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	assertEquals("Not all tokens have been parsed.", tokens.length, parser.getPosition());
	assertEquals("Incorrectly formed AST", "(+ 1d (+ 2.3 (/ (+ property 1m) (* 23y property))))", ast.treeToString());
    }

    @Test
    public void test_parsing_of_functions() throws SequenceRecognitionFailed, RecognitionException {
	final Token[] tokens = new ExpressionLexer("AVG(property)").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	assertEquals("Not all tokens have been parsed.", tokens.length, parser.getPosition());
	assertEquals("Incorrectly formed AST", "(AVG property)", ast.treeToString());
    }

    @Test
    public void test_parsing_of_functions_in_complex_expressions() throws SequenceRecognitionFailed, RecognitionException {
	final Token[] tokens = new ExpressionLexer("23 + AVG(property * 5) + (SUM (AVG(property.subProp1) / 3) - 10d)").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	assertEquals("Not all tokens have been parsed.", tokens.length, parser.getPosition());
	assertEquals("Incorrectly formed AST", "(+ 23 (+ (AVG (* property 5)) (- (SUM (/ (AVG property.subProp1) 3)) 10d)))", ast.treeToString());
    }

    @Test
    public void test_parsing_of_expression_with_precedence_operations_case_1() throws SequenceRecognitionFailed, RecognitionException {
	final Token[] tokens = new ExpressionLexer("23 * 5 / 3 + 6").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	assertEquals("Not all tokens have been parsed.", tokens.length, parser.getPosition());
	assertEquals("Incorrectly formed AST", "(+ (/ (* 23 5) 3) 6)", ast.treeToString());
    }

    @Test
    public void test_parsing_of_expression_with_precedence_operations_case_2() throws RecognitionException, SequenceRecognitionFailed {
	final Token[] tokens = new ExpressionLexer("5 - 4 + 3").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	assertEquals("Incorrectly formed AST", "(+ (- 5 4) 3)", ast.treeToString());
    }

    @Test
    public void test_parsing_of_expression_with_precedence_operations_case_3() throws RecognitionException, SequenceRecognitionFailed {
	final Token[] tokens = new ExpressionLexer("5 - 4 * 3").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	assertEquals("Incorrectly formed AST", "(- 5 (* 4 3))", ast.treeToString());
    }

    @Test
    public void test_parsing_of_expression_with_precedence_operations_case_4() throws RecognitionException, SequenceRecognitionFailed {
	final Token[] tokens = new ExpressionLexer("5 + 4 - 3").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	assertEquals("Incorrectly formed AST", "(+ 5 (- 4 3))", ast.treeToString());
    }

    @Test
    public void test_parsing_of_expression_with_precedence_operations_case_5() throws RecognitionException, SequenceRecognitionFailed {
	final Token[] tokens = new ExpressionLexer("5 + 4 * 6 - 3").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	assertEquals("Incorrectly formed AST", "(+ 5 (- (* 4 6) 3))", ast.treeToString());
    }

    @Test
    public void test_parsing_of_expression_with_precedence_operations_case_6() throws RecognitionException, SequenceRecognitionFailed {
	final Token[] tokens = new ExpressionLexer("5 * 4 - 6 / 3").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	assertEquals("Incorrectly formed AST", "(- (* 5 4) (/ 6 3))", ast.treeToString());
    }

    @Test
    public void test_parsing_of_expression_with_missing_rparen() throws SequenceRecognitionFailed, RecognitionException {
	final Token[] tokens = new ExpressionLexer("23 * (property + 5").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);

	try {
	    parser.parse();
	    fail("Expression is invalid and should not be parsed");
	} catch (final NoViableAltException e) {
	    assertEquals("Incorrect parsing error message.", "Expecting token RPAREN, but found end of input.", e.getMessage());
	}
    }

    @Test
    public void test_parsing_of_expression_with_missing_lparen() throws RecognitionException, SequenceRecognitionFailed {
	final Token[] tokens = new ExpressionLexer("23 + property) * 5").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);

	try {
	    parser.parse();
	    fail("Expression is invalid and should not be parsed");
	} catch (final UnwantedTokenException e) {
	    assertEquals("Incorrect parsing error message.", "Unwanted token <')',RPAREN>", e.getMessage());
	}
    }

    @Test
    public void test_parsing_of_expression_with_missing_right_operand() throws RecognitionException, SequenceRecognitionFailed {
	final Token[] tokens = new ExpressionLexer(" + property").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);

	try {
	    parser.parse();
	    fail("Expression is invalid and should not be parsed");
	} catch (final NoViableAltException e) {
	    assertEquals("Incorrect parsing error message.", "Unexpected token <'+',PLUS>", e.getMessage());
	}
    }

    @Test
    public void test_parsing_of_expression_with_missing_left_operand() throws SequenceRecognitionFailed, RecognitionException {
	final Token[] tokens = new ExpressionLexer("property +").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);

	try {
	    parser.parse();
	    fail("Expression is invalid and should not be parsed");
	} catch (final MissingTokenException e) {
	    assertEquals("Incorrect parsing error message.", "Missing token after token <'+',PLUS>", e.getMessage());
	}
    }

    @Test
    public void unrelated_part_of_arithmetic_expression_shoul_fail_parsing() throws SequenceRecognitionFailed, RecognitionException {
	final Token[] tokens = new ExpressionLexer("(property + 1) (3 + 4)").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);

	try {
	    parser.parse();
	    fail("Expression is invalid and should not be parsed");
	} catch (final UnwantedTokenException e) {
	}
    }

    @Test
    public void test_parsing_of_expression_with_missing_function_argument() throws SequenceRecognitionFailed, RecognitionException {
	final Token[] tokens = new ExpressionLexer("AVG ()").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);

	try {
	    parser.parse();
	    fail("Expression is invalid and should not be parsed");
	} catch (final NoViableAltException e) {
	    assertEquals("Incorrect parsing error message.", "Unexpected token <')',RPAREN>", e.getMessage());
	}
    }

    @Test
    public void test_parsing_of_expression_with_empty_paren() throws SequenceRecognitionFailed, RecognitionException {
	final Token[] tokens = new ExpressionLexer("()").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);

	try {
	    parser.parse();
	    fail("Expression is invalid and should not be parsed");
	} catch (final NoViableAltException e) {
	    assertEquals("Incorrect parsing error message.", "Unexpected token <')',RPAREN>", e.getMessage());
	}
    }

    @Test
    public void parsing_of_expression_with_repeted_operation_should_fail() throws SequenceRecognitionFailed, RecognitionException {
	final Token[] tokens = new ExpressionLexer("2 + + 3").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);

	try {
	    parser.parse();
	    fail("Expression is invalid and should not be parsed");
	} catch (final NoViableAltException e) {
	    assertEquals("Incorrect parsing error message.", "Unexpected token <'+',PLUS>", e.getMessage());
	}
    }

    @Test
    public void should_have_parsed_expressions_with_self_keyword() throws RecognitionException, SequenceRecognitionFailed {
	final Token[] tokens = new ExpressionLexer("COUNT( SELF ) + 2").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	assertEquals("Incorrectly formed AST", "(+ (COUNT SELF) 2)", ast.treeToString());
	assertEquals("Incorrectly recognition of SELF", EgTokenCategory.SELF, ast.getChildren().get(0).getChildren().get(0).getToken().category);
    }

    @Test
    public void self_keyword_should_be_used_only_as_part_of_count() throws RecognitionException, SequenceRecognitionFailed {
	final Token[] tokens = new ExpressionLexer("AVG( SELF ) + 2").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	try {
	    parser.parse();
	    fail("Expression is invalid and should not be parsed");
	} catch (final MisplacedTokenException e) {
	    assertEquals("Incorrect parsing error message.", "Token " + EgTokenCategory.SELF + " can only be used as part of " + EgTokenCategory.COUNT + ".", e.getMessage());
	}
    }

    @Test
    public void should_have_failed_to_parsed_expressions_with_incorrectly_used_self_keyword() throws RecognitionException, SequenceRecognitionFailed {
	final Token[] tokens = new ExpressionLexer("COUNT( SELF + 2)").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	try {
	    parser.parse();
	    fail("Expression is invalid and should not be parsed");
	} catch (final ReservedNameException e) {
	    assertEquals("Incorrect parsing error message.", "SELF is a keyword, should not be used as some property name.", e.getMessage());
	}
    }
}
