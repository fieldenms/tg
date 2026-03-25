package ua.com.fielden.platform.eql.stage1.operands.functions;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage1.ITransformableFromStage1To2;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.operands.functions.ConcatOfOrderItem2;

import java.util.Set;

public record ConcatOfOrderItem1(
        ISingleOperand1<? extends ISingleOperand2<?>> operand,
        boolean isDesc)
        implements ITransformableFromStage1To2<ConcatOfOrderItem2>
{

    public ConcatOfOrderItem2 transform(final TransformationContextFromStage1To2 context) {
        return new ConcatOfOrderItem2(operand.transform(context), isDesc);
    }

    @Override
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return operand.collectEntityTypes();
    }

}
