package ua.com.fielden.platform.eql.antlr.tokens;

import org.antlr.v4.runtime.CommonToken;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.eql.antlr.EQLLexer;

public final class ConditionToken extends CommonToken {

    public final ConditionModel model;

    public ConditionToken(final ConditionModel model) {
        super(EQLLexer.CONDITION, "condition");
        this.model = model;
    }

}
