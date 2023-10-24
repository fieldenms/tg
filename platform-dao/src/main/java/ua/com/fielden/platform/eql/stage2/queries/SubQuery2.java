package ua.com.fielden.platform.eql.stage2.queries;

import java.util.Objects;

import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage2.QueryComponents2;
import ua.com.fielden.platform.eql.stage2.TransformationContext2;
import ua.com.fielden.platform.eql.stage2.TransformationResult2;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.QueryComponents3;
import ua.com.fielden.platform.eql.stage3.conditions.Conditions3;
import ua.com.fielden.platform.eql.stage3.etc.GroupBys3;
import ua.com.fielden.platform.eql.stage3.etc.OrderBys3;
import ua.com.fielden.platform.eql.stage3.etc.Yields3;
import ua.com.fielden.platform.eql.stage3.queries.SubQuery3;
import ua.com.fielden.platform.eql.stage3.sources.IJoinNode3;

public class SubQuery2 extends AbstractQuery2 implements ISingleOperand2<SubQuery3> {
    public final boolean isRefetchOnly;
    public final PropType type;
    
    public SubQuery2(final QueryComponents2 queryComponents, final PropType type, final boolean isRefetchOnly) {
        super(queryComponents, type.javaType());
        this.isRefetchOnly = isRefetchOnly;
        this.type = type;
    }

    @Override
    public TransformationResult2<SubQuery3> transform(final TransformationContext2 context) {
        final TransformationResult2<? extends IJoinNode3> joinRootTr = joinRoot != null ? joinRoot.transform(context) : transformNone(context);
        final TransformationResult2<Conditions3> whereConditionsTr = whereConditions.transform(joinRootTr.updatedContext);
        final TransformationResult2<Yields3> yieldsTr = yields.transform(whereConditionsTr.updatedContext);
        final TransformationResult2<GroupBys3> groupsTr = groups.transform(yieldsTr.updatedContext);
        final TransformationResult2<OrderBys3> orderingsTr = orderings.transform(groupsTr.updatedContext, yieldsTr.item);

        final QueryComponents3 queryComponents3 = new QueryComponents3(joinRootTr.item, whereConditionsTr.item, yieldsTr.item, groupsTr.item, orderingsTr.item);

        return new TransformationResult2<>(new SubQuery3(queryComponents3, type), orderingsTr.updatedContext);
    }

    @Override
    public PropType type() {
        return type;
    }

    @Override
    public boolean ignore() {
        return false;
    }
    
    @Override
    public boolean isNonnullableEntity() {
        return isRefetchOnly;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + type.hashCode();
        result = prime * result + (isRefetchOnly ? 1231 : 1237);
        return prime * result + SubQuery2.class.getName().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (!super.equals(obj)) {
            return false;
        }
        
        if (!(obj instanceof SubQuery2)) {
            return false;
        }
        
        final SubQuery2 other = (SubQuery2) obj;
        
        return Objects.equals(type, other.type) && (isRefetchOnly == other.isRefetchOnly);
    }
}