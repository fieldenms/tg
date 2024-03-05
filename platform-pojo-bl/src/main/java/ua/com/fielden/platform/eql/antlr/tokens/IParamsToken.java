package ua.com.fielden.platform.eql.antlr.tokens;

import ua.com.fielden.platform.utils.CollectionUtil;

import java.util.List;

import static ua.com.fielden.platform.eql.antlr.EQLLexer.IPARAMS;

public final class IParamsToken extends AbstractParameterisedEqlToken {

    public final List<String> params;

    public IParamsToken(final List<String> params) {
        super(IPARAMS, "iParams");
        this.params = params;
    }

    public String parametersText() {
        return CollectionUtil.toString(params, ", ");
    }

}
