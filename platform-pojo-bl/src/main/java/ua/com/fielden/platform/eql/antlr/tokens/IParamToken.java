package ua.com.fielden.platform.eql.antlr.tokens;

import org.apache.commons.lang3.StringUtils;

import static ua.com.fielden.platform.eql.antlr.EQLLexer.IPARAM;

public final class IParamToken extends AbstractParameterisedEqlToken {

    public final String paramName;

    public IParamToken(final String paramName) {
        super(IPARAM, "iParam");
        this.paramName = paramName;
    }

    @Override
    public String parametersText() {
        return StringUtils.wrap(paramName, '"');
    }

}
