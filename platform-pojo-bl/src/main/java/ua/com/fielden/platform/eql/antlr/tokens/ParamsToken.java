package ua.com.fielden.platform.eql.antlr.tokens;

import ua.com.fielden.platform.utils.CollectionUtil;

import java.util.List;

import static ua.com.fielden.platform.eql.antlr.EQLLexer.PARAMS;

public final class ParamsToken extends AbstractParameterisedEqlToken {

    public final List<String> params;

    public ParamsToken(final List<String> params) {
        super(PARAMS, "params");
        this.params = params;
    }

    public String parametersText() {
        return CollectionUtil.toString(params, ", ");
    }

}
