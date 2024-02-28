package ua.com.fielden.platform.entity.query.model;

import org.antlr.v4.runtime.Token;
import ua.com.fielden.platform.eql.antlr.EQLLexer;

import java.util.List;

/**
 * Represents a computational model for expressions, which can be used together with entity query API.
 *
 * @author TG Team
 *
 */
public class ExpressionModel extends AbstractModel {

    protected ExpressionModel() {
    }

    public ExpressionModel(final List<? extends Token> tokens) {
        super(tokens);
    }

    // TODO rather than use this method, compile as standalone expression and use the result
    public boolean containsSingleValueToken() {
        return tokens.size() == 1 && tokens.getFirst().getType() == EQLLexer.VAL;
    }

}
