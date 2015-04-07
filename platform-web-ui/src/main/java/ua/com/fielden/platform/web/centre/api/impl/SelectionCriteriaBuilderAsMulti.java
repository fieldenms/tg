package ua.com.fielden.platform.web.centre.api.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.utils.EntityUtils;
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
    public <V extends AbstractEntity<?>> IMutliValueAutocompleterBuilder<T, V> autocompleter(final Class<V> type) {
        if (type == null) {
            throw new IllegalArgumentException("Property type is a required argument and cannot be omitted.");
        }
        // check if the specified property type is applicable to an autocompleter
        final Class<?> propType = PropertyTypeDeterminator.determinePropertyType(builder.getEntityType(), builder.currSelectionCrit.get());
        if (!EntityUtils.isEntityType(propType)) {
            throw new IllegalArgumentException(String.format("Property '%s'@'%s' cannot be used for autocompletion as it is not of an entity type (%s).", builder.currSelectionCrit.get(), builder.getEntityType().getSimpleName(), propType.getSimpleName()));
        } else if (type != propType) {
            throw new IllegalArgumentException(String.format("Property '%s'@'%s' has type %s, but type %s is has been specified instead.", builder.currSelectionCrit.get(), builder.getEntityType().getSimpleName(), propType.getSimpleName(), type.getSimpleName()));
        }

        return new SelectionCriteriaBuilderAsMultiString<T, V>(builder, selectionCritBuilder);
    }


    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public IMultiStringDefaultValueAssigner<T> text() {
        // check if the specified property type is applicable to a text component
        final Class<?> propType = PropertyTypeDeterminator.determinePropertyType(builder.getEntityType(), builder.currSelectionCrit.get());
        if (!EntityUtils.isString(propType)) {
            throw new IllegalArgumentException(String.format("Property '%s'@'%s' cannot be used for a text component as it is not of type String (%s).", builder.currSelectionCrit.get(), builder.getEntityType().getSimpleName(), propType.getSimpleName()));
        }

        return new SelectionCriteriaBuilderAsMultiString(builder, selectionCritBuilder);
    }


    @Override
    public IMultiBooleanDefaultValueAssigner<T> bool() {
        // check if the specified property type is applicable to a text component
        final Class<?> propType = PropertyTypeDeterminator.determinePropertyType(builder.getEntityType(), builder.currSelectionCrit.get());
        if (!EntityUtils.isBoolean(propType)) {
            throw new IllegalArgumentException(String.format("Property '%s'@'%s' cannot be used for a boolean component as it is not of type boolean (%s).", builder.currSelectionCrit.get(), builder.getEntityType().getSimpleName(), propType.getSimpleName()));
        }

        return new SelectionCriteriaBuilderAsMultiBool<T>(builder, selectionCritBuilder);
    }

}
