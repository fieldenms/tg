package ua.com.fielden.platform.csv;

import java.util.List;

import org.antlr.runtime.Lexer;
import org.antlr.runtime.Parser;
import org.antlr.runtime.RecognitionException;

import ua.com.fielden.platform.csv.comma.CsvFileUtils;
import ua.com.fielden.platform.csv.comma.CsvLexer;
import ua.com.fielden.platform.csv.comma.CsvParser;

/**
 * This test was taken from http://www.antlr.org/wiki/display/ANTLR3/Test-Driven+Development+with+ANTLR and modified by 01es.
 */
public class CsvTests extends CharSeparatedValuesTest {

    private final CsvFileUtils utils;

    public CsvTests() {
	super(",");
	utils = new CsvFileUtils();
    }

    @Override
    public Lexer createLexer(final String line) throws Exception {
	return utils.createLexer(line);
    }

    @Override
    public Parser createParser(final Lexer lexer) throws Exception {
	return utils.createParser((CsvLexer) lexer);
    }

    @Override
    public List<RecognitionException> getExceptions(final Lexer lexer) {
	return ((CsvLexer) lexer).getExceptions();
    }

    @Override
    public List<String> parse(final Parser parser) throws Exception {
	return ((CsvParser) parser).line();
    }
}
