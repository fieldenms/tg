package ua.com.fielden.platform.eql.stage2.operands;

import ua.com.fielden.platform.eql.stage2.ITransformableToS3;
import ua.com.fielden.platform.eql.stage2.QueryBlocks2;
import ua.com.fielden.platform.eql.stage2.TransformationContext;
import ua.com.fielden.platform.eql.stage2.TransformationResult;
import ua.com.fielden.platform.eql.stage3.QueryBlocks3;
import ua.com.fielden.platform.eql.stage3.conditions.Conditions3;
import ua.com.fielden.platform.eql.stage3.etc.GroupBys3;
import ua.com.fielden.platform.eql.stage3.etc.OrderBys3;
import ua.com.fielden.platform.eql.stage3.etc.Yields3;
import ua.com.fielden.platform.eql.stage3.operands.TypelessSubQuery3;
import ua.com.fielden.platform.eql.stage3.sources.ISources3;

public class TypelessSubQuery2 extends AbstractQuery2 implements ITransformableToS3<TypelessSubQuery3> {

    public TypelessSubQuery2(final QueryBlocks2 queryBlocks) {
        super(queryBlocks, null);
    }

    @Override
    public TransformationResult<TypelessSubQuery3> transform(final TransformationContext context) {
        final TransformationResult<ISources3> sourcesTr = sources.transform(context);
        final TransformationResult<Conditions3> conditionsTr = conditions.transform(sourcesTr.updatedContext);
        final TransformationResult<Yields3> yieldsTr = yields.transform(conditionsTr.updatedContext);
        final TransformationResult<GroupBys3> groupsTr = groups.transform(yieldsTr.updatedContext);
        final TransformationResult<OrderBys3> orderingsTr = orderings.transform(groupsTr.updatedContext, yieldsTr.item);

        final QueryBlocks3 entQueryBlocks = new QueryBlocks3(sourcesTr.item, conditionsTr.item, yieldsTr.item, groupsTr.item, orderingsTr.item);

        return new TransformationResult<>(new TypelessSubQuery3(entQueryBlocks), orderingsTr.updatedContext);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        return prime * result + TypelessSubQuery2.class.getName().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return (this == obj) || super.equals(obj) && obj instanceof TypelessSubQuery2;
    }
}