package ua.com.fielden.platform.web.centre.api.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.crit.IAlsoCrit;
import ua.com.fielden.platform.web.centre.api.crit.ISelectionCriteriaBuilder;
import ua.com.fielden.platform.web.centre.api.crit.defaults.ISingleBooleanDefaultValueAssigner;
import ua.com.fielden.platform.web.centre.api.crit.defaults.assigners.IValueAssigner;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.SingleCritOtherValueMnemonic;

/**
 * A package private helper class to decompose the task of implementing the Entity Centre DSL. It has direct access to protected fields in {@link EntityCentreBuilder}.
 *
 * @author TG Team
 *
 * @param <T>
 */
class SelectionCriteriaBuilderAsSingleBool<T extends AbstractEntity<?>> extends SelectionCriteriaBuilderAlsoCrit<T> implements ISingleBooleanDefaultValueAssigner<T> {

    private final EntityCentreBuilder<T> builder;

    public SelectionCriteriaBuilderAsSingleBool(final EntityCentreBuilder<T> builder, final ISelectionCriteriaBuilder<T> selectionCritBuilder) {
        super(builder, selectionCritBuilder);
        this.builder = builder;
    }

    @Override
    public IAlsoCrit<T> withDefaultValueAssigner(final Class<? extends IValueAssigner<SingleCritOtherValueMnemonic<Boolean>, T>> assigner) {
        if (!builder.currSelectionCrit.isPresent()) {
            throw new IllegalArgumentException("The current selection criterion should have been associated with some property at this stage.");
        }

        if (assigner == null) {
            throw new IllegalArgumentException("Assinger value must be provided.");
        }

        this.builder.defaultSingleValueAssignersForBooleanSelectionCriteria.put(builder.currSelectionCrit.get(), assigner);

        return this;
    }

    @Override
    public IAlsoCrit<T> setDefaultValue(final SingleCritOtherValueMnemonic<Boolean> value) {
        if (!builder.currSelectionCrit.isPresent()) {
            throw new IllegalArgumentException("The current selection criterion should have been associated with some property at this stage.");
        }

        if (value == null) {
            throw new IllegalArgumentException("Default value must be provided.");
        }

        this.builder.defaultSingleValuesForBooleanSelectionCriteria.put(builder.currSelectionCrit.get(), value);

        return this;
    }

}
