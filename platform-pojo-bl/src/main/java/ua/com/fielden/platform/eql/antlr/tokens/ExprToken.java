package ua.com.fielden.platform.eql.antlr.tokens;

import org.antlr.v4.runtime.CommonToken;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.eql.antlr.EQLLexer;

public final class ExprToken extends CommonToken {

    public final ExpressionModel model;

    public ExprToken(final ExpressionModel model) {
        super(EQLLexer.EXPR, "expr");
        this.model = model;
    }

}
