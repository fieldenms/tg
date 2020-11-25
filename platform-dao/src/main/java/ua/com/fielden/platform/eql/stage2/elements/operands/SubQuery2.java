package ua.com.fielden.platform.eql.stage2.elements.operands;

import java.util.Objects;

import org.hibernate.type.LongType;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage2.elements.EntQueryBlocks2;
import ua.com.fielden.platform.eql.stage2.elements.TransformationContext;
import ua.com.fielden.platform.eql.stage2.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage3.elements.EntQueryBlocks3;
import ua.com.fielden.platform.eql.stage3.elements.conditions.Conditions3;
import ua.com.fielden.platform.eql.stage3.elements.core.GroupBys3;
import ua.com.fielden.platform.eql.stage3.elements.core.OrderBys3;
import ua.com.fielden.platform.eql.stage3.elements.core.Yields3;
import ua.com.fielden.platform.eql.stage3.elements.operands.SubQuery3;
import ua.com.fielden.platform.eql.stage3.elements.sources.IQrySources3;

public class SubQuery2 extends AbstractQuery2 implements ISingleOperand2<SubQuery3> {

    public final Object hibType;

    public SubQuery2(final EntQueryBlocks2 queryBlocks, final Class<? extends AbstractEntity<?>> resultType) {
        super(queryBlocks, resultType);
        this.hibType = resultType == null ? yields.getYields().iterator().next().operand.hibType() : LongType.INSTANCE;
    }

    @Override
    public TransformationResult<SubQuery3> transform(final TransformationContext context) {
        final TransformationResult<IQrySources3> sourcesTr = sources.transform(context);
        final TransformationResult<Conditions3> conditionsTr = conditions.transform(sourcesTr.updatedContext);
        final TransformationResult<Yields3> yieldsTr = yields.transform(conditionsTr.updatedContext);
        final TransformationResult<GroupBys3> groupsTr = groups.transform(yieldsTr.updatedContext);
        final TransformationResult<OrderBys3> orderingsTr = orderings.transform(groupsTr.updatedContext, yieldsTr.item);

        final EntQueryBlocks3 entQueryBlocks = new EntQueryBlocks3(sourcesTr.item, conditionsTr.item, yieldsTr.item, groupsTr.item, orderingsTr.item);

        return new TransformationResult<SubQuery3>(new SubQuery3(entQueryBlocks, resultType), orderingsTr.updatedContext);
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