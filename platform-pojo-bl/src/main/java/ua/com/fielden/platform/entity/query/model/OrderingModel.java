package ua.com.fielden.platform.entity.query.model;

import ua.com.fielden.platform.eql.antlr.ListTokenSource;

/**
 * Represents a computational model for expressions, which can be used together with entity query API.
 * 
 * @author TG Team
 * 
 */
public class OrderingModel extends AbstractModel {

    public OrderingModel(final ListTokenSource tokens) {
        super(tokens);
    }
}
