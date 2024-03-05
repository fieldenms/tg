package ua.com.fielden.platform.eql.antlr.tokens;

import org.apache.commons.lang3.StringUtils;

import static ua.com.fielden.platform.eql.antlr.EQLLexer.ASREQUIRED;

public final class AsRequiredToken extends AbstractParameterisedEqlToken {

    public final String alias;

    public AsRequiredToken(final String alias) {
        super(ASREQUIRED, "asRequired");
        this.alias = alias;
    }

    @Override
    public String parametersText() {
        return StringUtils.wrap(alias, '"');
    }

}
