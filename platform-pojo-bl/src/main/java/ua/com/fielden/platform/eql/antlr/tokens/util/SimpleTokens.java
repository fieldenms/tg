package ua.com.fielden.platform.eql.antlr.tokens.util;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;
import ua.com.fielden.platform.eql.antlr.EQLLexer;
import ua.com.fielden.platform.eql.antlr.exceptions.EqlSyntaxException;

/**
 * Provides access to all <i>simple</i> (non-parameterised) EQL tokens.
 */
public final class SimpleTokens {

    /**
     * Storage of singleton instances for <i>simple</i> tokens.
     * <p>
     * Array is used instead of a Map for efficiency. Token types are used as array indices, taking advantage of token
     * types being sequential integers.
     * <p>
     * <b>Caveat</b>: resulting token instances don't have "position" information (which is not needed anyway).
     * <p>
     * <b>Caveat</b>: although this array has room for all EQL tokens, it is used only for <i>simple</i> ones
     * (parameterised tokens are created via custom ANTLR token types).
     */
    private static final Token[] tokens = new Token[EQLLexer.VOCABULARY.getMaxTokenType() + 1];

    /**
     * Returns a simple, non-parameterised token corresponding to the given type.
     */
    public static Token token(final int type) {
        final int maxTokenType = EQLLexer.VOCABULARY.getMaxTokenType();
        if (type < 0 || type > maxTokenType) {
            throw new EqlSyntaxException(
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
