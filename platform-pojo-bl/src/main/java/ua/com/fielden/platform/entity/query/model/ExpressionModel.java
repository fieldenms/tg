package ua.com.fielden.platform.entity.query.model;

import org.antlr.v4.runtime.Token;
import ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory;

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

    public boolean containsSingleValueToken() {
        // TODO
        throw new UnsupportedOperationException();
//        return getTokens().size() == 1 && getTokens().get(0).getKey() == TokenCategory.VAL;
    }
}
