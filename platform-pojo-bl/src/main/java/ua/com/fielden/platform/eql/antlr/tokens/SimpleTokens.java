package ua.com.fielden.platform.eql.antlr.tokens;

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
        return tokens.computeIfAbsent(type, typ -> new CommonToken(typ, EQLLexer.VOCABULARY.getDisplayName(typ)));
    }

    private SimpleTokens() {}

}
