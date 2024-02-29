package ua.com.fielden.platform.entity.query.model;

import ua.com.fielden.platform.eql.antlr.EQLLexer;
import ua.com.fielden.platform.eql.antlr.ListTokenSource;

/**
 * Represents a computational model for expressions, which can be used together with entity query API.
 *
 * @author TG Team
 *
 */
public class ExpressionModel extends AbstractModel {

    public ExpressionModel(final ListTokenSource tokens) {
        super(tokens);
    }

    // TODO rather than use this method, compile as standalone expression and use the result
    public boolean containsSingleValueToken() {
        final var tokens = tokenSource.tokens();
        return tokens.size() == 1 && tokens.getFirst().getType() == EQLLexer.VAL;
    }

}
