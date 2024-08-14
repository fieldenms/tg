package ua.com.fielden.platform.web.centre.api.impl;

import static java.lang.String.format;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.web.app.exceptions.WebUiBuilderException;
import ua.com.fielden.platform.web.centre.api.crit.IMultiValueCritSelector;
import ua.com.fielden.platform.web.centre.api.crit.IMultiValueAutocompleterBuilder;
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
    public <V extends AbstractEntity<?>> IMultiValueAutocompleterBuilder<T, V> autocompleter(final Class<V> type) {
        if (type == null) {
            throw new WebUiBuilderException("Property type is a required argument and cannot be omitted.");
        }
        // check if the specified property type is applicable to an autocompleter
        final String propPath = builder.currSelectionCrit.orElseThrow(() -> new WebUiBuilderException("Selection criteria is not defined."));
        final Class<?> propType = PropertyTypeDeterminator.determinePropertyType(builder.getEntityType(), propPath);
        if (!EntityUtils.isEntityType(propType)) {
            throw new WebUiBuilderException(format("Property '%s'@'%s' cannot be used for autocompletion as it is not of an entity type (%s).", propPath, builder.getEntityType().getSimpleName(), propType.getSimpleName()));
        } else if (!type.isAssignableFrom(propType)) {
            throw new WebUiBuilderException(format("Property '%s'@'%s' has type %s, but type %s has been specified instead.", propPath, builder.getEntityType().getSimpleName(), propType.getSimpleName(), type.getSimpleName()));
        }

        builder.providedTypesForAutocompletedSelectionCriteria.put(propPath, type);
        return new SelectionCriteriaBuilderAsMultiString<>(builder, selectionCritBuilder);
    }


    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public IMultiStringDefaultValueAssigner<T> text() {
        // check if the specified property type is applicable to a text component
        final Class<?> propType = PropertyTypeDeterminator.determinePropertyType(builder.getEntityType(), builder.currSelectionCrit.get());
        if (!EntityUtils.isString(propType)/* || !EntityUtils.isRichText(propType)*/) {
            throw new IllegalArgumentException(String.format("Property '%s'@'%s' cannot be used for a text component as it is not of type String (%s).", builder.currSelectionCrit.get(), builder.getEntityType().getSimpleName(), propType.getSimpleName()));
        }

        return new SelectionCriteriaBuilderAsMultiString(builder, selectionCritBuilder);
    }


    @Override
    public IMultiBooleanDefaultValueAssigner<T> bool() {
        // check if the specified property type is applicable to a boolean component
        final Class<?> propType = PropertyTypeDeterminator.determinePropertyType(builder.getEntityType(), builder.currSelectionCrit.get());
        if (!EntityUtils.isBoolean(propType)) {
            throw new IllegalArgumentException(String.format("Property '%s'@'%s' cannot be used for a boolean component as it is not of type boolean (%s).", builder.currSelectionCrit.get(), builder.getEntityType().getSimpleName(), propType.getSimpleName()));
        }

        return new SelectionCriteriaBuilderAsMultiBool<>(builder, selectionCritBuilder);
    }

}
