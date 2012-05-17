package ua.com.fielden.platform.csv;

import java.util.List;

import org.antlr.runtime.Lexer;
import org.antlr.runtime.Parser;
import org.antlr.runtime.RecognitionException;

import ua.com.fielden.platform.csv.tab.TsvFileUtils;
import ua.com.fielden.platform.csv.tab.TsvLexer;
import ua.com.fielden.platform.csv.tab.TsvParser;

public class TsvTests extends CharSeparatedValuesTest {

    private final TsvFileUtils utils;

    public TsvTests() {
	super("\t");
	utils = new TsvFileUtils();
    }

    @Override
    public Lexer createLexer(final String line) throws Exception {
	return utils.createLexer(line);
    }

    @Override
    public Parser createParser(final Lexer lexer) throws Exception {
	return utils.createParser((TsvLexer)lexer);
    }

    @Override
    public List<String> parse(final Parser parser) throws Exception {
	return ((TsvParser) parser).line();
    }

    @Override
    public List<RecognitionException> getExceptions(final Lexer lexer) {
	return ((TsvLexer) lexer).getExceptions();
    }

}
