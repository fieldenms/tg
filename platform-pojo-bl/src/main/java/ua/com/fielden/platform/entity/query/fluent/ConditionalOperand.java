package ua.com.fielden.platform.entity.query.fluent;

import static java.lang.String.format;

import java.util.List;
import java.util.Optional;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperand;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperator;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ILogicalOperator;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ISingleConditionOperator;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;

abstract class ConditionalOperand<T1 extends IComparisonOperator<T2, ET>, T2 extends ILogicalOperator<?>, ET extends AbstractEntity<?>> //
        extends ExpConditionalOperand<T1, ET> //
        implements IComparisonOperand<T1, ET>, ISingleConditionOperator<T2> {

    protected ConditionalOperand(final Tokens tokens) {
        super(tokens);
    }

    protected abstract T2 nextForConditionalOperand(final Tokens tokens);

    @Override
    public T2 exists(final QueryModel subQuery) {
        return nextForConditionalOperand(getTokens().exists(false, subQuery));
    }

    @Override
    public T2 notExists(final QueryModel subQuery) {
        return nextForConditionalOperand(getTokens().exists(true, subQuery));
    }

    @Override
    public T2 existsAnyOf(final QueryModel... subQueries) {
        return nextForConditionalOperand(getTokens().existsAnyOf(false, subQueries));
    }

    @Override
    public T2 notExistsAnyOf(final QueryModel... subQueries) {
        return nextForConditionalOperand(getTokens().existsAnyOf(true, subQueries));
    }

    @Override
    public T2 existsAllOf(final QueryModel... subQueries) {
        return nextForConditionalOperand(getTokens().existsAllOf(false, subQueries));
    }

    @Override
    public T2 notExistsAllOf(final QueryModel... subQueries) {
        return nextForConditionalOperand(getTokens().existsAllOf(true, subQueries));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T2 critCondition(final String propName, final String critPropName) {
        return nextForConditionalOperand(getTokens().critCondition(propName, critPropName));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T2 critCondition(final IConvertableToPath prop, final IConvertableToPath critProp) {
        return critCondition(prop.toPath(), critProp.toPath());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T2 critCondition(final ICompoundCondition0<?> collectionQueryStart, final String propName, final String critPropName) {
        return nextForConditionalOperand(getTokens().critCondition(collectionQueryStart, propName, critPropName, Optional.empty()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T2 critCondition(final ICompoundCondition0<?> collectionQueryStart, final String propName, final String critPropName, final Object defaultValue) {
        if (!(defaultValue instanceof List) && !(defaultValue instanceof String) && !(defaultValue instanceof ua.com.fielden.platform.types.tuples.T2)) {
            throw new EqlException(format("Argument [defaultValue] for property [%s] in a [critCondition] call should either be a list of strings, a string, or a tuple (T2).", propName));
        }
        return nextForConditionalOperand(getTokens().critCondition(collectionQueryStart, propName, critPropName, Optional.of(defaultValue)));
    }

    @Override
    public T2 condition(final ConditionModel condition) {
        return nextForConditionalOperand(getTokens().cond(condition));
    }

    @Override
    public T2 negatedCondition(final ConditionModel condition) {
        return nextForConditionalOperand(getTokens().negatedCond(condition));
    }
}