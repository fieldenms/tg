package ua.com.fielden.platform.expression.parser;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ua.com.fielden.platform.expression.ExpressionLexer;
import ua.com.fielden.platform.expression.ExpressionParser;
import ua.com.fielden.platform.expression.Token;
import ua.com.fielden.platform.expression.ast.AstNode;
import ua.com.fielden.platform.expression.automata.SequenceRecognitionFailed;
import ua.com.fielden.platform.expression.exception.RecognitionException;

public class ParsingDateExpressionsTest {

    @Test
    public void parsing_expression_with_days_function() throws RecognitionException, SequenceRecognitionFailed {
	final Token[] tokens = new ExpressionLexer("DAYS(dateProp, NOW) > 2d").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	assertEquals("Not all tokens have been parsed.", tokens.length, parser.getPosition());
	assertEquals("Incorrectly formed AST", "(> (DAYS dateProp NOW) 2d)", ast.treeToString());
    }

    @Test
    public void parsing_expression_with_months_function() throws RecognitionException, SequenceRecognitionFailed {
	final Token[] tokens = new ExpressionLexer("CASE WHEN MONTHS(dateProp, NOW) <= 3m THEN \"Yellow\" ELSE \"Green\" END").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	assertEquals("Not all tokens have been parsed.", tokens.length, parser.getPosition());
	assertEquals("Incorrectly formed AST", "(CASE (WHEN (<= (MONTHS dateProp NOW) 3m) \"Yellow\") \"Green\")", ast.treeToString());
    }

    @Test
    public void parsing_expression_with_years_function() throws RecognitionException, SequenceRecognitionFailed {
	final Token[] tokens = new ExpressionLexer("years(dateProp, anotherDateProp) > 1y").tokenize();
	final ExpressionParser parser = new ExpressionParser(tokens);
	final AstNode ast = parser.parse();
	assertEquals("Not all tokens have been parsed.", tokens.length, parser.getPosition());
	assertEquals("Incorrectly formed AST", "(> (YEARS dateProp anotherDateProp) 1y)", ast.treeToString());
    }

}
