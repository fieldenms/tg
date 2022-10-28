package ua.com.fielden.platform.eql.stage2.operands;

import static ua.com.fielden.platform.eql.stage2.sources.ISources2.transformNone;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage2.ITransformableToS3;
import ua.com.fielden.platform.eql.stage2.QueryBlocks2;
import ua.com.fielden.platform.eql.stage2.TransformationContext2;
import ua.com.fielden.platform.eql.stage2.TransformationResult2;
import ua.com.fielden.platform.eql.stage3.QueryBlocks3;
import ua.com.fielden.platform.eql.stage3.conditions.Conditions3;
import ua.com.fielden.platform.eql.stage3.etc.GroupBys3;
import ua.com.fielden.platform.eql.stage3.etc.OrderBys3;
import ua.com.fielden.platform.eql.stage3.etc.Yields3;
import ua.com.fielden.platform.eql.stage3.operands.ResultQuery3;
import ua.com.fielden.platform.eql.stage3.sources.ISources3;

public class ResultQuery2 extends AbstractQuery2 implements ITransformableToS3<ResultQuery3> {

    public ResultQuery2(final QueryBlocks2 queryBlocks, final Class<? extends AbstractEntity<?>> resultType) {
        super(queryBlocks, resultType);
    }

    @Override
    public TransformationResult2<ResultQuery3> transform(final TransformationContext2 context) {
        final TransformationResult2<? extends ISources3> sourcesTr = sources != null ? sources.transform(context) : transformNone(context);
        final TransformationResult2<Conditions3> conditionsTr = conditions.transform(sourcesTr.updatedContext);
        final TransformationResult2<Yields3> yieldsTr = yields.transform(conditionsTr.updatedContext);
        final TransformationResult2<GroupBys3> groupsTr = groups.transform(yieldsTr.updatedContext);
        final TransformationResult2<OrderBys3> orderingsTr = orderings.transform(groupsTr.updatedContext, yieldsTr.item);

        final QueryBlocks3 entQueryBlocks = new QueryBlocks3(sourcesTr.item, conditionsTr.item, yieldsTr.item, groupsTr.item, orderingsTr.item);

        return new TransformationResult2<>(new ResultQuery3(entQueryBlocks, resultType), orderingsTr.updatedContext);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + ResultQuery2.class.getName().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || super.equals(obj) && obj instanceof ResultQuery2;
    }
}