package ua.com.fielden.platform.eql.antlr.tokens.util;

import org.antlr.v4.runtime.Token;

import java.util.List;

import static java.util.Collections.unmodifiableList;

/**
 * Provides the following enhancements:
 * <ul>
 *   <li> Read access to the token list via {@link #tokens()}.
 *   <li> Ability to restart the source so it can be read again (via {@link #restart()}.
 * </ul>
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

    /**
     * If this source is positioned at the first token, returns this instance.
     * Otherwise, returns a new instance equivalent to this one but repositioned at the first token.
     * Use this method to ensure that the token source is "fresh".
     */
    public ListTokenSource restart() {
        return i == 0 ? this : new ListTokenSource(tokens, getSourceName());
    }

}
