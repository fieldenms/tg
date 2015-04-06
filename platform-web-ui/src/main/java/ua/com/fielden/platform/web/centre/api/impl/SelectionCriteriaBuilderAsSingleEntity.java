package ua.com.fielden.platform.web.centre.api.impl;

import java.util.Optional;

import ua.com.fielden.platform.basic.IValueMatcherWithCentreContext;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.centre.api.crit.IAlsoCrit;
import ua.com.fielden.platform.web.centre.api.crit.ISelectionCriteriaBuilder;
import ua.com.fielden.platform.web.centre.api.crit.ISingleValueAutocompleterBuilder;
import ua.com.fielden.platform.web.centre.api.crit.defaults.ISingleEntityDefaultValueAssigner;
import ua.com.fielden.platform.web.centre.api.crit.defaults.assigners.ISingleValueAssigner;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.SingleCritOtherValueMnemonic;

/**
 * A package private helper class to decompose the task of implementing the Entity Centre DSL. It has direct access to protected fields in {@link EntityCentreBuilder}.
 *
 * @author TG Team
 *
 * @param <T>
 */
class SelectionCriteriaBuilderAsSingleEntity<T extends AbstractEntity<?>, V extends AbstractEntity<?>> extends SelectionCriteriaBuilderAlsoCrit<T> implements ISingleValueAutocompleterBuilder<T, V> {

    private final EntityCentreBuilder<T> builder;

    public SelectionCriteriaBuilderAsSingleEntity(final EntityCentreBuilder<T> builder, final ISelectionCriteriaBuilder<T> selectionCritBuilder) {
        super(builder, selectionCritBuilder);
        this.builder = builder;
    }

    @Override
    public IAlsoCrit<T> withDefaultValueAssigner(final Class<? extends ISingleValueAssigner<SingleCritOtherValueMnemonic<? extends AbstractEntity<?>>, T>> assigner) {
        if (!builder.currSelectionCrit.isPresent()) {
            throw new IllegalArgumentException("The current selection criterion should have been associated with some property at this stage.");
        }

        this.builder.defaultSingleValueAssignersForEntitySelectionCriteria.put(builder.currSelectionCrit.get(), assigner);

        return this;
    }

    @Override
    public IAlsoCrit<T> setDefaultValue(final SingleCritOtherValueMnemonic<V> value) {
        if (!builder.currSelectionCrit.isPresent()) {
            throw new IllegalArgumentException("The current selection criterion should have been associated with some property at this stage.");
        }

        this.builder.defaultSingleValuesForEntitySelectionCriteria.put(builder.currSelectionCrit.get(), value);

        return this;
    }

    @Override
    public ISingleEntityDefaultValueAssigner<T, V> withMatcher(final Class<? extends IValueMatcherWithCentreContext<T>> matcherType) {
        if (!builder.currSelectionCrit.isPresent()) {
            throw new IllegalArgumentException("The current selection criterion should have been associated with some property at this stage.");
        }

        this.builder.valueMatchersForSelectionCriteria.put(builder.currSelectionCrit.get(), new Pair<>(matcherType, Optional.empty()));

        return this;
    }

    @Override
    public ISingleEntityDefaultValueAssigner<T, V> withMatcher(final Class<? extends IValueMatcherWithCentreContext<T>> matcherType, final CentreContextConfig context) {
        if (!builder.currSelectionCrit.isPresent()) {
            throw new IllegalArgumentException("The current selection criterion should have been associated with some property at this stage.");
        }

        this.builder.valueMatchersForSelectionCriteria.put(builder.currSelectionCrit.get(), new Pair<>(matcherType, Optional.of(context)));

        return this;
    }

}
