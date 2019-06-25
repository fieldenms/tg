package ua.com.fielden.platform.eql.stage1.elements.conditions;

import java.util.Objects;

import ua.com.fielden.platform.eql.stage1.elements.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage1.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage1.elements.operands.ISetOperand1;
import ua.com.fielden.platform.eql.stage1.elements.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.elements.conditions.SetTest2;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISetOperand2;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;

public class SetTest1 implements ICondition1<SetTest2> {
    private final ISingleOperand1<? extends ISingleOperand2> leftOperand;
    private final ISetOperand1<? extends ISetOperand2> rightOperand;
    private final boolean negated;

    public SetTest1(final ISingleOperand1<? extends ISingleOperand2> leftOperand, final boolean negated, final ISetOperand1<? extends ISetOperand2> rightOperand) {
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
        this.negated = negated;
    }

    @Override
    public TransformationResult<SetTest2> transform(final PropsResolutionContext resolutionContext) {
        final TransformationResult<? extends ISingleOperand2> leftOperandTransformationResult = leftOperand.transform(resolutionContext);
        final TransformationResult<? extends ISetOperand2> rightOperandTransformationResult = rightOperand.transform(leftOperandTransformationResult.getUpdatedContext());
        return new TransformationResult<SetTest2>(new SetTest2(leftOperandTransformationResult.getItem(), negated, rightOperandTransformationResult.getItem()), rightOperandTransformationResult.getUpdatedContext());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((leftOperand == null) ? 0 : leftOperand.hashCode());
        result = prime * result + (negated ? 1231 : 1237);
        result = prime * result + ((rightOperand == null) ? 0 : rightOperand.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof SetTest1)) {
            return false;
        }
        
        final SetTest1 other = (SetTest1) obj;
        
        return Objects.equals(leftOperand, other.leftOperand) &&
                Objects.equals(rightOperand, other.rightOperand) &&
                Objects.equals(negated, other.negated);
    }
}