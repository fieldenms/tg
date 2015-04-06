package ua.com.fielden.platform.web.centre.api.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.crit.layout.ILayoutConfigWithResultsetSupport;
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
class SelectionCriteriaLayoutBuilder<T extends AbstractEntity<?>> extends ResultSetBuilder<T> implements ILayoutConfigWithResultsetSupport<T> {

    private final EntityCentreBuilder<T> builder;


    public SelectionCriteriaLayoutBuilder(final EntityCentreBuilder<T> builder) {
        super(builder);
        this.builder = builder;
    }


    @Override
    public ILayoutConfigWithResultsetSupport<T> setLayoutFor(final Device device, final Orientation orientation, final String flexString) {
        this.builder.selectionCriteriaLayout.whenMedia(device, orientation).set(flexString);
        return this;
    }

}
