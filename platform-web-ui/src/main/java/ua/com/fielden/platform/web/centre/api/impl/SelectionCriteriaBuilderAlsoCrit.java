package ua.com.fielden.platform.web.centre.api.impl;

import java.util.Optional;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.crit.IAlsoCrit;
import ua.com.fielden.platform.web.centre.api.crit.ISelectionCriteriaBuilder;
import ua.com.fielden.platform.web.centre.api.crit.layout.ILayoutConfigWithResultsetSupport;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.interfaces.ILayout.Orientation;

/**
 * A package private helper class to decompose the task of implementing the Entity Centre DSL. It has direct access to protected fields in {@link EntityCentreBuilder}.
 *
 * @author TG Team
 *
 * @param <T>
 */
class SelectionCriteriaBuilderAlsoCrit<T extends AbstractEntity<?>> implements IAlsoCrit<T> {

    private final EntityCentreBuilder<T> builder;
    private final ISelectionCriteriaBuilder<T> selectionCritBuilder;

    public SelectionCriteriaBuilderAlsoCrit(final EntityCentreBuilder<T> builder, final ISelectionCriteriaBuilder<T> selectionCritBuilder) {
        this.builder = builder;
        this.selectionCritBuilder = selectionCritBuilder;
    }

    @Override
    public ISelectionCriteriaBuilder<T> also() {
        this.builder.currSelectionCrit = Optional.empty();
        return selectionCritBuilder;
    }

    @Override
    public ILayoutConfigWithResultsetSupport<T> setLayoutFor(final Device device, final Orientation orientation, final String flexString) {
        if (builder.selectionCriteria.size() == 0) {
            throw new IllegalArgumentException("Looks like out of sequence call as there are selection criteria to layout.");
        }

        return new SelectionCriteriaLayoutBuilder<T>(builder).setLayoutFor(device, orientation, flexString);
    }

}
