package ua.com.fielden.platform.entity.query.model;

import ua.com.fielden.platform.eql.antlr.ListTokenSource;

public class PrimitiveResultQueryModel extends SingleResultQueryModel {

    public PrimitiveResultQueryModel(final ListTokenSource tokens) {
        super(tokens, null, false);
    }

    @Override
    public PrimitiveResultQueryModel setFilterable(boolean filterable) {
        super.setFilterable(filterable);
        return this;
    }

}
