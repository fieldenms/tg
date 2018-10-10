package ua.com.fielden.platform.expression.parser;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ua.com.fielden.platform.expression.ExpressionLexer;
import ua.com.fielden.platform.expression.ExpressionParser;
import ua.com.fielden.platform.expression.Token;
import ua.com.fielden.platform.expression.ast.AstNode;
import ua.com.fielden.platform.expression.automata.SequenceRecognitionFailed;
import ua.com.fielden.platform.expression.exception.RecognitionException;

public class ParsingArithmeticExpressionsWithCaseWhenTest {

    @Test
    public void PLUS_with_CASE_WHEN_as_second_operand_is_parsable() throws RecognitionException, SequenceRecognitionFailed {
        final Token[] tokens = new ExpressionLexer("1 + CASE WHEN 2 > 1 THEN 2 ELSE 3 END").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        assertEquals("Not all tokens have been parsed.", tokens.length, parser.getPosition());
        assertEquals("Incorrectly formed AST", "(+ 1 (CASE (WHEN (> 2 1) 2) 3))", ast.treeToString());
    }

    @Test
    public void PLUS_with_CASE_WHEN_as_first_operand_is_parsable() throws RecognitionException, SequenceRecognitionFailed {
        final Token[] tokens = new ExpressionLexer("CASE WHEN 2 > 1 THEN 2 ELSE 3 END + 1").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        assertEquals("Not all tokens have been parsed.", tokens.length, parser.getPosition());
        assertEquals("Incorrectly formed AST", "(+ (CASE (WHEN (> 2 1) 2) 3) 1)", ast.treeToString());
    }

    @Test
    public void dual_PLUS_with_CASE_WHEN_as_second_operand_is_parsable() throws RecognitionException, SequenceRecognitionFailed {
        final Token[] tokens = new ExpressionLexer("1 + CASE WHEN 2 > 1 THEN 2 ELSE 3 END + 3").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        assertEquals("Not all tokens have been parsed.", tokens.length, parser.getPosition());
        assertEquals("Incorrectly formed AST", "(+ 1 (+ (CASE (WHEN (> 2 1) 2) 3) 3))", ast.treeToString());
    }

    @Test
    public void SUM_divided_by_SUM_under_CASE_WHEN_is_parsable() throws RecognitionException, SequenceRecognitionFailed {
        final Token[] tokens = new ExpressionLexer("SUM(intProperty) / CASE WHEN SUM(decimalProperty) > 0 THEN SUM(decimalProperty) END").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        assertEquals("Not all tokens have been parsed.", tokens.length, parser.getPosition());
        assertEquals("Incorrectly formed AST", "(/ (SUM intProperty) (CASE (WHEN (> (SUM decimalProperty) 0) (SUM decimalProperty))))", ast.treeToString());
    }
    
    @Test
    public void CASE_WHEN_over_SUM_divided_by_SUM_is_parsable() throws RecognitionException, SequenceRecognitionFailed {
        final Token[] tokens = new ExpressionLexer("CASE WHEN SUM(kms) <> 0 THEN SUM(leaseCost) / SUM(kms) END").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        assertEquals("Not all tokens have been parsed.", tokens.length, parser.getPosition());
        assertEquals("Incorrectly formed AST", "(CASE (WHEN (<> (SUM kms) 0) (/ (SUM leaseCost) (SUM kms))))", ast.treeToString());
    }
    

}
