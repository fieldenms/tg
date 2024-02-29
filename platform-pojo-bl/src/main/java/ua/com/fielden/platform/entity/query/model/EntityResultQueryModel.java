package ua.com.fielden.platform.entity.query.model;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.antlr.ListTokenSource;

public class EntityResultQueryModel<T extends AbstractEntity<?>> extends SingleResultQueryModel<T> {

    public EntityResultQueryModel(final ListTokenSource tokens, final Class<T> resultType, final boolean yieldAll) {
        super(tokens, resultType, yieldAll);
    }

    @Override
    public EntityResultQueryModel<T> setFilterable(boolean filterable) {
        super.setFilterable(filterable);
        return this;
    }

    public EntityResultQueryModel<T> setShouldMaterialiseCalcPropsAsColumnsInSqlQuery(final boolean value) {
        this.shouldMaterialiseCalcPropsAsColumnsInSqlQuery = value;
        return this;
    }
}
