package ua.com.fielden.platform.eql.antlr.tokens;

import com.google.common.collect.ImmutableList;
import ua.com.fielden.platform.utils.CollectionUtil;

import java.util.List;
import java.util.Objects;

import static ua.com.fielden.platform.eql.antlr.EQLLexer.ANYOFIPARAMS;

public final class AnyOfIParamsToken extends AbstractParameterisedEqlToken {

    public final List<String> params;

    public AnyOfIParamsToken(final List<String> params) {
        super(ANYOFIPARAMS, "anyOfIParams");
        this.params = ImmutableList.copyOf(params);
    }

    public String parametersText() {
        return CollectionUtil.toString(params, ", ");
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof AnyOfIParamsToken that &&
                Objects.equals(params, that.params);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(params);
    }

}
