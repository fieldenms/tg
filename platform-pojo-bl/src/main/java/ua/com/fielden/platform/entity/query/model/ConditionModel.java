package ua.com.fielden.platform.entity.query.model;

import java.util.List;

import ua.com.fielden.platform.entity.query.fluent.TokenCategory;
import ua.com.fielden.platform.utils.Pair;

/**
 * Represents a computational model for condition, which can be used together with entity query API.
 * 
 * @author TG Team
 * 
 */
public class ConditionModel extends AbstractModel {
    protected ConditionModel() {
    }

    public ConditionModel(final List<Pair<TokenCategory, Object>> tokens) {
        super(tokens);
    }
}