package ua.com.fielden.platform.eql.antlr.tokens;

import ua.com.fielden.platform.utils.CollectionUtil;

import java.util.List;

import static ua.com.fielden.platform.eql.antlr.EQLLexer.ANYOFPARAMS;

public final class AnyOfParamsToken extends AbstractParameterisedEqlToken {

    public final List<String> params;

    public AnyOfParamsToken(final List<String> params) {
        super(ANYOFPARAMS, "anyOfParams");
        this.params = params;
    }

    public String parametersText() {
        return CollectionUtil.toString(params, ", ");
    }

}
