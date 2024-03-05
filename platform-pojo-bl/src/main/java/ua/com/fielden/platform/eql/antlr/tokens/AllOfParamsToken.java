package ua.com.fielden.platform.eql.antlr.tokens;

import ua.com.fielden.platform.utils.CollectionUtil;

import java.util.List;

import static ua.com.fielden.platform.eql.antlr.EQLLexer.ALLOFPARAMS;

public final class AllOfParamsToken extends AbstractParameterisedEqlToken {

    public final List<String> params;

    public AllOfParamsToken(final List<String> params) {
        super(ALLOFPARAMS, "allOfParams");
        this.params = params;
    }

    public String parametersText() {
        return CollectionUtil.toString(params, ", ");
    }

}
