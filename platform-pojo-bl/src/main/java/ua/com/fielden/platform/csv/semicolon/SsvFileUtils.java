package ua.com.fielden.platform.csv.semicolon;

import java.io.IOException;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;

import ua.com.fielden.platform.csv.IParserCreator;

/**
 * Contains utility static methods for parsing semicolon-separated-value lines etc.
 *
 * @author TG Team
 */
public final class SsvFileUtils implements IParserCreator<SsvParser> {

    /**
     * Creates SSV parser.
     *
     * @param line
     * @return
     * @throws IOException
     */
    @Override
    public SsvParser createParser(final String line) throws Exception {
	return createParser(createLexer(line));
    }

    public SsvParser createParser(final SsvLexer lexer) throws IOException {
	final CommonTokenStream tokens = new CommonTokenStream(lexer);
	final SsvParser parser = new SsvParser(tokens);
	return parser;
    }

    /**
     * Creates lexer for SSV parser.
     *
     * @param testString
     * @return
     * @throws IOException
     */
    public SsvLexer createLexer(final String line) throws IOException {
	final CharStream stream = new ANTLRStringStream(line);
	final SsvLexer lexer = new SsvLexer(stream);
	return lexer;
    }
}
