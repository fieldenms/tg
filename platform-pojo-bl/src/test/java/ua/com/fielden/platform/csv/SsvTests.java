package ua.com.fielden.platform.csv;

import java.util.List;

import org.antlr.runtime.Lexer;
import org.antlr.runtime.Parser;
import org.antlr.runtime.RecognitionException;

import ua.com.fielden.platform.csv.semicolon.SsvFileUtils;
import ua.com.fielden.platform.csv.semicolon.SsvLexer;
import ua.com.fielden.platform.csv.semicolon.SsvParser;

public class SsvTests extends CharSeparatedValuesTest {

    private final SsvFileUtils utils;

    public SsvTests() {
	super(";");
	utils = new SsvFileUtils();
    }

    @Override
    public Lexer createLexer(final String line) throws Exception {
	return utils.createLexer(line);
    }

    @Override
    public Parser createParser(final Lexer lexer) throws Exception {
	return utils.createParser((SsvLexer) lexer);
    }

    @Override
    public List<RecognitionException> getExceptions(final Lexer lexer) {
	return ((SsvLexer) lexer).getExceptions();
    }

    @Override
    public List<String> parse(final Parser parser) throws Exception {
	return ((SsvParser) parser).line();
    }

}
