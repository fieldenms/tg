package ua.com.fielden.platform.web.centre.api.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.crit.ISelectionCriteriaBuilder;
import ua.com.fielden.platform.web.centre.api.crit.ISingleValueAutocompleterBuilder;
import ua.com.fielden.platform.web.centre.api.crit.ISingleValueCritSelector;
import ua.com.fielden.platform.web.centre.api.crit.defaults.ISingleBooleanDefaultValueAssigner;
import ua.com.fielden.platform.web.centre.api.crit.defaults.ISingleDateDefaultValueAssigner;
import ua.com.fielden.platform.web.centre.api.crit.defaults.ISingleDecimalDefaultValueAssigner;
import ua.com.fielden.platform.web.centre.api.crit.defaults.ISingleIntegerDefaultValueAssigner;
import ua.com.fielden.platform.web.centre.api.crit.defaults.ISingleStringDefaultValueAssigner;

/**
 * A package private helper class to decompose the task of implementing the Entity Centre DSL.
 * It has direct access to protected fields in {@link EntityCentreBuilder}.
 *
 * @author TG Team
 *
 * @param <T>
 */
class SelectionCriteriaBuilderAsSingle<T extends AbstractEntity<?>> implements ISingleValueCritSelector<T> {

    private final EntityCentreBuilder<T> builder;
    private final ISelectionCriteriaBuilder<T> selectionCritBuilder;


    public SelectionCriteriaBuilderAsSingle(final EntityCentreBuilder<T> builder, final ISelectionCriteriaBuilder<T> selectionCritBuilder) {
        this.builder = builder;
        this.selectionCritBuilder = selectionCritBuilder;
    }


    @Override
    public <V extends AbstractEntity<?>> ISingleValueAutocompleterBuilder<T, V> autocompleter(final Class<V> propertyType) {
        return new SelectionCriteriaBuilderAsSingleEntity<T, V>(builder, selectionCritBuilder);
    }


    @Override
    public ISingleStringDefaultValueAssigner<T> text() {
        return new SelectionCriteriaBuilderAsSingleString<T>(builder, selectionCritBuilder);
    }


    @Override
    public ISingleBooleanDefaultValueAssigner<T> bool() {
        return new SelectionCriteriaBuilderAsSingleBool<T>(builder, selectionCritBuilder);
    }


    @Override
    public ISingleIntegerDefaultValueAssigner<T> integer() {
        return new SelectionCriteriaBuilderAsSingleInteger<T>(builder, selectionCritBuilder);
    }


    @Override
    public ISingleDecimalDefaultValueAssigner<T> decimal() {
        return new SelectionCriteriaBuilderAsSingleDecimal<T>(builder, selectionCritBuilder);
    }


    @Override
    public ISingleDateDefaultValueAssigner<T> date() {
        return new SelectionCriteriaBuilderAsSingleDate<T>(builder, selectionCritBuilder);
    }


    @Override
    public ISingleDateDefaultValueAssigner<T> dateTime() {
        return new SelectionCriteriaBuilderAsSingleDate<T>(builder, selectionCritBuilder);
    }


    @Override
    public ISingleDateDefaultValueAssigner<T> time() {
        return new SelectionCriteriaBuilderAsSingleDate<T>(builder, selectionCritBuilder);
    }


}
