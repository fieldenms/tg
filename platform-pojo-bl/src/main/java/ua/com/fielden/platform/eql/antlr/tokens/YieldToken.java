package ua.com.fielden.platform.eql.antlr.tokens;

import static ua.com.fielden.platform.eql.antlr.EQLLexer.YIELD;

public final class YieldToken extends AbstractParameterisedEqlToken {

    public final String yieldName;

    public YieldToken(String yieldName) {
        super(YIELD, "yield");
        this.yieldName = yieldName;
    }

    public String parametersText() {
        return "\"%s\"".formatted(yieldName);
    }

}
