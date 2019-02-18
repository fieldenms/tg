package ua.com.fielden.platform.eql.stage1.elements.conditions;

import ua.com.fielden.platform.eql.meta.PropsResolutionContext;
import ua.com.fielden.platform.eql.meta.TransformationResult;
import ua.com.fielden.platform.eql.stage1.elements.operands.ISetOperand1;
import ua.com.fielden.platform.eql.stage1.elements.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.elements.ISetOperand2;
import ua.com.fielden.platform.eql.stage2.elements.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.elements.SetTest2;

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
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof SetTest1)) {
            return false;
        }
        final SetTest1 other = (SetTest1) obj;
        if (leftOperand == null) {
            if (other.leftOperand != null) {
                return false;
            }
        } else if (!leftOperand.equals(other.leftOperand)) {
            return false;
        }
        if (negated != other.negated) {
            return false;
        }
        if (rightOperand == null) {
            if (other.rightOperand != null) {
                return false;
            }
        } else if (!rightOperand.equals(other.rightOperand)) {
            return false;
        }
        return true;
    }
}