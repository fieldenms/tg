package ua.com.fielden.platform.web.centre.api.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.web.app.exceptions.WebUiBuilderException;
import ua.com.fielden.platform.web.centre.api.crit.IMultiValueAutocompleterBuilder;
import ua.com.fielden.platform.web.centre.api.crit.IMultiValueCritSelector;
import ua.com.fielden.platform.web.centre.api.crit.ISelectionCriteriaBuilder;
import ua.com.fielden.platform.web.centre.api.crit.defaults.IMultiBooleanDefaultValueAssigner;
import ua.com.fielden.platform.web.centre.api.crit.defaults.IMultiStringDefaultValueAssigner;

import static java.lang.String.format;

/// A package private helper class to decompose the task of implementing the Entity Centre DSL.
/// It has direct access to protected fields in [EntityCentreBuilder].
///
class SelectionCriteriaBuilderAsMulti<T extends AbstractEntity<?>> implements IMultiValueCritSelector<T> {

    static final String
            ERR_PROP_TYPE_IS_STRING_BUT_AUTOCOMPLETER_TYPE_IS_NOT_ENTITY_TYPE = "Type [%s] cannot be used for autocompletion of String-typed property [%s.%s]. Only entity types may be used.",
            ERR_PROP_CANNOT_BE_AUTOCOMPLETED = "Property [%s.%s] cannot be used for autocompletion as it is not of an entity type: [%s].",
            ERR_AUTOCOMPLETER_TYPE_DOES_NOT_MATCH_PROP_TYPE = "Type [%s] cannot be used for autocompletion of property [%s.%s] with type [%s].";

    private final EntityCentreBuilder<T> builder;
    private final ISelectionCriteriaBuilder<T> selectionCritBuilder;

    public SelectionCriteriaBuilderAsMulti(final EntityCentreBuilder<T> builder, final ISelectionCriteriaBuilder<T> selectionCritBuilder) {
        this.builder = builder;
        this.selectionCritBuilder = selectionCritBuilder;
    }

    @Override
    public <V extends AbstractEntity<?>> IMultiValueAutocompleterBuilder<T, V> autocompleter(final Class<V> type) {
        if (type == null) {
            throw new WebUiBuilderException("[type] is a required argument and cannot be omitted.");
        }
        // check if the specified property type is applicable to an autocompleter
        final String propPath = builder.currSelectionCrit.orElseThrow(() -> new WebUiBuilderException("Selection criteria are not defined."));
        final Class<?> propType = PropertyTypeDeterminator.determinePropertyType(builder.getEntityType(), propPath);
        if (EntityUtils.isString(propType)) {
            if (!EntityUtils.isEntityType(type)) {
                throw new WebUiBuilderException(format(ERR_PROP_TYPE_IS_STRING_BUT_AUTOCOMPLETER_TYPE_IS_NOT_ENTITY_TYPE, type.getTypeName(), builder.getEntityType().getSimpleName(), propPath));
            }
        } else if (!EntityUtils.isEntityType(propType)) {
            throw new WebUiBuilderException(format(ERR_PROP_CANNOT_BE_AUTOCOMPLETED, builder.getEntityType().getSimpleName(), propPath, propType.getTypeName()));
        } else if (!type.isAssignableFrom(propType)) {
            throw new WebUiBuilderException(format(ERR_AUTOCOMPLETER_TYPE_DOES_NOT_MATCH_PROP_TYPE, type.getName(), builder.getEntityType().getSimpleName(), propPath, propType.getTypeName()));
        }

        builder.providedTypesForAutocompletedSelectionCriteria.put(propPath, type);
        return new SelectionCriteriaBuilderAsMultiString<>(builder, selectionCritBuilder);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public IMultiStringDefaultValueAssigner<T> text() {
        // check if the specified property type is applicable to a text component
        final Class<?> propType = PropertyTypeDeterminator.determinePropertyType(builder.getEntityType(), builder.currSelectionCrit.get());
        if (!EntityUtils.isString(propType) && !EntityUtils.isRichText(propType)) {
            throw new IllegalArgumentException(String.format("Property '%s'@'%s' cannot be used for a text component as it is not of type String or RichText (%s).", builder.currSelectionCrit.get(), builder.getEntityType().getSimpleName(), propType.getSimpleName()));
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
