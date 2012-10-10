package ua.com.fielden.platform.entity.query.model;

import java.util.List;

import ua.com.fielden.platform.entity.query.fluent.TokenCategory;
import ua.com.fielden.platform.utils.Pair;

/**
 * Represents a computational model for expressions, which can be used together with entity query API.
 *
 * @author TG Team
 *
 */
public class ExpressionModel extends AbstractModel {

    protected ExpressionModel() {
    }

    public ExpressionModel(final List<Pair<TokenCategory, Object>> tokens) {
	super(tokens);
    }

    public boolean containsSingleValueToken() {
	return getTokens().size() == 1 && getTokens().get(0).getKey() == TokenCategory.VAL;
    }
}