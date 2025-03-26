package ua.com.fielden.platform.eql.stage2.queries;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage2.ITransformableFromStage2To3;
import ua.com.fielden.platform.eql.stage2.QueryComponents2;
import ua.com.fielden.platform.eql.stage2.TransformationContextFromStage2To3;
import ua.com.fielden.platform.eql.stage2.TransformationResultFromStage2To3;
import ua.com.fielden.platform.eql.stage2.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage2.sources.IJoinNode2;
import ua.com.fielden.platform.eql.stage2.sundries.GroupBys2;
import ua.com.fielden.platform.eql.stage2.sundries.OrderBys2;
import ua.com.fielden.platform.eql.stage2.sundries.Yields2;
import ua.com.fielden.platform.eql.stage3.QueryComponents3;
import ua.com.fielden.platform.eql.stage3.queries.SourceQuery3;
import ua.com.fielden.platform.eql.stage3.sources.IJoinNode3;

import java.util.Optional;

public class SourceQuery2 extends AbstractQuery2 implements ITransformableFromStage2To3<SourceQuery3> {

    public SourceQuery2(final QueryComponents2 queryComponents, final Class<? extends AbstractEntity<?>> resultType) {
        super(queryComponents, resultType);
    }

    public SourceQuery2(
            final Optional<IJoinNode2<? extends IJoinNode3>> maybeJoinRoot,
            final Conditions2 whereConditions,
            final Yields2 yields,
            final GroupBys2 groups,
            final OrderBys2 orderings,
            final Class<?> resultType)
    {
        super(maybeJoinRoot, whereConditions, yields, groups, orderings, resultType);
    }

    @Override
    public TransformationResultFromStage2To3<SourceQuery3> transform(final TransformationContextFromStage2To3 context) {
        final TransformationResultFromStage2To3<QueryComponents3> queryComponentsTr = transformQueryComponents(context);
        return new TransformationResultFromStage2To3<>(new SourceQuery3(queryComponentsTr.item, resultType), queryComponentsTr.updatedContext);
    }

    @Override
    public AbstractQuery2 setOrderings(final OrderBys2 orderings) {
        return new SourceQuery2(maybeJoinRoot, whereConditions, yields, groups, orderings, resultType);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + SourceQuery2.class.getName().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || super.equals(obj) && obj instanceof SourceQuery2;
    }
}
