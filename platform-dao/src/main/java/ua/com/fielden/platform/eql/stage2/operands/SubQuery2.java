package ua.com.fielden.platform.eql.stage2.operands;

import static ua.com.fielden.platform.eql.stage2.sources.ISources2.transformNone;

import java.util.Objects;

import ua.com.fielden.platform.eql.stage2.QueryBlocks2;
import ua.com.fielden.platform.eql.stage2.TransformationContext2;
import ua.com.fielden.platform.eql.stage2.TransformationResult2;
import ua.com.fielden.platform.eql.stage3.QueryBlocks3;
import ua.com.fielden.platform.eql.stage3.conditions.Conditions3;
import ua.com.fielden.platform.eql.stage3.etc.GroupBys3;
import ua.com.fielden.platform.eql.stage3.etc.OrderBys3;
import ua.com.fielden.platform.eql.stage3.etc.Yields3;
import ua.com.fielden.platform.eql.stage3.operands.SubQuery3;
import ua.com.fielden.platform.eql.stage3.sources.ISources3;

public class SubQuery2 extends AbstractQuery2 implements ISingleOperand2<SubQuery3> {

    public final Object hibType;

    public SubQuery2(final QueryBlocks2 queryBlocks, final Class<?> resultType, final Object hibType) {
        super(queryBlocks, resultType);
        this.hibType = hibType;
    }

    @Override
    public TransformationResult2<SubQuery3> transform(final TransformationContext2 context) {
        final TransformationResult2<? extends ISources3> sourcesTr = sources != null ? sources.transform(context) : transformNone(context);
        final TransformationResult2<Conditions3> conditionsTr = conditions.transform(sourcesTr.updatedContext);
        final TransformationResult2<Yields3> yieldsTr = yields.transform(conditionsTr.updatedContext);
        final TransformationResult2<GroupBys3> groupsTr = groups.transform(yieldsTr.updatedContext);
        final TransformationResult2<OrderBys3> orderingsTr = orderings.transform(groupsTr.updatedContext, yieldsTr.item);

        final QueryBlocks3 entQueryBlocks = new QueryBlocks3(sourcesTr.item, conditionsTr.item, yieldsTr.item, groupsTr.item, orderingsTr.item);

        return new TransformationResult2<>(new SubQuery3(entQueryBlocks, resultType, hibType), orderingsTr.updatedContext);
    }

    @Override
    public Class<?> type() {
        return resultType;
    }

    @Override
    public Object hibType() {
        return hibType;
    }

    @Override
    public boolean ignore() {
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + hibType.hashCode();
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

        return Objects.equals(hibType, other.hibType);
    }
}