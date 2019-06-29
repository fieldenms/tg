package ua.com.fielden.platform.eql.stage1.elements.conditions;

import java.util.Objects;

import ua.com.fielden.platform.entity.query.fluent.LikeOptions;
import ua.com.fielden.platform.eql.stage1.elements.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage1.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage1.elements.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.elements.conditions.LikeTest2;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;

public class LikeTest1 implements ICondition1<LikeTest2> {
    private final ISingleOperand1<? extends ISingleOperand2<?>> leftOperand;
    private final ISingleOperand1<? extends ISingleOperand2<?>> rightOperand;
    private final LikeOptions options;

    public LikeTest1(final ISingleOperand1<? extends ISingleOperand2<?>> leftOperand, final ISingleOperand1<? extends ISingleOperand2<?>> rightOperand, final LikeOptions options) {
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
        this.options = options;
    }

    @Override
    public TransformationResult<LikeTest2> transform(final PropsResolutionContext context) {
        final TransformationResult<? extends ISingleOperand2<?>> leftOperandTransformationResult = leftOperand.transform(context);
        final TransformationResult<? extends ISingleOperand2<?>> rightOperandTransformationResult = rightOperand.transform(leftOperandTransformationResult.updatedContext);
        return new TransformationResult<LikeTest2>(new LikeTest2(leftOperandTransformationResult.item, rightOperandTransformationResult.item, options), rightOperandTransformationResult.updatedContext);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((leftOperand == null) ? 0 : leftOperand.hashCode());
        result = prime * result + ((options == null) ? 0 : options.hashCode());
        result = prime * result + ((rightOperand == null) ? 0 : rightOperand.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (!(obj instanceof LikeTest1)) {
            return false;
        }
        
        final LikeTest1 other = (LikeTest1) obj;
        
        return Objects.equals(leftOperand, other.leftOperand) &&
                Objects.equals(rightOperand, other.rightOperand) &&
                Objects.equals(options, other.options);
    }
}