package ua.com.fielden.platform.eql.antlr.tokens;

import com.google.common.collect.ImmutableList;
import ua.com.fielden.platform.utils.CollectionUtil;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static ua.com.fielden.platform.entity.query.exceptions.EqlException.requireNotNullArgument;
import static ua.com.fielden.platform.eql.antlr.EQLLexer.IPARAMS;

public final class IParamsToken extends AbstractParameterisedEqlToken {

    public final List<String> params;

    public IParamsToken(final Collection<String> params) {
        super(IPARAMS, "iParams");
        requireNotNullArgument(params, "params");
        this.params = ImmutableList.copyOf(params);
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
