package ua.com.fielden.platform.eql.antlr.tokens;

import com.google.common.collect.ImmutableList;
import ua.com.fielden.platform.utils.CollectionUtil;

import java.util.List;
import java.util.Objects;

import static ua.com.fielden.platform.eql.antlr.EQLLexer.ANYOFPARAMS;

public final class AnyOfParamsToken extends AbstractParameterisedEqlToken {

    public final List<String> params;

    public AnyOfParamsToken(final List<String> params) {
        super(ANYOFPARAMS, "anyOfParams");
        this.params = ImmutableList.copyOf(params);
    }

    public String parametersText() {
        return CollectionUtil.toString(params, ", ");
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof AnyOfParamsToken that &&
                Objects.equals(params, that.params);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(params);
    }

}
