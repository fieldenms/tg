package ua.com.fielden.platform.eql.antlr.tokens.util;

import org.antlr.v4.runtime.Token;
import ua.com.fielden.platform.eql.antlr.ListTokenSource;

import java.util.Collection;

public interface TokensFormatter {

    String format(final Collection<? extends Token> tokens);

    default String format(final ListTokenSource tokenSource) {
        return format(tokenSource.tokens());
    }

    String formatLiteral(final Object object);

    static TokensFormatter getInstance() {
        return SimpleTokensFormatter.INSTANCE;
    }

}
