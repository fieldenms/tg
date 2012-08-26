package ua.com.fielden.platform.expression.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import ua.com.fielden.platform.expression.ExpressionLexer;
import ua.com.fielden.platform.expression.ExpressionParser;
import ua.com.fielden.platform.expression.Token;
import ua.com.fielden.platform.expression.ast.AstNode;
import ua.com.fielden.platform.expression.automata.SequenceRecognitionFailed;
import ua.com.fielden.platform.expression.exception.RecognitionException;

public class ParsingComparisonExpressionsTest {

    @Test
    public void trivial_less_comparison() throws RecognitionException, SequenceRecognitionFailed {
	final Token[] tokens = new ExpressionLexer("1 > 2").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	assertEquals("Not all tokens have been parsed.", tokens.length, parser.getPosition());
	assertEquals("Incorrectly formed AST", "(> 1 2)", ast.treeToString());
    }

    @Test
    public void trivial_le_comparison() throws RecognitionException, SequenceRecognitionFailed {
	final Token[] tokens = new ExpressionLexer("1 >= 2").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	assertEquals("Not all tokens have been parsed.", tokens.length, parser.getPosition());
	assertEquals("Incorrectly formed AST", "(>= 1 2)", ast.treeToString());
    }

    @Test
    public void trivial_eq_comparison() throws RecognitionException, SequenceRecognitionFailed {
	final Token[] tokens = new ExpressionLexer("1 = 2").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	assertEquals("Not all tokens have been parsed.", tokens.length, parser.getPosition());
	assertEquals("Incorrectly formed AST", "(= 1 2)", ast.treeToString());
    }

    @Test
    public void trivial_greater_comparison() throws RecognitionException, SequenceRecognitionFailed {
	final Token[] tokens = new ExpressionLexer("1 > 2").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	assertEquals("Not all tokens have been parsed.", tokens.length, parser.getPosition());
	assertEquals("Incorrectly formed AST", "(> 1 2)", ast.treeToString());
    }

    @Test
    public void trivial_qe_comparison() throws RecognitionException, SequenceRecognitionFailed {
	final Token[] tokens = new ExpressionLexer("1 >= 2").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	assertEquals("Not all tokens have been parsed.", tokens.length, parser.getPosition());
	assertEquals("Incorrectly formed AST", "(>= 1 2)", ast.treeToString());
    }

    @Test
    public void trivial_ne_comparison() throws RecognitionException, SequenceRecognitionFailed {
	final Token[] tokens = new ExpressionLexer("1 <> 2").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	assertEquals("Not all tokens have been parsed.", tokens.length, parser.getPosition());
	assertEquals("Incorrectly formed AST", "(<> 1 2)", ast.treeToString());
    }


    @Test
    public void trivial_less_comparison_with_parenthsis() throws RecognitionException, SequenceRecognitionFailed {
	final Token[] tokens = new ExpressionLexer("(1 > 2)").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	assertEquals("Not all tokens have been parsed.", tokens.length, parser.getPosition());
	assertEquals("Incorrectly formed AST", "(> 1 2)", ast.treeToString());
    }

    @Test
    public void trivial_less_comparison_with_multiple_parenthsis() throws RecognitionException, SequenceRecognitionFailed {
	final Token[] tokens = new ExpressionLexer("((1 > 2))").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	assertEquals("Not all tokens have been parsed.", tokens.length, parser.getPosition());
	assertEquals("Incorrectly formed AST", "(> 1 2)", ast.treeToString());
    }

    @Test
    public void less_comparison_with_arithmetic_expression() throws RecognitionException, SequenceRecognitionFailed {
	final Token[] tokens = new ExpressionLexer("1 + 2 > 3 - 1").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	assertEquals("Not all tokens have been parsed.", tokens.length, parser.getPosition());
	assertEquals("Incorrectly formed AST", "(> (+ 1 2) (- 3 1))", ast.treeToString());
    }

    @Test
    public void less_comparison_with_arithmetic_expression_and_paren() throws RecognitionException, SequenceRecognitionFailed {
	final Token[] tokens = new ExpressionLexer("(1 + 2 > 3 - 1)").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	assertEquals("Not all tokens have been parsed.", tokens.length, parser.getPosition());
	assertEquals("Incorrectly formed AST", "(> (+ 1 2) (- 3 1))", ast.treeToString());
    }

    @Test
    public void less_comparison_with_arithmetic_expression_and_paren_and_subparen() throws RecognitionException, SequenceRecognitionFailed {
	final Token[] tokens = new ExpressionLexer("((1 + 2) > (3 - 1))").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	assertEquals("Not all tokens have been parsed.", tokens.length, parser.getPosition());
	assertEquals("Incorrectly formed AST", "(> (+ 1 2) (- 3 1))", ast.treeToString());
    }

    @Test
    public void less_comparison_with_arithmetic_expression_as_subsentences() throws RecognitionException, SequenceRecognitionFailed {
	final Token[] tokens = new ExpressionLexer("(1 + 2) > (3 - 1)").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	assertEquals("Not all tokens have been parsed.", tokens.length, parser.getPosition());
	assertEquals("Incorrectly formed AST", "(> (+ 1 2) (- 3 1))", ast.treeToString());
    }

    @Test
    public void comparison_operators_cannot_be_chained_case1() throws RecognitionException, SequenceRecognitionFailed {
	final Token[] tokens = new ExpressionLexer("(1 + 2) > (3 - 1) > (2+1)").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	try {
	    parser.parse();
	    fail("Should have failed due to incorrect use of comparison operators.");
	} catch (final Exception ex) {
	}
    }

    @Test
    public void comparison_operators_cannot_be_chained_case2() throws RecognitionException, SequenceRecognitionFailed {
	final Token[] tokens = new ExpressionLexer("(1 > 3) > 2").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	try {
	    parser.parse();
	    fail("Should have failed due to incorrect use of comparison operators.");
	} catch (final Exception ex) {
	}
    }

    @Test
    public void comparison_operators_cannot_be_chained_case3() throws RecognitionException, SequenceRecognitionFailed {
	final Token[] tokens = new ExpressionLexer("((1 + 2) > (3 - 1)) > (2+1)").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	try {
	    parser.parse();
	    fail("Should have failed due to incorrect use of comparison operators.");
	} catch (final Exception ex) {
	}
    }

}
