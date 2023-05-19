package ua.com.fielden.platform.web.centre.api.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import ua.com.fielden.platform.basic.IValueMatcherWithCentreContext;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.centre.api.crit.IAlsoCrit;
import ua.com.fielden.platform.web.centre.api.crit.ISelectionCriteriaBuilder;
import ua.com.fielden.platform.web.centre.api.crit.ISingleValueAutocompleterBuilder;
import ua.com.fielden.platform.web.centre.api.crit.ISingleValueAutocompleterBuilder0;
import ua.com.fielden.platform.web.centre.api.crit.ISingleValueAutocompleterBuilder1;
import ua.com.fielden.platform.web.centre.api.crit.defaults.ISingleEntityDefaultValueAssigner;
import ua.com.fielden.platform.web.centre.api.crit.defaults.assigners.IValueAssigner;
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
    //@SuppressWarnings("unchecked")
    public IAlsoCrit<T> withDefaultValueAssigner(final Class<? extends IValueAssigner<SingleCritOtherValueMnemonic<V>, T>> assigner) {
        if (!builder.currSelectionCrit.isPresent()) {
            throw new IllegalArgumentException("The current selection criterion should have been associated with some property at this stage.");
        }

        if (assigner == null) {
            throw new IllegalArgumentException("Assinger value must be provided.");
        }

        builder.defaultSingleValueAssignersForEntitySelectionCriteria.put(builder.currSelectionCrit.get(), assigner);

        return this;
    }

    @Override
    public IAlsoCrit<T> setDefaultValue(final SingleCritOtherValueMnemonic<V> value) {
        if (!builder.currSelectionCrit.isPresent()) {
            throw new IllegalArgumentException("The current selection criterion should have been associated with some property at this stage.");
        }

        if (value == null) {
            throw new IllegalArgumentException("Default value must be provided.");
        }

        if (value.value.isPresent()) {
            final Class<?> actualDefautValueType = value.value.get().getType();
            final Class<?> propType = PropertyTypeDeterminator.determinePropertyType(builder.getEntityType(), builder.currSelectionCrit.get());
            if (actualDefautValueType != propType) {
                throw new IllegalArgumentException(String.format("The provided default value of type %s does not match the type of property '%s'@'%s' (%s).", actualDefautValueType.getSimpleName(), builder.currSelectionCrit.get(), builder.getEntityType().getSimpleName(), propType.getSimpleName()));
            }
        }

        this.builder.defaultSingleValuesForEntitySelectionCriteria.put(builder.currSelectionCrit.get(), value);

        return this;
    }

    @Override
    public ISingleValueAutocompleterBuilder0<T, V> withMatcher(final Class<? extends IValueMatcherWithCentreContext<V>> matcherType) {
        if (!builder.currSelectionCrit.isPresent()) {
            throw new IllegalArgumentException("The current selection criterion should have been associated with some property at this stage.");
        }

        this.builder.valueMatchersForSelectionCriteria.put(builder.currSelectionCrit.get(), new Pair<>(matcherType, Optional.empty()));

        return this;
    }

    @Override
    public ISingleValueAutocompleterBuilder0<T, V> withMatcher(final Class<? extends IValueMatcherWithCentreContext<V>> matcherType, final CentreContextConfig context) {
        if (!builder.currSelectionCrit.isPresent()) {
            throw new IllegalArgumentException("The current selection criterion should have been associated with some property at this stage.");
        }

        this.builder.valueMatchersForSelectionCriteria.put(builder.currSelectionCrit.get(), new Pair<>(matcherType, Optional.of(context)));

        return this;
    }

    @Override
    public ISingleEntityDefaultValueAssigner<T, V> lightDesc() {
        return withProps(new Pair<>(AbstractEntity.DESC, true));
    }

    @SafeVarargs
    @Override
    public final ISingleEntityDefaultValueAssigner<T, V> withProps(final Pair<String, Boolean> propNameAndLightOption, final Pair<String, Boolean>... morePropNameAndLightOption) {
        final List<Pair<String, Boolean>> props = new ArrayList<>();
        props.add(propNameAndLightOption);
        props.addAll(Arrays.asList(morePropNameAndLightOption));
        this.builder.additionalPropsForAutocompleter.put(builder.currSelectionCrit.get(), props);
        return this;
    }

    @Override
    public ISingleValueAutocompleterBuilder1<T, V> withoutActiveOnlyOption() {
        this.builder.withoutActiveOnlyOptionForAutocompleter.put(builder.currSelectionCrit.get(), true);
        return this;
    }

}
