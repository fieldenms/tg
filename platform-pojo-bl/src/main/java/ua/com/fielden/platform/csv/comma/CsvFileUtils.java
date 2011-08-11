/**
 *
 */
package ua.com.fielden.platform.csv.comma;

import java.io.IOException;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;

import ua.com.fielden.platform.csv.IParserCreator;

/**
 * Contains utility static methods for parsing .csv files, lines etc.
 *
 * @author TG Team
 */
public final class CsvFileUtils implements IParserCreator<CsvParser> {

    /**
     * Creates CSV parser.
     *
     * @param line
     * @return
     * @throws IOException
     */
    @Override
    public CsvParser createParser(final String line) throws Exception {
	return createParser(createLexer(line));
    }

    public CsvParser createParser(final CsvLexer lexer) throws Exception {
	final CommonTokenStream tokens = new CommonTokenStream(lexer);
	final CsvParser parser = new CsvParser(tokens);
	return parser;
    }

    /**
     * Creates lexer for CSV parser.
     *
     * @param testString
     * @return
     * @throws IOException
     */
    public CsvLexer createLexer(final String line) throws Exception {
	final CharStream stream = new ANTLRStringStream(line);
	final CsvLexer lexer = new CsvLexer(stream);
	return lexer;
    }

}
