package ua.com.fielden.platform.web.centre.api.impl;

import java.math.BigDecimal;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.crit.IAlsoCrit;
import ua.com.fielden.platform.web.centre.api.crit.ISelectionCriteriaBuilder;
import ua.com.fielden.platform.web.centre.api.crit.defaults.IRangeDecimalDefaultValueAssigner;
import ua.com.fielden.platform.web.centre.api.crit.defaults.assigners.IRangeValueAssigner;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.RangeCritOtherValueMnemonic;

/**
 * A package private helper class to decompose the task of implementing the Entity Centre DSL. It has direct access to protected fields in {@link EntityCentreBuilder}.
 *
 * @author TG Team
 *
 * @param <T>
 */
class SelectionCriteriaBuilderAsRangeDecimal<T extends AbstractEntity<?>> extends SelectionCriteriaBuilderAlsoCrit<T> implements IRangeDecimalDefaultValueAssigner<T> {

    private final EntityCentreBuilder<T> builder;

    public SelectionCriteriaBuilderAsRangeDecimal(final EntityCentreBuilder<T> builder, final ISelectionCriteriaBuilder<T> selectionCritBuilder) {
        super(builder, selectionCritBuilder);
        this.builder = builder;
    }

    @Override
    public IAlsoCrit<T> withDefaultValueAssigner(final Class<? extends IRangeValueAssigner<RangeCritOtherValueMnemonic<BigDecimal>, T>> assigner) {
        if (!builder.currSelectionCrit.isPresent()) {
            throw new IllegalArgumentException("The current selection criterion should have been associated with some property at this stage.");
        }

        if (assigner == null) {
            throw new IllegalArgumentException("Assinger value must be provided.");
        }

        this.builder.defaultRangeValueAssignersForBigDecimalAndMoneySelectionCriteria.put(builder.currSelectionCrit.get(), assigner);

        return this;
    }

    @Override
    public IAlsoCrit<T> setDefaultValue(final RangeCritOtherValueMnemonic<BigDecimal> value) {
        if (!builder.currSelectionCrit.isPresent()) {
            throw new IllegalArgumentException("The current selection criterion should have been associated with some property at this stage.");
        }

        if (value == null) {
            throw new IllegalArgumentException("Value value must be provided.");
        }

        this.builder.defaultRangeValuesForBigDecimalAndMoneySelectionCriteria.put(builder.currSelectionCrit.get(), value);

        return this;
    }

}
