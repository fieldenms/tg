package ua.com.fielden.platform.web.centre.api.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.crit.layout.ILayoutConfigWithResultsetSupport;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder0Ordering;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetBuilder2WithPropAction;
import ua.com.fielden.platform.web.centre.api.resultset.PropDef;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.interfaces.ILayout.Orientation;

/**
 * A package private helper class to decompose the task of implementing the Entity Centre DSL.
 * It has direct access to protected fields in {@link EntityCentreBuilder}.
 *
 * @author TG Team
 *
 * @param <T>
 */
class SelectionCriteriaLayoutBuilder<T extends AbstractEntity<?>> implements ILayoutConfigWithResultsetSupport<T> {

    private final EntityCentreBuilder<T> builder;


    public SelectionCriteriaLayoutBuilder(final EntityCentreBuilder<T> builder) {
        this.builder = builder;
    }


    @Override
    public ILayoutConfigWithResultsetSupport<T> setLayoutFor(final Device device, final Orientation orientation, final String flexString) {
        this.builder.selectionCriteriaLayout.whenMedia(device, orientation).set(flexString);
        return this;
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
