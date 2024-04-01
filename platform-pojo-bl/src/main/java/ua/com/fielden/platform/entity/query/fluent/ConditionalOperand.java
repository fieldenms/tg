package ua.com.fielden.platform.entity.query.fluent;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperand;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperator;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ILogicalOperator;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ISingleConditionOperator;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.entity.query.model.QueryModel;

abstract class ConditionalOperand<T1 extends IComparisonOperator<T2, ET>, T2 extends ILogicalOperator<?>, ET extends AbstractEntity<?>> //
        extends ExpConditionalOperand<T1, ET> //
        implements IComparisonOperand<T1, ET>, ISingleConditionOperator<T2> {

    protected ConditionalOperand(final EqlSentenceBuilder builder) {
        super(builder);
    }

    protected abstract T2 nextForConditionalOperand(final EqlSentenceBuilder builder);

    @Override
    public T2 exists(final QueryModel subQuery) {
        return nextForConditionalOperand(builder.exists(false, subQuery));
    }

    @Override
    public T2 notExists(final QueryModel subQuery) {
        return nextForConditionalOperand(builder.exists(true, subQuery));
    }

    @Override
    public T2 existsAnyOf(final Collection<? extends QueryModel<?>> subQueries) {
        return nextForConditionalOperand(builder.existsAnyOf(false, subQueries));
    }

    public T2 existsAnyOf(final QueryModel<?>... subQueries) {
        return existsAnyOf(asList(subQueries));
    }

    @Override
    public T2 notExistsAnyOf(final Collection<? extends QueryModel<?>>  subQueries) {
        return nextForConditionalOperand(builder.existsAnyOf(true, subQueries));
    }

    public T2 notExistsAnyOf(final QueryModel<?>... subQueries) {
        return notExistsAnyOf(asList(subQueries));
    }

    @Override
    public T2 existsAllOf(final Collection<? extends QueryModel<?>>  subQueries) {
        return nextForConditionalOperand(builder.existsAllOf(false, subQueries));
    }

    public T2 existsAllOf(final QueryModel<?>... subQueries) {
        return existsAllOf(asList(subQueries));
    }

    @Override
    public T2 notExistsAllOf(final Collection<? extends QueryModel<?>>  subQueries) {
        return nextForConditionalOperand(builder.existsAllOf(true, subQueries));
    }

    public T2 notExistsAllOf(final QueryModel<?>... subQueries) {
        return notExistsAllOf(asList(subQueries));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T2 critCondition(final CharSequence propName, final CharSequence critPropName) {
        return nextForConditionalOperand(builder.critCondition(propName, critPropName));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T2 critCondition(final ICompoundCondition0<?> collectionQueryStart, final CharSequence propName, final CharSequence critPropName) {
        return nextForConditionalOperand(builder.critCondition(collectionQueryStart, propName, critPropName, empty()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T2 critCondition(final ICompoundCondition0<?> collectionQueryStart, final CharSequence propName, final CharSequence critPropName, final Object defaultValue) {
        if (!(defaultValue instanceof List) && !(defaultValue instanceof String) && !(defaultValue instanceof ua.com.fielden.platform.types.tuples.T2)) {
            throw new EqlException(format("Argument [defaultValue] for property [%s] in a [critCondition] call should either be a list of strings, a string, or a tuple (T2).", propName));
        }
        return nextForConditionalOperand(builder.critCondition(collectionQueryStart, propName, critPropName, of(defaultValue)));
    }

    @Override
    public T2 condition(final ConditionModel condition) {
        return nextForConditionalOperand(builder.cond(condition));
    }

    @Override
    public T2 negatedCondition(final ConditionModel condition) {
        return nextForConditionalOperand(builder.negatedCond(condition));
    }

}
