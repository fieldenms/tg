package ua.com.fielden.platform.web.centre.api.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.crit.IRangeValueCritSelector;
import ua.com.fielden.platform.web.centre.api.crit.ISelectionCriteriaBuilder;
import ua.com.fielden.platform.web.centre.api.crit.defaults.IRangeDateDefaultValueAssigner;
import ua.com.fielden.platform.web.centre.api.crit.defaults.IRangeDecimalDefaultValueAssigner;
import ua.com.fielden.platform.web.centre.api.crit.defaults.IRangeIntegerDefaultValueAssigner;

/**
 * A package private helper class to decompose the task of implementing the Entity Centre DSL.
 * It has direct access to protected fields in {@link EntityCentreBuilder}.
 *
 * @author TG Team
 *
 * @param <T>
 */
class SelectionCriteriaBuilderAsRange<T extends AbstractEntity<?>> implements IRangeValueCritSelector<T> {

    private final EntityCentreBuilder<T> builder;
    private final ISelectionCriteriaBuilder<T> selectionCritBuilder;


    public SelectionCriteriaBuilderAsRange(final EntityCentreBuilder<T> builder, final ISelectionCriteriaBuilder<T> selectionCritBuilder) {
        this.builder = builder;
        this.selectionCritBuilder = selectionCritBuilder;
    }


    @Override
    public IRangeIntegerDefaultValueAssigner<T> integer() {
        return new SelectionCriteriaBuilderAsRangeInteger<T>(builder, selectionCritBuilder);
    }


    @Override
    public IRangeDecimalDefaultValueAssigner<T> decimal() {
        return new SelectionCriteriaBuilderAsRangeDecimal<T>(builder, selectionCritBuilder);
    }


    @Override
    public IRangeDateDefaultValueAssigner<T> date() {
        return new SelectionCriteriaBuilderAsRangeDate<T>(builder, selectionCritBuilder);
    }


    @Override
    public IRangeDateDefaultValueAssigner<T> dateTime() {
        return new SelectionCriteriaBuilderAsRangeDate<T>(builder, selectionCritBuilder);
    }


    @Override
    public IRangeDateDefaultValueAssigner<T> time() {
        return new SelectionCriteriaBuilderAsRangeDate<T>(builder, selectionCritBuilder);
    }


}
