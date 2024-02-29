package ua.com.fielden.platform.eql.antlr;

import org.antlr.v4.runtime.Token;
import ua.com.fielden.platform.entity.query.QuerySourceCodeInfo;

import java.util.List;

import static java.util.Collections.unmodifiableList;

/**
 * Provides read access to the token list via {@link #tokens()}.
 */
public final class ListTokenSource extends org.antlr.v4.runtime.ListTokenSource {

    public ListTokenSource(final List<? extends Token> tokens) {
        super(tokens);
    }

    public ListTokenSource(final List<? extends Token> tokens, final String sourceName) {
        super(tokens, sourceName);
    }

    public List<Token> tokens() {
        return unmodifiableList(tokens);
    }

}
