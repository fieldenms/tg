package ua.com.fielden.platform.entity.query.model;

import ua.com.fielden.platform.eql.antlr.EQLLexer;
import ua.com.fielden.platform.eql.antlr.tokens.ValToken;
import ua.com.fielden.platform.eql.antlr.tokens.util.ListTokenSource;

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

    /**
     * Determines whether this model has a simple form like: {@code expr val(x) end}.
     */
    public boolean containsSingleValueToken() {
        final var tokens = tokenSource.tokens();
        // Not the most robust solution but it is simple.
        // Ideally, the callers of this method would rely on some mechanism of "expression simplification".
        // TODO Adjust after any change to EQL's grammar that affects this kind of expressions.
        return tokens.size() == 3 && tokens.get(1) instanceof ValToken;
    }

}
