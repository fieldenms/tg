package ua.com.fielden.platform.web.centre.api.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.utils.EntityUtils;
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
        // check if the specified property type is applicable to an integer component
        final Class<?> propType = PropertyTypeDeterminator.determinePropertyType(builder.getEntityType(), builder.currSelectionCrit.get());
        if (!EntityUtils.isInteger(propType)) {
            throw new IllegalArgumentException(String.format("Property '%s'@'%s' cannot be used for an integer component as it is not of type Integer (%s).", builder.currSelectionCrit.get(), builder.getEntityType().getSimpleName(), propType.getSimpleName()));
        }

        return new SelectionCriteriaBuilderAsRangeInteger<T>(builder, selectionCritBuilder);
    }


    @Override
    public IRangeDecimalDefaultValueAssigner<T> decimal() {
        // check if the specified property type is applicable to a decimal component
        final Class<?> propType = PropertyTypeDeterminator.determinePropertyType(builder.getEntityType(), builder.currSelectionCrit.get());
        if (!EntityUtils.isDecimal(propType)) {
            throw new IllegalArgumentException(String.format("Property '%s'@'%s' cannot be used for a decimal component as it is not of type BigDecimal or Money (%s).", builder.currSelectionCrit.get(), builder.getEntityType().getSimpleName(), propType.getSimpleName()));
        }

        return new SelectionCriteriaBuilderAsRangeDecimal<T>(builder, selectionCritBuilder);
    }


    @Override
    public IRangeDateDefaultValueAssigner<T> date() {
        // check if the specified property type is applicable to a date component
        final Class<?> propType = PropertyTypeDeterminator.determinePropertyType(builder.getEntityType(), builder.currSelectionCrit.get());
        if (!EntityUtils.isDate(propType)) {
            throw new IllegalArgumentException(String.format("Property '%s'@'%s' cannot be used for a date component as it is not of type Date (%s).", builder.currSelectionCrit.get(), builder.getEntityType().getSimpleName(), propType.getSimpleName()));
        }

        return new SelectionCriteriaBuilderAsRangeDate<T>(builder, selectionCritBuilder);
    }


    @Override
    public IRangeDateDefaultValueAssigner<T> dateTime() {
        // check if the specified property type is applicable to a date component
        final Class<?> propType = PropertyTypeDeterminator.determinePropertyType(builder.getEntityType(), builder.currSelectionCrit.get());
        if (!EntityUtils.isDate(propType)) {
            throw new IllegalArgumentException(String.format("Property '%s'@'%s' cannot be used for a date component as it is not of type Date (%s).", builder.currSelectionCrit.get(), builder.getEntityType().getSimpleName(), propType.getSimpleName()));
        }

        return new SelectionCriteriaBuilderAsRangeDate<T>(builder, selectionCritBuilder);
    }


    @Override
    public IRangeDateDefaultValueAssigner<T> time() {
        // check if the specified property type is applicable to a date component
        final Class<?> propType = PropertyTypeDeterminator.determinePropertyType(builder.getEntityType(), builder.currSelectionCrit.get());
        if (!EntityUtils.isDate(propType)) {
            throw new IllegalArgumentException(String.format("Property '%s'@'%s' cannot be used for a date component as it is not of type Date (%s).", builder.currSelectionCrit.get(), builder.getEntityType().getSimpleName(), propType.getSimpleName()));
        }

        return new SelectionCriteriaBuilderAsRangeDate<T>(builder, selectionCritBuilder);
    }


}
