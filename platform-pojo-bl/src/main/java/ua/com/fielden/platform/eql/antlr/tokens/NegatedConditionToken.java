package ua.com.fielden.platform.eql.antlr.tokens;

import ua.com.fielden.platform.entity.query.model.ConditionModel;

import java.util.Objects;

import static java.util.Objects.requireNonNull;
import static ua.com.fielden.platform.eql.antlr.EQLLexer.NEGATEDCONDITION;
import static ua.com.fielden.platform.eql.antlr.tokens.util.TokensFormatter.getInstance;

public final class NegatedConditionToken extends AbstractParameterisedEqlToken {

    public final ConditionModel model;

    public NegatedConditionToken(final ConditionModel model) {
        super(NEGATEDCONDITION, "negatedCondition");
        this.model = requireNonNull(model);
    }

    @Override
    public String parametersText() {
        return getInstance().format(model.getTokenSource());
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof NegatedConditionToken that &&
                Objects.equals(model, that.model);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(model);
    }

}
