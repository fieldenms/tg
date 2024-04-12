package ua.com.fielden.platform.eql.antlr.tokens.util;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;
import ua.com.fielden.platform.eql.antlr.EQLLexer;

public final class SimpleTokens {

    // Use a single Token instance for all tokens of a given type (denoted by an integer).
    // One consequence is that tokens don't have "position" information, but it is not needed anyway.
    private static final Token[] tokens = new Token[EQLLexer.VOCABULARY.getMaxTokenType() + 1];

    /**
     * Returns a simple, non-parameterised token corresponding to the given type.
     */
    public static Token token(int type) {
        final int maxTokenType = EQLLexer.VOCABULARY.getMaxTokenType();
        if (type < 0 || type > maxTokenType) {
            throw new IllegalArgumentException(
                    "Token type %s does not belong to the vocabulary (valid range: [0, %s])".formatted(
                            type, maxTokenType));
        }

        final Token token = tokens[type];
        if (token != null) {
            return token;
        }

        String name = EQLLexer.VOCABULARY.getLiteralName(type);
        if (name != null) {
            name = name.replace("'", "");
        } else {
            name = EQLLexer.VOCABULARY.getSymbolicName(type);
        }
        final Token newToken = new CommonToken(type, name != null ? name : Integer.toString(type));

        tokens[type] = newToken;

        return newToken;
    }

    private SimpleTokens() {}

}
