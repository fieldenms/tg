package ua.com.fielden.platform.csv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.antlr.runtime.Lexer;
import org.antlr.runtime.Parser;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;

import ua.com.fielden.platform.csv.comma.CsvFileUtils;
import ua.com.fielden.platform.csv.vertical_bar.VbsvFileUtils;

/**
 * Abstract class to generalise testing of char-separated-value file's parsers like {@link CsvFileUtils}, {@link VbsvFileUtils} etc
 * 
 * @author yura
 * 
 */
public abstract class CharSeparatedValuesTest {

    private final String delimiter;

    public CharSeparatedValuesTest(final String delimiter) {
	this.delimiter = delimiter;
    }

    @Test
    public void testNewline() throws Exception {
	final List<String> result = parseLine("\n");
	assertTrue("Nothing to return", result.isEmpty());
    }

    @Test
    public void testCRLF() throws Exception {
	final List<String> result = parseLine("\r\n");
	assertTrue("Nothing to return", result.isEmpty());
    }

    @Test
    public void testSingleWord() throws Exception {
	final String result = parseField("Red");
	assertTrue("Expected Red, but found " + result, result.equals("Red"));
    }

    @Test
    public void testQuotedString() throws Exception {
	// trying to parse line "\"Red, White, and Blue\"" provided that delimiter is comma
	final String result = parseField("\"Red" + delimiter + " White" + delimiter + " and Blue\"");
	assertTrue("Expected <<Red" + delimiter + " White" + delimiter + " and Blue>>, but found <<" + result + ">>", result.equals("Red" + delimiter + " White" + delimiter
		+ " and Blue"));
    }

    @Test
    public void testQuoteEscaping() throws Exception {
	final String result = parseField("\"Before\"\"After\"");
	assertTrue("Expected <<Before\"After>>, but found <<" + result + ">>", result.equals("Before\"After"));
    }

    @Test
    public void testMultipleWordsWithEmbeddedCarageReturn() throws Exception {
	// trying to parse line "Red,Green,,\"Blue\n\"\n" provided that delimiter is comma
	final List<String> result = parseLine("Red" + delimiter + "Green" + delimiter + delimiter + "\"Blue\n\"\n");
	assertTrue("Expected 4 items", result.size() == 4);
	assertTrue("Expected Red", result.get(0).equals("Red"));
	assertTrue("Expected Green", result.get(1).equals("Green"));
	assertTrue("Expected empty", result.get(2).equals(""));
	assertEquals("Expected Blue", "Blue\n", result.get(3));
    }

    /**
     * Leading spaces are expected to be removed, trailing are not.
     * 
     * @throws IOException
     * @throws RecognitionException
     */
    @Test
    public void testThatSpaceRemoval() throws Exception {
	// trying to parse line "\" Red  \",   Green, ,Blue\n" provided that delimiter is comma
	final Lexer lexer = createLexer("\" Red  \"" + delimiter + "   Green" + delimiter + " " + delimiter + "Blue\n");
	final List<String> result = parse(createParser(lexer));
	assertTrue("Expected 4 items", result.size() == 4);
	assertTrue("Expected Red", result.get(0).equals(" Red  "));
	assertEquals("Expected Green", "Green", result.get(1));
	assertTrue("Expected empty", result.get(2).equals(""));
	assertTrue("Expected Blue", result.get(3).equals("Blue"));
	// Now make sure we didn't have any lexing errors, which were failing silently earlier
	// The parser drives the lexer, so check for exceptions after
	// parsing.
	final List<RecognitionException> lexerExceptions = getExceptions(lexer);
	assertTrue("Lexer threw exceptions -- see output", lexerExceptions.isEmpty());
    }

    private String parseField(final String testString) throws Exception {
	final List<String> result = parseLine(testString + "\n");
	return result.get(0);
    }

    private List<String> parseLine(final String line) throws Exception {
	final Lexer lexer = createLexer(line);
	final Parser parser = createParser(lexer);
	return parse(parser);
    }

    /**
     * Should return parsed char-separated line using specified {@link Parser}.
     * 
     * @param parser
     * @return
     * @throws Exception
     */
    public abstract List<String> parse(Parser parser) throws Exception;

    /**
     * Should create {@link Lexer} instance using specified {@link String}, to be used later in order to parse char-separated lines.
     * 
     * @param line
     * @return
     */
    public abstract Lexer createLexer(String line) throws Exception;

    /**
     * Should create {@link Parser} instance from the specified {@link Lexer}.
     * 
     * @param lexer
     * @return
     */
    public abstract Parser createParser(Lexer lexer) throws Exception;

    /**
     * Should return list of all {@link RecognitionException}s from the specified {@link Lexer}.
     * 
     * @param lexer
     * @return
     */
    public abstract List<RecognitionException> getExceptions(Lexer lexer);

}
