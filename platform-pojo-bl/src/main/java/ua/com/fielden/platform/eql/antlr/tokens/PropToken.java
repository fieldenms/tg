package ua.com.fielden.platform.eql.antlr.tokens;

import org.antlr.v4.runtime.CommonToken;
import ua.com.fielden.platform.eql.antlr.EQLLexer;

public final class PropToken extends CommonToken {

    public final String propPath;

    public PropToken(String propPath) {
        super(EQLLexer.PROP, "prop");
        this.propPath = propPath;
    }

    @Override
    public String getText() {
        return "prop(\"%s\")".formatted(propPath);
    }

}
