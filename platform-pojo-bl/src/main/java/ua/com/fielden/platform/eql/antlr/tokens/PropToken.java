package ua.com.fielden.platform.eql.antlr.tokens;

import static ua.com.fielden.platform.eql.antlr.EQLLexer.PROP;

public final class PropToken extends AbstractParameterisedEqlToken {

    public final String propPath;

    public PropToken(String propPath) {
        super(PROP, "prop");
        this.propPath = propPath;
    }

    public String parametersText() {
        return "\"%s\"".formatted(propPath);
    }

}
