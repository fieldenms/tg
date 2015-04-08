package ua.com.fielden.platform.web.centre.api.impl;

import java.util.Optional;

import ua.com.fielden.platform.basic.IValueMatcherWithCentreContext;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.centre.api.crit.IAlsoCrit;
import ua.com.fielden.platform.web.centre.api.crit.IMutliValueAutocompleterBuilder;
import ua.com.fielden.platform.web.centre.api.crit.ISelectionCriteriaBuilder;
import ua.com.fielden.platform.web.centre.api.crit.defaults.IMultiStringDefaultValueAssigner;
import ua.com.fielden.platform.web.centre.api.crit.defaults.assigners.IMultiValueAssigner;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.MultiCritStringValueMnemonic;

/**
 * A package private helper class to decompose the task of implementing the Entity Centre DSL. It has direct access to protected fields in {@link EntityCentreBuilder}.
 *
 * @author TG Team
 *
 * @param <T>
 */
class SelectionCriteriaBuilderAsMultiString<T extends AbstractEntity<?>, V extends AbstractEntity<?>> extends SelectionCriteriaBuilderAlsoCrit<T> implements IMutliValueAutocompleterBuilder<T, V>, IMultiStringDefaultValueAssigner<T> {

    private final EntityCentreBuilder<T> builder;

    public SelectionCriteriaBuilderAsMultiString(final EntityCentreBuilder<T> builder, final ISelectionCriteriaBuilder<T> selectionCritBuilder) {
        super(builder, selectionCritBuilder);
        this.builder = builder;
    }

    @Override
    public IAlsoCrit<T> withDefaultValueAssigner(final Class<? extends IMultiValueAssigner<MultiCritStringValueMnemonic, T>> assigner) {
        if (!builder.currSelectionCrit.isPresent()) {
            throw new IllegalArgumentException("The current selection criterion should have been associated with some property at this stage.");
        }

        if (assigner == null) {
            throw new IllegalArgumentException("Assinger value must be provided.");
        }

        this.builder.defaultMultiValueAssignersForEntityAndStringSelectionCriteria.put(builder.currSelectionCrit.get(), assigner);

        return this;
    }

    @Override
    public IAlsoCrit<T> setDefaultValue(final MultiCritStringValueMnemonic value) {
        if (!builder.currSelectionCrit.isPresent()) {
            throw new IllegalArgumentException("The current selection criterion should have been associated with some property at this stage.");
        }

        if (value == null) {
            throw new IllegalArgumentException("Value must be provided.");
        }

        this.builder.defaultMultiValuesForEntityAndStringSelectionCriteria.put(builder.currSelectionCrit.get(), value);

        return this;
    }

    @Override
    public IMultiStringDefaultValueAssigner<T> withMatcher(final Class<? extends IValueMatcherWithCentreContext<V>> matcherType) {
        if (!builder.currSelectionCrit.isPresent()) {
            throw new IllegalArgumentException("The current selection criterion should have been associated with some property at this stage.");
        }

        if (matcherType == null) {
            throw new IllegalArgumentException("Matcher must be provided.");
        }

        this.builder.valueMatchersForSelectionCriteria.put(builder.currSelectionCrit.get(), new Pair<>(matcherType, Optional.empty()));

        return this;
    }

    @Override
    public IMultiStringDefaultValueAssigner<T> withMatcher(final Class<? extends IValueMatcherWithCentreContext<V>> matcherType, final CentreContextConfig context) {
        if (!builder.currSelectionCrit.isPresent()) {
            throw new IllegalArgumentException("The current selection criterion should have been associated with some property at this stage.");
        }

        if (matcherType == null) {
            throw new IllegalArgumentException("Matcher must be provided.");
        }

        this.builder.valueMatchersForSelectionCriteria.put(builder.currSelectionCrit.get(), new Pair<>(matcherType, Optional.of(context)));

        return this;
    }

}
