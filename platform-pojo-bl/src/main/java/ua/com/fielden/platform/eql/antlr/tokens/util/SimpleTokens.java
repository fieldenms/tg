package ua.com.fielden.platform.eql.antlr.tokens.util;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;
import ua.com.fielden.platform.eql.antlr.EQLLexer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SimpleTokens {

    private static final Map<Integer, Token> tokens = new ConcurrentHashMap<>();

    /**
     * Returns a simple, non-parameterised token corresponding to the given type.
     */
    public static Token token(int type) {
        final int maxTokenType = EQLLexer.VOCABULARY.getMaxTokenType();
        if (type > maxTokenType) {
            throw new IllegalArgumentException(
                    "Token type %s does not belong to the vocabulary (max. token type = %s)".formatted(
                            type, maxTokenType));
        }

        return tokens.computeIfAbsent(type, typ -> {
            String name = EQLLexer.VOCABULARY.getLiteralName(typ);
            if (name != null) {
                name = name.replace("'", "");
            } else {
                name = EQLLexer.VOCABULARY.getSymbolicName(typ);
            }
            return new CommonToken(typ, name != null ? name : Integer.toString(type));
        });
    }

    private SimpleTokens() {}

}
