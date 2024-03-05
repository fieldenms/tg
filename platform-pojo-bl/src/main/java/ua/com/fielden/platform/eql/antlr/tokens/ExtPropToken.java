package ua.com.fielden.platform.eql.antlr.tokens;

import org.apache.commons.lang3.StringUtils;

import static ua.com.fielden.platform.eql.antlr.EQLLexer.EXTPROP;

public final class ExtPropToken extends AbstractParameterisedEqlToken {

    public final String propPath;

    public ExtPropToken(final String propPath) {
        super(EXTPROP, "extProp");
        this.propPath = propPath;
    }

    @Override
    public String parametersText() {
        return StringUtils.wrap(propPath, '"');
    }

}
