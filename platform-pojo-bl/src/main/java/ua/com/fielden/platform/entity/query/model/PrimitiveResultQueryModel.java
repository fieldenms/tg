package ua.com.fielden.platform.entity.query.model;

import org.antlr.v4.runtime.Token;

import java.util.List;

public class PrimitiveResultQueryModel extends SingleResultQueryModel {

    protected PrimitiveResultQueryModel() {
    }

    public PrimitiveResultQueryModel(final List<? extends Token> tokens) {
        super(tokens, null, false);
    }

    @Override
    public PrimitiveResultQueryModel setFilterable(boolean filterable) {
        super.setFilterable(filterable);
        return this;
    }

}
