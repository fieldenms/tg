package ua.com.fielden.platform.entity.query.model;

import static ua.com.fielden.platform.entity.query.fluent.enums.Functions.COUNT_ALL;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.COLLECTIONAL_FUNCTION;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.EXPR_TOKENS;

import java.util.List;

import ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory;
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
    
    /**
     * Returns true if any of the aggregate functions ({@code maxOf()}, {@code minOf()}, {@code sumOf()}, {@code countOf()}, {@code avgOf()}, {@code countAll()}, {@code sumOfDistinct()}, {@code countOfDistinct()}, {@code avgOfDistinct()}) is explicitly or implicitly present in the given expression.
     * 
     * By implicit presence here it is meant that it may be part of another expression model included into this one at any level of nesting.
     * 
     * @return
     */
    public boolean containsAggregations() {
        return getTokens().stream().anyMatch(t -> t.getKey() == COLLECTIONAL_FUNCTION || t.getValue() == COUNT_ALL || t.getKey() == EXPR_TOKENS && ((ExpressionModel) t.getValue()).containsAggregations());
    }
}