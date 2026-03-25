package ua.com.fielden.platform.eql.stage2.operands.functions;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.stage2.ITransformableFromStage2To3;
import ua.com.fielden.platform.eql.stage2.TransformationContextFromStage2To3;
import ua.com.fielden.platform.eql.stage2.TransformationResultFromStage2To3;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.eql.stage3.operands.functions.ConcatOfOrderItem3;

import java.util.Set;

public record ConcatOfOrderItem2(
        ISingleOperand2<? extends ISingleOperand3> operand,
        boolean isDesc)
        implements ITransformableFromStage2To3<ConcatOfOrderItem3>
{
    public TransformationResultFromStage2To3<ConcatOfOrderItem3> transform(final TransformationContextFromStage2To3 context) {
        final var operandResult = operand.transform(context);
        return new TransformationResultFromStage2To3<>(
                new ConcatOfOrderItem3(operandResult.item, isDesc),
                operandResult.updatedContext);
    }

    @Override
    public Set<Prop2> collectProps() {
        return operand.collectProps();
    }

    @Override
    public Set<Class<? extends AbstractEntity<?>>> collectEntityTypes() {
        return operand.collectEntityTypes();
    }

}
