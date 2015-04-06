package ua.com.fielden.platform.web.centre.api.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.crit.IMultiValueCritSelector;
import ua.com.fielden.platform.web.centre.api.crit.IMutliValueAutocompleterBuilder;
import ua.com.fielden.platform.web.centre.api.crit.ISelectionCriteriaBuilder;
import ua.com.fielden.platform.web.centre.api.crit.defaults.IMultiBooleanDefaultValueAssigner;
import ua.com.fielden.platform.web.centre.api.crit.defaults.IMultiStringDefaultValueAssigner;

/**
 * A package private helper class to decompose the task of implementing the Entity Centre DSL.
 * It has direct access to protected fields in {@link EntityCentreBuilder}.
 *
 * @author TG Team
 *
 * @param <T>
 */
class SelectionCriteriaBuilderAsMulti<T extends AbstractEntity<?>> implements IMultiValueCritSelector<T> {

    private final EntityCentreBuilder<T> builder;
    private final ISelectionCriteriaBuilder<T> selectionCritBuilder;


    public SelectionCriteriaBuilderAsMulti(final EntityCentreBuilder<T> builder, final ISelectionCriteriaBuilder<T> selectionCritBuilder) {
        this.builder = builder;
        this.selectionCritBuilder = selectionCritBuilder;
    }


    @Override
    public IMutliValueAutocompleterBuilder<T> autocompleter() {
        return new SelectionCriteriaBuilderAsMultiString<T>(builder, selectionCritBuilder);
    }


    @Override
    public IMultiStringDefaultValueAssigner<T> text() {
        return new SelectionCriteriaBuilderAsMultiString<T>(builder, selectionCritBuilder);
    }


    @Override
    public IMultiBooleanDefaultValueAssigner<T> bool() {
        return new SelectionCriteriaBuilderAsMultiBool<T>(builder, selectionCritBuilder);
    }

}
