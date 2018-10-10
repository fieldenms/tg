package ua.com.fielden.platform.web.centre.api.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.utils.EntityUtils;
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
    public <V extends AbstractEntity<?>> ISingleValueAutocompleterBuilder<T, V> autocompleter(final Class<V> type) {
        if (type == null) {
            throw new IllegalArgumentException("Property type is a required argument and cannot be omitted.");
        }
        // check if the specified property type is applicable to an autocompleter
        final Class<?> propType = PropertyTypeDeterminator.determinePropertyType(builder.getEntityType(), builder.currSelectionCrit.get());
        if (!EntityUtils.isEntityType(propType)) {
            throw new IllegalArgumentException(String.format("Property '%s'@'%s' cannot be used for autocompletion as it is not of an entity type (%s).", builder.currSelectionCrit.get(), builder.getEntityType().getSimpleName(), propType.getSimpleName()));
        } else if (type != propType) {
            throw new IllegalArgumentException(String.format("Property '%s'@'%s' has type %s, but type %s has been specified instead.", builder.currSelectionCrit.get(), builder.getEntityType().getSimpleName(), propType.getSimpleName(), type.getSimpleName()));
        }

        return new SelectionCriteriaBuilderAsSingleEntity<T, V>(builder, selectionCritBuilder);
    }


    @Override
    public ISingleStringDefaultValueAssigner<T> text() {
        // check if the specified property type is applicable to a text component
        final Class<?> propType = PropertyTypeDeterminator.determinePropertyType(builder.getEntityType(), builder.currSelectionCrit.get());
        if (!EntityUtils.isString(propType)) {
            throw new IllegalArgumentException(String.format("Property '%s'@'%s' cannot be used for a text component as it is not of type String (%s).", builder.currSelectionCrit.get(), builder.getEntityType().getSimpleName(), propType.getSimpleName()));
        }

        return new SelectionCriteriaBuilderAsSingleString<T>(builder, selectionCritBuilder);
    }


    @Override
    public ISingleBooleanDefaultValueAssigner<T> bool() {
        // check if the specified property type is applicable to a boolean component
        final Class<?> propType = PropertyTypeDeterminator.determinePropertyType(builder.getEntityType(), builder.currSelectionCrit.get());
        if (!EntityUtils.isBoolean(propType)) {
            throw new IllegalArgumentException(String.format("Property '%s'@'%s' cannot be used for a boolean component as it is not of type boolean (%s).", builder.currSelectionCrit.get(), builder.getEntityType().getSimpleName(), propType.getSimpleName()));
        }

        return new SelectionCriteriaBuilderAsSingleBool<T>(builder, selectionCritBuilder);
    }


    @Override
    public ISingleIntegerDefaultValueAssigner<T> integer() {
        // check if the specified property type is applicable to an integer component
        final Class<?> propType = PropertyTypeDeterminator.determinePropertyType(builder.getEntityType(), builder.currSelectionCrit.get());
        if (!EntityUtils.isInteger(propType)) {
            throw new IllegalArgumentException(String.format("Property '%s'@'%s' cannot be used for an integer component as it is not of type Integer (%s).", builder.currSelectionCrit.get(), builder.getEntityType().getSimpleName(), propType.getSimpleName()));
        }

        return new SelectionCriteriaBuilderAsSingleInteger<T>(builder, selectionCritBuilder);
    }


    @Override
    public ISingleDecimalDefaultValueAssigner<T> decimal() {
        // check if the specified property type is applicable to a decimal component
        final Class<?> propType = PropertyTypeDeterminator.determinePropertyType(builder.getEntityType(), builder.currSelectionCrit.get());
        if (!EntityUtils.isDecimal(propType)) {
            throw new IllegalArgumentException(String.format("Property '%s'@'%s' cannot be used for a decimal component as it is not of type BigDecimal or Money (%s).", builder.currSelectionCrit.get(), builder.getEntityType().getSimpleName(), propType.getSimpleName()));
        }

        return new SelectionCriteriaBuilderAsSingleDecimal<T>(builder, selectionCritBuilder);
    }


    @Override
    public ISingleDateDefaultValueAssigner<T> date() {
        // check if the specified property type is applicable to a date component
        final Class<?> propType = PropertyTypeDeterminator.determinePropertyType(builder.getEntityType(), builder.currSelectionCrit.get());
        if (!EntityUtils.isDate(propType)) {
            throw new IllegalArgumentException(String.format("Property '%s'@'%s' cannot be used for a date component as it is not of type Date (%s).", builder.currSelectionCrit.get(), builder.getEntityType().getSimpleName(), propType.getSimpleName()));
        }

        return new SelectionCriteriaBuilderAsSingleDate<T>(builder, selectionCritBuilder);
    }


    @Override
    public ISingleDateDefaultValueAssigner<T> dateTime() {
        // check if the specified property type is applicable to a date component
        final Class<?> propType = PropertyTypeDeterminator.determinePropertyType(builder.getEntityType(), builder.currSelectionCrit.get());
        if (!EntityUtils.isDate(propType)) {
            throw new IllegalArgumentException(String.format("Property '%s'@'%s' cannot be used for a date component as it is not of type Date (%s).", builder.currSelectionCrit.get(), builder.getEntityType().getSimpleName(), propType.getSimpleName()));
        }

        return new SelectionCriteriaBuilderAsSingleDate<T>(builder, selectionCritBuilder);
    }


    @Override
    public ISingleDateDefaultValueAssigner<T> time() {
        // check if the specified property type is applicable to a date component
        final Class<?> propType = PropertyTypeDeterminator.determinePropertyType(builder.getEntityType(), builder.currSelectionCrit.get());
        if (!EntityUtils.isDate(propType)) {
            throw new IllegalArgumentException(String.format("Property '%s'@'%s' cannot be used for a date component as it is not of type Date (%s).", builder.currSelectionCrit.get(), builder.getEntityType().getSimpleName(), propType.getSimpleName()));
        }

        return new SelectionCriteriaBuilderAsSingleDate<T>(builder, selectionCritBuilder);
    }


}
