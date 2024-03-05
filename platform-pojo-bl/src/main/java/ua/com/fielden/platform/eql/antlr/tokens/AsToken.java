package ua.com.fielden.platform.eql.antlr.tokens;

import org.apache.commons.lang3.StringUtils;

import static ua.com.fielden.platform.eql.antlr.EQLLexer.AS;

public final class AsToken extends AbstractParameterisedEqlToken {

    public final String alias;

    public AsToken(final String alias) {
        super(AS, "as");
        this.alias = alias;
    }

    public String parametersText() {
        return StringUtils.wrap(alias, '"');
    }

}
