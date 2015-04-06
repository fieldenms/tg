package ua.com.fielden.platform.web.centre.api.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder0Ordering;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder2WithPropAction;
import ua.com.fielden.platform.web.centre.api.resultset.PropDef;

class ResultSetBuilder<T extends AbstractEntity<?>> implements IResultSetBuilder<T> {

    private final EntityCentreBuilder<T> builder;

    public ResultSetBuilder(final EntityCentreBuilder<T> builder) {
        this.builder = builder;
    }

    @Override
    public IResultSetBuilder0Ordering<T> addProp(final String propName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IResultSetBuilder2WithPropAction<T> addProp(final PropDef<?> propDef) {
        // TODO Auto-generated method stub
        return null;
    }

}
