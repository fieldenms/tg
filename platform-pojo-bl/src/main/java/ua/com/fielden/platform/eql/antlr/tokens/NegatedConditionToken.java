package ua.com.fielden.platform.eql.antlr.tokens;

import org.antlr.v4.runtime.CommonToken;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.eql.antlr.EQLLexer;

public final class NegatedConditionToken extends CommonToken {

    public final ConditionModel model;

    public NegatedConditionToken(final ConditionModel model) {
        super(EQLLexer.NEGATEDCONDITION, "negatedCondition");
        this.model = model;
    }

}
