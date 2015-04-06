package ua.com.fielden.platform.web.centre.api.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.crit.IMultiValueCritSelector;
import ua.com.fielden.platform.web.centre.api.crit.IRangeValueCritSelector;
import ua.com.fielden.platform.web.centre.api.crit.ISelectionCritKindSelector;
import ua.com.fielden.platform.web.centre.api.crit.ISelectionCriteriaBuilder;
import ua.com.fielden.platform.web.centre.api.crit.ISingleValueCritSelector;

/**
 * A package private helper class to decompose the task of implementing the Entity Centre DSL.
 * It has direct access to protected fields in {@link EntityCentreBuilder}.
 *
 * @author TG Team
 *
 * @param <T>
 */
class SelectionCriteriaBuilder<T extends AbstractEntity<?>> implements ISelectionCritKindSelector<T> {

    private final EntityCentreBuilder<T> builder;
    private final ISelectionCriteriaBuilder<T> selectionCritBuilder;


    public SelectionCriteriaBuilder(final EntityCentreBuilder<T> builder, final ISelectionCriteriaBuilder<T> selectionCritBuilder) {
        this.builder = builder;
        this.selectionCritBuilder = selectionCritBuilder;
    }


    @Override
    public IMultiValueCritSelector<T> asMulti() {
        return new SelectionCriteriaBuilderAsMulti<T>(builder, selectionCritBuilder);
    }


    @Override
    public ISingleValueCritSelector<T> asSingle() {
        return new SelectionCriteriaBuilderAsSingle<T>(builder, selectionCritBuilder);
    }


    @Override
    public IRangeValueCritSelector<T> asRange() {
        return new SelectionCriteriaBuilderAsRange<T>(builder, selectionCritBuilder);
    }

}
