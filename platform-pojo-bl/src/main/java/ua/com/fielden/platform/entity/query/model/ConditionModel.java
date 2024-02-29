package ua.com.fielden.platform.entity.query.model;

import ua.com.fielden.platform.eql.antlr.ListTokenSource;

/**
 * Represents a computational model for condition, which can be used together with entity query API.
 * 
 * @author TG Team
 * 
 */
public class ConditionModel extends AbstractModel {

    public ConditionModel(final ListTokenSource tokens) {
        super(tokens);
    }
}
