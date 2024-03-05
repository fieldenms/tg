package ua.com.fielden.platform.eql.antlr.tokens;

import ua.com.fielden.platform.utils.CollectionUtil;

import java.util.List;

import static ua.com.fielden.platform.eql.antlr.EQLLexer.ANYOFIPARAMS;

public final class AnyOfIParamsToken extends AbstractParameterisedEqlToken {

    public final List<String> params;

    public AnyOfIParamsToken(final List<String> params) {
        super(ANYOFIPARAMS, "anyOfIParams");
        this.params = params;
    }

    public String parametersText() {
        return CollectionUtil.toString(params, ", ");
    }

}
