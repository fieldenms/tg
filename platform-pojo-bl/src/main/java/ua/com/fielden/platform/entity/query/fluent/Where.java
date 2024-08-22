package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperator;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ILogicalOperator;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere;

abstract class Where<T1 extends IComparisonOperator<T2, ET>, T2 extends ILogicalOperator<? extends IWhere<T1, T2, T3, ET>>, T3, ET extends AbstractEntity<?>> //
        extends WhereWithoutNesting<T1, T2, ET> //
        implements IWhere<T1, T2, T3, ET> {

    protected Where(final EqlSentenceBuilder builder) {
        super(builder);
    }

    protected abstract T3 nextForWhere(final EqlSentenceBuilder builder);

    @Override
    public T3 begin() {
        return nextForWhere(builder.beginCondition(false));
    }

    @Override
    public T3 notBegin() {
        return nextForWhere(builder.beginCondition(true));
    }

}
