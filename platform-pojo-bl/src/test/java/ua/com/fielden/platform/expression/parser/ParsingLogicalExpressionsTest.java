package ua.com.fielden.platform.expression.parser;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ua.com.fielden.platform.expression.ExpressionLexer;
import ua.com.fielden.platform.expression.ExpressionParser;
import ua.com.fielden.platform.expression.Token;
import ua.com.fielden.platform.expression.ast.AstNode;
import ua.com.fielden.platform.expression.automata.SequenceRecognitionFailed;
import ua.com.fielden.platform.expression.exception.RecognitionException;

public class ParsingLogicalExpressionsTest {

    @Test
    public void parsing_expression_with_single_AND_operation() throws RecognitionException, SequenceRecognitionFailed {
        final Token[] tokens = new ExpressionLexer("1 > 2 && 2 < 3").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        assertEquals("Not all tokens have been parsed.", tokens.length, parser.getPosition());
        assertEquals("Incorrectly formed AST", "(&& (> 1 2) (< 2 3))", ast.treeToString());
    }

    @Test
    public void parsing_expression_with_single_AND_operation_with_subparen() throws RecognitionException, SequenceRecognitionFailed {
        final Token[] tokens = new ExpressionLexer("(1 > 2) && (2 < 3)").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        assertEquals("Not all tokens have been parsed.", tokens.length, parser.getPosition());
        assertEquals("Incorrectly formed AST", "(&& (> 1 2) (< 2 3))", ast.treeToString());
    }

    @Test
    public void parsing_expression_with_single_AND_operation_with_paren() throws RecognitionException, SequenceRecognitionFailed {
        final Token[] tokens = new ExpressionLexer("((1 > 2 && 2 < 3))").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        assertEquals("Not all tokens have been parsed.", tokens.length, parser.getPosition());
        assertEquals("Incorrectly formed AST", "(&& (> 1 2) (< 2 3))", ast.treeToString());
    }

    @Test
    public void parsing_expression_with_single_AND_operation_with_multiple_paren() throws RecognitionException, SequenceRecognitionFailed {
        final Token[] tokens = new ExpressionLexer("((1 > 2) && (2 < 3))").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        assertEquals("Not all tokens have been parsed.", tokens.length, parser.getPosition());
        assertEquals("Incorrectly formed AST", "(&& (> 1 2) (< 2 3))", ast.treeToString());
    }

    @Test
    public void parsing_expression_with_multiple_AND_operation() throws RecognitionException, SequenceRecognitionFailed {
        final Token[] tokens = new ExpressionLexer("1 < 2 && 2 < 3 && 3 < 4").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        assertEquals("Not all tokens have been parsed.", tokens.length, parser.getPosition());
        assertEquals("Incorrectly formed AST", "(&& (< 1 2) (&& (< 2 3) (< 3 4)))", ast.treeToString());
    }

    @Test
    public void parsing_expression_with_multiple_AND_operations_and_paren() throws RecognitionException, SequenceRecognitionFailed {
        final Token[] tokens = new ExpressionLexer("((1 < 2) && (2 < 3) && (3 < 4))").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        assertEquals("Not all tokens have been parsed.", tokens.length, parser.getPosition());
        assertEquals("Incorrectly formed AST", "(&& (< 1 2) (&& (< 2 3) (< 3 4)))", ast.treeToString());
    }

    @Test
    public void parsing_expression_with_multiple_AND_ordered_by_paren() throws RecognitionException, SequenceRecognitionFailed {
        final Token[] tokens = new ExpressionLexer("(1 < 2 && 2 < 3) && (3 < 4)").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        assertEquals("Not all tokens have been parsed.", tokens.length, parser.getPosition());
        assertEquals("Incorrectly formed AST", "(&& (&& (< 1 2) (< 2 3)) (< 3 4))", ast.treeToString());
    }

    @Test
    public void parsing_expression_with_mixed_AND_and_OR_ordered_by_paren() throws RecognitionException, SequenceRecognitionFailed {
        final Token[] tokens = new ExpressionLexer("(1 < 2 || 2 < 3) && (3 < 4)").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        assertEquals("Not all tokens have been parsed.", tokens.length, parser.getPosition());
        assertEquals("Incorrectly formed AST", "(&& (|| (< 1 2) (< 2 3)) (< 3 4))", ast.treeToString());
    }

    @Test
    public void parsing_expression_with_AND_and_OR_no_paren() throws RecognitionException, SequenceRecognitionFailed {
        final Token[] tokens = new ExpressionLexer("((1 < 2) && (2 < 3) || (3 < 4))").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        assertEquals("Not all tokens have been parsed.", tokens.length, parser.getPosition());
        assertEquals("Incorrectly formed AST", "(&& (< 1 2) (|| (< 2 3) (< 3 4)))", ast.treeToString());
    }

    @Test
    public void parsing_expression_with_mixed_AND_and_OR_ordered_by_paren_and_using_functions() throws RecognitionException, SequenceRecognitionFailed {
        final Token[] tokens = new ExpressionLexer("(1 < 2 || COUNT(property) < 3) && (3 < SUM(property))").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        assertEquals("Not all tokens have been parsed.", tokens.length, parser.getPosition());
        assertEquals("Incorrectly formed AST", "(&& (|| (< 1 2) (< (COUNT property) 3)) (< 3 (SUM property)))", ast.treeToString());
    }

    @Test
    public void parsing_simple_expression_with_NULL() throws RecognitionException, SequenceRecognitionFailed {
        final Token[] tokens = new ExpressionLexer("property <> NULL").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        assertEquals("Not all tokens have been parsed.", tokens.length, parser.getPosition());
        assertEquals("Incorrectly formed AST", "(<> property NULL)", ast.treeToString());
    }

    @Test
    public void parsing_expression_with_mixed_AND_and_OR_and_NULL() throws RecognitionException, SequenceRecognitionFailed {
        final Token[] tokens = new ExpressionLexer("(1 < 2 || property = NULL) && (3 < SUM(property))").tokenize();
        final ExpressionParser parser = new ExpressionParser(tokens);
        final AstNode ast = parser.parse();
        assertEquals("Not all tokens have been parsed.", tokens.length, parser.getPosition());
        assertEquals("Incorrectly formed AST", "(&& (|| (< 1 2) (= property NULL)) (< 3 (SUM property)))", ast.treeToString());
    }

}
