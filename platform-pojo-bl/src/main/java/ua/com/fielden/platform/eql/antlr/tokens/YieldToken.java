package ua.com.fielden.platform.eql.antlr.tokens;

import org.antlr.v4.runtime.CommonToken;
import ua.com.fielden.platform.eql.antlr.EQLLexer;

public final class YieldToken extends CommonToken {

    public final String yieldName;

    public YieldToken(String yieldName) {
        super(EQLLexer.YIELD, "yield");
        this.yieldName = yieldName;
    }

    @Override
    public String getText() {
        return "yield(\"%s\")".formatted(yieldName);
    }

}
