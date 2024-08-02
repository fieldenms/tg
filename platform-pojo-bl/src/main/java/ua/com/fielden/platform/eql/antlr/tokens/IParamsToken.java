package ua.com.fielden.platform.eql.antlr.tokens;

import ua.com.fielden.platform.utils.CollectionUtil;

import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;
import static ua.com.fielden.platform.eql.antlr.EQLLexer.IPARAMS;

public final class IParamsToken extends AbstractParameterisedEqlToken {

    public final List<String> params;

    public IParamsToken(final List<String> params) {
        super(IPARAMS, "iParams");
        this.params = requireNonNull(params);
    }

    public String parametersText() {
        return CollectionUtil.toString(params, ", ");
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof IParamsToken that &&
                Objects.equals(params, that.params);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(params);
    }

}
