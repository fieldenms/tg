package ua.com.fielden.platform.csv;

import java.util.List;

import org.antlr.runtime.Lexer;
import org.antlr.runtime.Parser;
import org.antlr.runtime.RecognitionException;

import ua.com.fielden.platform.csv.vertical_bar.VbsvFileUtils;
import ua.com.fielden.platform.csv.vertical_bar.VbsvLexer;
import ua.com.fielden.platform.csv.vertical_bar.VbsvParser;

/**
 * Test for {@link VbsvFileUtils} class.
 *
 * @author TG Team
 *
 */
public class VbsvTests extends CharSeparatedValuesTest {

    private final VbsvFileUtils utils;

    public VbsvTests() {
	super("|");
	utils = new VbsvFileUtils();
    }

    @Override
    public Lexer createLexer(final String line) throws Exception {
	return utils.createLexer(line);
    }

    @Override
    public Parser createParser(final Lexer lexer) throws Exception {
	return utils.createParser((VbsvLexer) lexer);
    }

    @Override
    public List<RecognitionException> getExceptions(final Lexer lexer) {
	return ((VbsvLexer) lexer).getExceptions();
    }

    @Override
    public List<String> parse(final Parser parser) throws Exception {
	return ((VbsvParser) parser).line();
    }
}
