package ua.com.fielden.platform.eql.stage1.elements.conditions;

import ua.com.fielden.platform.eql.meta.PropsResolutionContext;
import ua.com.fielden.platform.eql.meta.TransformationResult;
import ua.com.fielden.platform.eql.stage1.elements.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.elements.conditions.NullTest2;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;

public class NullTest1 implements ICondition1<NullTest2> {
    private final ISingleOperand1<? extends ISingleOperand2> operand;
    private final boolean negated;

    public NullTest1(final ISingleOperand1<? extends ISingleOperand2> operand, final boolean negated) {
        this.operand = operand;
        this.negated = negated;
    }

    public NullTest1(final ISingleOperand1<? extends ISingleOperand2> operand) {
        this(operand, false);
    }

    @Override
    public TransformationResult<NullTest2> transform(final PropsResolutionContext resolutionContext) {
        final TransformationResult<? extends ISingleOperand2> operandTransformationResult = operand.transform(resolutionContext);
        return new TransformationResult<NullTest2>(new NullTest2(operandTransformationResult.getItem(), negated), operandTransformationResult.getUpdatedContext());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (negated ? 1231 : 1237);
        result = prime * result + ((operand == null) ? 0 : operand.hashCode());
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
        if (!(obj instanceof NullTest1)) {
            return false;
        }
        final NullTest1 other = (NullTest1) obj;
        if (negated != other.negated) {
            return false;
        }
        if (operand == null) {
            if (other.operand != null) {
                return false;
            }
        } else if (!operand.equals(other.operand)) {
            return false;
        }
        return true;
    }
}