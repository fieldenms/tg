package ua.com.fielden.platform.eql.stage1.elements.conditions;

import ua.com.fielden.platform.entity.query.fluent.LikeOptions;
import ua.com.fielden.platform.eql.meta.PropsResolutionContext;
import ua.com.fielden.platform.eql.meta.TransformationResult;
import ua.com.fielden.platform.eql.stage1.elements.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.elements.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.elements.LikeTest2;

public class LikeTest1 implements ICondition1<LikeTest2> {
    private final ISingleOperand1<? extends ISingleOperand2> leftOperand;
    private final ISingleOperand1<? extends ISingleOperand2> rightOperand;
    private final boolean negated;
    private final boolean caseInsensitive;

    public LikeTest1(final ISingleOperand1<? extends ISingleOperand2> leftOperand, final ISingleOperand1<? extends ISingleOperand2> rightOperand, final LikeOptions options) {
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
        this.negated = options.negated;
        this.caseInsensitive = options.caseInsensitive;
        /*TODO TFM compare with original*/
    }

    @Override
    public TransformationResult<LikeTest2> transform(final PropsResolutionContext resolutionContext) {
        final TransformationResult<? extends ISingleOperand2> leftOperandTransformationResult = leftOperand.transform(resolutionContext);
        final TransformationResult<? extends ISingleOperand2> rightOperandTransformationResult = rightOperand.transform(leftOperandTransformationResult.getUpdatedContext());
        return new TransformationResult<LikeTest2>(new LikeTest2(leftOperandTransformationResult.getItem(), rightOperandTransformationResult.getItem(), negated, caseInsensitive), rightOperandTransformationResult.getUpdatedContext());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (caseInsensitive ? 1231 : 1237);
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
        if (!(obj instanceof LikeTest1)) {
            return false;
        }
        final LikeTest1 other = (LikeTest1) obj;
        if (caseInsensitive != other.caseInsensitive) {
            return false;
        }
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