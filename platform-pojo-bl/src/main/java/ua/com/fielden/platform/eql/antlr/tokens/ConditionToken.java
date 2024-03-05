package ua.com.fielden.platform.eql.antlr.tokens;

import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.eql.antlr.tokens.util.TokensFormatter;

import static ua.com.fielden.platform.eql.antlr.EQLLexer.CONDITION;

public final class ConditionToken extends AbstractParameterisedEqlToken {

    public final ConditionModel model;

    public ConditionToken(final ConditionModel model) {
        super(CONDITION, "condition");
        this.model = model;
    }

    @Override
    public String parametersText() {
        return TokensFormatter.getInstance().format(model.getTokenSource());
    }

}
