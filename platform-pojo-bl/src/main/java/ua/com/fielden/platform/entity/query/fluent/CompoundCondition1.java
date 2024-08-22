package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere1;

final class CompoundCondition1<ET extends AbstractEntity<?>> extends //
        CompoundCondition<IWhere1<ET>, ICompoundCondition0<ET>> //
        implements ICompoundCondition1<ET> {

    public CompoundCondition1(final EqlSentenceBuilder builder) {
        super(builder);
    }

    @Override
    protected IWhere1<ET> nextForLogicalCondition(final EqlSentenceBuilder builder) {
        return new Where1<ET>(builder);
    }

    @Override
    protected ICompoundCondition0<ET> nextForCompoundCondition(final EqlSentenceBuilder builder) {
        return new CompoundCondition0<ET>(builder);
    }

}
