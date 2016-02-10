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
import ua.com.fielden.platform.expression.exception.RecognitionException;

public class ParsingCaseExpressionsTest {

    @Test
    public void parsing_expression_with_single_WHEN() throws RecognitionException, SequenceRecognitionFailed {
        final Token[] tokens = new ExpressionLexer("CASE WHEN 1 > 2 THEN \"Red\" ELSE \"Green\" END").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        assertEquals("Not all tokens have been parsed.", tokens.length, parser.getPosition());
        assertEquals("Incorrectly formed AST", "(CASE (WHEN (> 1 2) \"Red\") \"Green\")", ast.treeToString());
    }

    @Test
    public void parsing_expression_with_single_WHEN_and_logical_conditions() throws RecognitionException, SequenceRecognitionFailed {
        final Token[] tokens = new ExpressionLexer("CASE WHEN 1 > 2 && SUM(prop) <= 5 THEN \"Red\" ELSE \"Green\" END").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        assertEquals("Not all tokens have been parsed.", tokens.length, parser.getPosition());
        assertEquals("Incorrectly formed AST", "(CASE (WHEN (&& (> 1 2) (<= (SUM prop) 5)) \"Red\") \"Green\")", ast.treeToString());
    }

    @Test
    public void parsing_expression_with_single_WHEN_and_logical_condition_with_now() throws RecognitionException, SequenceRecognitionFailed {
        final Token[] tokens = new ExpressionLexer("CASE WHEN dateProp <= NOW THEN \"Red\" ELSE \"Green\" END").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        assertEquals("Not all tokens have been parsed.", tokens.length, parser.getPosition());
        assertEquals("Incorrectly formed AST", "(CASE (WHEN (<= dateProp NOW) \"Red\") \"Green\")", ast.treeToString());
        // let's also check that NOW is not recognised as NAME
        final AstNode now = ast.getChildren().get(0).getChildren().get(0).getChildren().get(1);
        assertEquals(EgTokenCategory.NOW, now.getToken().category);
    }

    @Test
    public void parsing_expression_with_single_WHEN_and_logical_conditions_and_paren() throws RecognitionException, SequenceRecognitionFailed {
        final Token[] tokens = new ExpressionLexer("case  when  ((1 > 2) && (SUM(prop) <= 5)) then \"Red\" else \"Green\" end ").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        assertEquals("Not all tokens have been parsed.", tokens.length, parser.getPosition());
        assertEquals("Incorrectly formed AST", "(CASE (WHEN (&& (> 1 2) (<= (SUM prop) 5)) \"Red\") \"Green\")", ast.treeToString());
    }

    @Test
    public void parsing_expression_with_multiple_WHEN_in_separate_lines() throws RecognitionException, SequenceRecognitionFailed {
        final Token[] tokens = new ExpressionLexer("CASE \n" + "WHEN 1 > 2 THEN \"Red\" \n" + "WHEN 3 = 2 THEN \"Yellow\" \n" + "ELSE \"Green\" END").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        assertEquals("Not all tokens have been parsed.", tokens.length, parser.getPosition());
        assertEquals("Incorrectly formed AST", "(CASE (WHEN (> 1 2) \"Red\") (WHEN (= 3 2) \"Yellow\") \"Green\")", ast.treeToString());
    }

    @Test
    public void CASE_without_ELSE_shoud_be_supported() throws RecognitionException, SequenceRecognitionFailed {
        final Token[] tokens = new ExpressionLexer("CASE WHEN 1 > 2 THEN \"string\" END").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        assertEquals("Not all tokens have been parsed.", tokens.length, parser.getPosition());
        assertEquals("Incorrectly formed AST", "(CASE (WHEN (> 1 2) \"string\"))", ast.treeToString());
    }

    @Test
    public void parsing_of_incorrectly_formed_CASE_expression_where_return_result_has_invalid_type() throws RecognitionException, SequenceRecognitionFailed {
        final Token[] tokens = new ExpressionLexer("CASE WHEN 1 > 2 THEN 2 ELSE \"Green\" END").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        try {
            parser.parse();
            fail("Invalid CASE expression should not have been parsed.");
        } catch (final Exception e) {
        }
    }

    @Test
    public void parsing_of_incorrectly_formed_CASE_expression_where_logical_expression_is_missing() throws RecognitionException, SequenceRecognitionFailed {
        final Token[] tokens = new ExpressionLexer("CASE WHEN 1 > 2 THEN \"string\" WHEN THEN \"string\" ELSE \"string\" END").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        try {
            parser.parse();
            fail("Invalid CASE expression should not have been parsed.");
        } catch (final Exception e) {
        }
    }

}
