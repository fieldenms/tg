package ua.com.fielden.platform.eql.antlr.tokens;

import org.antlr.v4.runtime.CommonToken;
import ua.com.fielden.platform.eql.antlr.EQLLexer;

public final class AsToken extends CommonToken {

    public final String alias;

    public AsToken(final String alias) {
        super(EQLLexer.AS, "as");
        this.alias = alias;
    }

    @Override
    public String getText() {
        return "as(\"%s\")".formatted(alias);
    }

}
