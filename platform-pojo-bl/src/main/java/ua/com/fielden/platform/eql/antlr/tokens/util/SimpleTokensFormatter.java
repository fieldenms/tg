package ua.com.fielden.platform.eql.antlr.tokens.util;

import org.antlr.v4.runtime.Token;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.stream.Collectors;

public final class SimpleTokensFormatter implements TokensFormatter {

    static final SimpleTokensFormatter INSTANCE = new SimpleTokensFormatter();

    @Override
    public String format(final Collection<? extends Token> tokens) {
        return tokens.stream().map(Token::getText).collect(Collectors.joining(" "));
    }

    @Override
    public String formatLiteral(final Object object) {
        return object instanceof String s ? StringUtils.wrap(s, '"') : String.valueOf(object);
    }

}
