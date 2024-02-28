package ua.com.fielden.platform.entity.query.model;

import java.util.List;

import org.antlr.v4.runtime.Token;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory;
import ua.com.fielden.platform.utils.Pair;

public class EntityResultQueryModel<T extends AbstractEntity<?>> extends SingleResultQueryModel<T> {

    protected EntityResultQueryModel() {
    }

    public EntityResultQueryModel(final List<? extends Token> tokens, final Class<T> resultType, final boolean yieldAll) {
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
