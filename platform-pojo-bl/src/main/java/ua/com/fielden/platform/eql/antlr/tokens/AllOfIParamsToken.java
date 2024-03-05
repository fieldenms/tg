package ua.com.fielden.platform.eql.antlr.tokens;

import ua.com.fielden.platform.utils.CollectionUtil;

import java.util.List;

import static ua.com.fielden.platform.eql.antlr.EQLLexer.ALLOFIPARAMS;

public final class AllOfIParamsToken extends AbstractParameterisedEqlToken {

    public final List<String> params;

    public AllOfIParamsToken(final List<String> params) {
        super(ALLOFIPARAMS, "allOfIParams");
        this.params = params;
    }

    public String parametersText() {
        return CollectionUtil.toString(params, ", ");
    }

}
