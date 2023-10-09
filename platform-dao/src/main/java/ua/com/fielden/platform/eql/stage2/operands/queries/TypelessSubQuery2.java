package ua.com.fielden.platform.eql.stage2.operands.queries;

import ua.com.fielden.platform.eql.stage2.ITransformableToStage3;
import ua.com.fielden.platform.eql.stage2.QueryComponents2;
import ua.com.fielden.platform.eql.stage2.TransformationContext2;
import ua.com.fielden.platform.eql.stage2.TransformationResult2;
import ua.com.fielden.platform.eql.stage3.QueryComponents3;
import ua.com.fielden.platform.eql.stage3.conditions.Conditions3;
import ua.com.fielden.platform.eql.stage3.etc.GroupBys3;
import ua.com.fielden.platform.eql.stage3.etc.OrderBys3;
import ua.com.fielden.platform.eql.stage3.etc.Yields3;
import ua.com.fielden.platform.eql.stage3.operands.queries.TypelessSubQuery3;
import ua.com.fielden.platform.eql.stage3.sources.IJoinNode3;

public class TypelessSubQuery2 extends AbstractQuery2 implements ITransformableToStage3<TypelessSubQuery3> {

    public TypelessSubQuery2(final QueryComponents2 queryComponents) {
        super(queryComponents, null);
    }

    @Override
    public TransformationResult2<TypelessSubQuery3> transform(final TransformationContext2 context) {
        final TransformationResult2<? extends IJoinNode3> joinRootTr = joinRoot != null ? joinRoot.transform(context) : transformNone(context);
        final TransformationResult2<Conditions3> conditionsTr = conditions.transform(joinRootTr.updatedContext);
        final TransformationResult2<Yields3> yieldsTr = yields.transform(conditionsTr.updatedContext);
        final TransformationResult2<GroupBys3> groupsTr = groups.transform(yieldsTr.updatedContext);
        final TransformationResult2<OrderBys3> orderingsTr = orderings.transform(groupsTr.updatedContext, yieldsTr.item);

        final QueryComponents3 queryComponents3 = new QueryComponents3(joinRootTr.item, conditionsTr.item, yieldsTr.item, groupsTr.item, orderingsTr.item);

        return new TransformationResult2<>(new TypelessSubQuery3(queryComponents3), orderingsTr.updatedContext);
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