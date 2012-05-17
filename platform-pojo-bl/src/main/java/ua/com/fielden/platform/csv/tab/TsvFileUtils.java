package ua.com.fielden.platform.csv.tab;

import java.io.IOException;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;

import ua.com.fielden.platform.csv.IParserCreator;

public class TsvFileUtils implements IParserCreator<TsvParser> {

    @Override
    public TsvParser createParser(final String line) throws Exception {
	return createParser(createLexer(line));
    }

    /**
     * Creates parser for tab-separated values parser.
     *
     * @param testString
     * @return
     * @throws IOException
     */
    public TsvParser createParser(final TsvLexer lexer) throws IOException {
	final CommonTokenStream tokens = new CommonTokenStream(lexer);
	final TsvParser parser = new TsvParser(tokens);
	return parser;
    }

    /**
     * Creates lexer for tab-separated values parser.
     *
     * @param testString
     * @return
     * @throws IOException
     */
    public TsvLexer createLexer(final String line) throws IOException {
	final CharStream stream = new ANTLRStringStream(line);
	final TsvLexer lexer = new TsvLexer(stream);
	return lexer;
    }

}
