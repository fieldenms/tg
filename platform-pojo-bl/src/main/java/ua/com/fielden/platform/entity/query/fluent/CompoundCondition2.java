package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere2;

final class CompoundCondition2<ET extends AbstractEntity<?>> //
        extends CompoundCondition<IWhere2<ET>, ICompoundCondition1<ET>> //
        implements ICompoundCondition2<ET> {

    public CompoundCondition2(final EqlSentenceBuilder builder) {
        super(builder);
    }

    @Override
    protected IWhere2<ET> nextForLogicalCondition(final EqlSentenceBuilder builder) {
        return new Where2<ET>(builder);
    }

    @Override
    protected ICompoundCondition1<ET> nextForCompoundCondition(final EqlSentenceBuilder builder) {
        return new CompoundCondition1<ET>(builder);
    }

}
