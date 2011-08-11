package ua.com.fielden.platform.csv.vertical_bar;

import java.io.IOException;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;

import ua.com.fielden.platform.csv.IParserCreator;

/**
 * Contains utility static methods for parsing lines separated by vertical bar character '|'.
 *
 * @author TG Team
 */
public final class VbsvFileUtils implements IParserCreator<VbsvParser> {

    /**
     * Creates vertical-bar separated value line parser.
     *
     * @param line
     * @return
     * @throws IOException
     */
    @Override
    public VbsvParser createParser(final String line) throws Exception {
	return createParser(createLexer(line));
    }

    public VbsvParser createParser(final VbsvLexer lexer) throws IOException {
	final CommonTokenStream tokens = new CommonTokenStream(lexer);
	final VbsvParser parser = new VbsvParser(tokens);
	return parser;
    }

    /**
     * Creates lexer for vertical-bar separated value line parser.
     *
     * @param testString
     * @return
     * @throws IOException
     */
    public VbsvLexer createLexer(final String line) throws IOException {
	final CharStream stream = new ANTLRStringStream(line);
	final VbsvLexer lexer = new VbsvLexer(stream);
	return lexer;
    }

}
