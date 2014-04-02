package ua.com.fielden.platform.expression.lexer;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import ua.com.fielden.platform.expression.EgTokenCategory;
import ua.com.fielden.platform.expression.ExpressionLexer;
import ua.com.fielden.platform.expression.Token;
import ua.com.fielden.platform.expression.automata.SequenceRecognitionFailed;
import ua.com.fielden.platform.expression.exception.RecognitionException;

public class ExpressionLexerTest {

    @Test
    public void test_full_set_of_lexemes() throws SequenceRecognitionFailed {
        final ExpressionLexer el = new ExpressionLexer(//
        "+ - / * , \t" + //
                "\" string value \" AVG(123) prop property1.subProperty SUM( 12.2 ) " + //
                "23d 36m 2y");
        assertEquals(new Token(EgTokenCategory.PLUS, "+"), el.nextToken());
        assertEquals(new Token(EgTokenCategory.MINUS, "-"), el.nextToken());
        assertEquals(new Token(EgTokenCategory.DIV, "/"), el.nextToken());
        assertEquals(new Token(EgTokenCategory.MULT, "*"), el.nextToken());
        assertEquals(new Token(EgTokenCategory.COMMA, ","), el.nextToken());
        assertEquals(new Token(EgTokenCategory.STRING, "\" string value \""), el.nextToken());
        assertEquals(new Token(EgTokenCategory.AVG, "AVG"), el.nextToken());
        assertEquals(new Token(EgTokenCategory.LPAREN, "("), el.nextToken());
        assertEquals(new Token(EgTokenCategory.INT, "123"), el.nextToken());
        assertEquals(new Token(EgTokenCategory.RPAREN, ")"), el.nextToken());
        assertEquals(new Token(EgTokenCategory.NAME, "prop"), el.nextToken());
        assertEquals(new Token(EgTokenCategory.NAME, "property1.subProperty"), el.nextToken());
        assertEquals(new Token(EgTokenCategory.SUM, "SUM"), el.nextToken());
        assertEquals(new Token(EgTokenCategory.LPAREN, "("), el.nextToken());
        assertEquals(new Token(EgTokenCategory.DECIMAL, "12.2"), el.nextToken());
        assertEquals(new Token(EgTokenCategory.RPAREN, ")"), el.nextToken());
        assertEquals(new Token(EgTokenCategory.DATE_CONST, "23d"), el.nextToken());
        assertEquals(new Token(EgTokenCategory.DATE_CONST, "36m"), el.nextToken());
        assertEquals(new Token(EgTokenCategory.DATE_CONST, "2y"), el.nextToken());
        assertEquals(new Token(EgTokenCategory.EOF, "<EOF>"), el.nextToken());
    }

    @Test
    public void test_complete_input_text_tokenization() throws SequenceRecognitionFailed {
        final ExpressionLexer el = new ExpressionLexer(//
        "+ - / * , \t" + //
                "\" string value \" AVG(123) prop property1.subProperty SUM( 12.2 ) " + //
                "23d 36m 2y");

        final Token[] expectedTokens = {//
        new Token(EgTokenCategory.PLUS, "+"), new Token(EgTokenCategory.MINUS, "-"),//
                new Token(EgTokenCategory.DIV, "/"), new Token(EgTokenCategory.MULT, "*"),//
                new Token(EgTokenCategory.COMMA, ","), new Token(EgTokenCategory.STRING, "\" string value \""),//
                new Token(EgTokenCategory.AVG, "AVG"), new Token(EgTokenCategory.LPAREN, "("),//
                new Token(EgTokenCategory.INT, "123"), new Token(EgTokenCategory.RPAREN, ")"),//
                new Token(EgTokenCategory.NAME, "prop"), new Token(EgTokenCategory.NAME, "property1.subProperty"),//
                new Token(EgTokenCategory.SUM, "SUM"), new Token(EgTokenCategory.LPAREN, "("),//
                new Token(EgTokenCategory.DECIMAL, "12.2"), new Token(EgTokenCategory.RPAREN, ")"),//
                new Token(EgTokenCategory.DATE_CONST, "23d"), new Token(EgTokenCategory.DATE_CONST, "36m"),//
                new Token(EgTokenCategory.DATE_CONST, "2y") };
        assertArrayEquals("Mismatch between expected and actually obtained tokens.", expectedTokens, el.tokenize());
    }

    @Test
    public void test_lexing_with_invalid_string_lexeme() throws SequenceRecognitionFailed {
        final ExpressionLexer el = new ExpressionLexer("(property.subProperty + \" + propety1)");
        assertEquals(new Token(EgTokenCategory.LPAREN, "("), el.nextToken());
        assertEquals(new Token(EgTokenCategory.NAME, "property.subProperty"), el.nextToken());
        assertEquals(new Token(EgTokenCategory.PLUS, "+"), el.nextToken());
        try {
            el.nextToken();
            fail("An exception is expected.");
        } catch (final SequenceRecognitionFailed ex) {
            assertEquals("Incorrect error message.", "Missing closing '\"'", ex.transitionException.getMessage());
        }
    }

    @Test
    public void test_lexing_text_which_starts_with_function() throws SequenceRecognitionFailed {
        final ExpressionLexer el = new ExpressionLexer("avg( SUM (property1.subProperty) ) + \"STriNG\"");
        assertEquals(new Token(EgTokenCategory.AVG, "AVG"), el.nextToken());
        assertEquals(new Token(EgTokenCategory.LPAREN, "("), el.nextToken());
        assertEquals(new Token(EgTokenCategory.SUM, "SUM"), el.nextToken());
        assertEquals(new Token(EgTokenCategory.LPAREN, "("), el.nextToken());
        assertEquals(new Token(EgTokenCategory.NAME, "property1.subProperty"), el.nextToken());
        assertEquals(new Token(EgTokenCategory.RPAREN, ")"), el.nextToken());
        assertEquals(new Token(EgTokenCategory.RPAREN, ")"), el.nextToken());
        assertEquals(new Token(EgTokenCategory.PLUS, "+"), el.nextToken());
        assertEquals(new Token(EgTokenCategory.STRING, "\"STriNG\""), el.nextToken());
        assertEquals(new Token(EgTokenCategory.EOF, "<EOF>"), el.nextToken());
    }

    @Test
    public void test_lexing_text_which_misspelled_functions() throws SequenceRecognitionFailed {
        final ExpressionLexer el = new ExpressionLexer("AV G( property1.subProperty / S UM (a)");
        assertEquals(new Token(EgTokenCategory.NAME, "AV"), el.nextToken());
        assertEquals(new Token(EgTokenCategory.NAME, "G"), el.nextToken());
        assertEquals(new Token(EgTokenCategory.LPAREN, "("), el.nextToken());
        assertEquals(new Token(EgTokenCategory.NAME, "property1.subProperty"), el.nextToken());
        assertEquals(new Token(EgTokenCategory.DIV, "/"), el.nextToken());
        assertEquals(new Token(EgTokenCategory.NAME, "S"), el.nextToken());
        assertEquals(new Token(EgTokenCategory.NAME, "UM"), el.nextToken());
        assertEquals(new Token(EgTokenCategory.LPAREN, "("), el.nextToken());
        assertEquals(new Token(EgTokenCategory.NAME, "a"), el.nextToken());
        assertEquals(new Token(EgTokenCategory.RPAREN, ")"), el.nextToken());
        assertEquals(new Token(EgTokenCategory.EOF, "<EOF>"), el.nextToken());
    }

    @Test
    public void test_token_positions() throws RecognitionException {
        final String expression = "1 + (2.3 + (property + 1) / (23.6 * AVG (property. p)))";
        final Token[] tokens = new ExpressionLexer(expression).tokenize();
        assertEquals("Incorrect token text position in the original expression", "1 ", expression.substring(tokens[0].beginIndex, tokens[0].endIndex));
        assertEquals("Incorrect token text position in the original expression", "+ ", expression.substring(tokens[1].beginIndex, tokens[1].endIndex));
        assertEquals("Incorrect token text position in the original expression", "(", expression.substring(tokens[2].beginIndex, tokens[2].endIndex));
        assertEquals("Incorrect token text position in the original expression", "2.3", expression.substring(tokens[3].beginIndex, tokens[3].endIndex));
        assertEquals("Incorrect token text position in the original expression", " + ", expression.substring(tokens[4].beginIndex, tokens[4].endIndex));
        assertEquals("Incorrect token text position in the original expression", "(", expression.substring(tokens[5].beginIndex, tokens[5].endIndex));
        assertEquals("Incorrect token text position in the original expression", "property ", expression.substring(tokens[6].beginIndex, tokens[6].endIndex));
        assertEquals("Incorrect token text position in the original expression", "+ ", expression.substring(tokens[7].beginIndex, tokens[7].endIndex));
        assertEquals("Incorrect token text position in the original expression", "1", expression.substring(tokens[8].beginIndex, tokens[8].endIndex));
        assertEquals("Incorrect token text position in the original expression", ") ", expression.substring(tokens[9].beginIndex, tokens[9].endIndex));
        assertEquals("Incorrect token text position in the original expression", "/ ", expression.substring(tokens[10].beginIndex, tokens[10].endIndex));
        assertEquals("Incorrect token text position in the original expression", "(", expression.substring(tokens[11].beginIndex, tokens[11].endIndex));
        assertEquals("Incorrect token text position in the original expression", "23.6", expression.substring(tokens[12].beginIndex, tokens[12].endIndex));
        assertEquals("Incorrect token text position in the original expression", " * ", expression.substring(tokens[13].beginIndex, tokens[13].endIndex));
        assertEquals("Incorrect token text position in the original expression", "AVG ", expression.substring(tokens[14].beginIndex, tokens[14].endIndex));
        assertEquals("Incorrect token text position in the original expression", "(", expression.substring(tokens[15].beginIndex, tokens[15].endIndex));
        assertEquals("Incorrect token text position in the original expression", "property. p", expression.substring(tokens[16].beginIndex, tokens[16].endIndex));
        assertEquals("Incorrect token text position in the original expression", ")", expression.substring(tokens[17].beginIndex, tokens[17].endIndex));
        assertEquals("Incorrect token text position in the original expression", ")", expression.substring(tokens[18].beginIndex, tokens[18].endIndex));
        assertEquals("Incorrect token text position in the original expression", ")", expression.substring(tokens[19].beginIndex, tokens[19].endIndex));
    }
}
