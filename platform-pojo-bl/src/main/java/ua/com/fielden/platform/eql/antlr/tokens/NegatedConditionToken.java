package ua.com.fielden.platform.eql.antlr.tokens;

import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.eql.antlr.tokens.util.TokensFormatter;

import static ua.com.fielden.platform.eql.antlr.EQLLexer.NEGATEDCONDITION;

public final class NegatedConditionToken extends AbstractParameterisedEqlToken {

    public final ConditionModel model;

    public NegatedConditionToken(final ConditionModel model) {
        super(NEGATEDCONDITION, "negatedCondition");
        this.model = model;
    }

    @Override
    public String parametersText() {
        return TokensFormatter.getInstance().format(model.getTokenSource());
    }

}
