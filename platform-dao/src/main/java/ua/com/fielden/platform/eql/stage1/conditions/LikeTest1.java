package ua.com.fielden.platform.eql.stage1.conditions;

import java.util.Objects;

import ua.com.fielden.platform.entity.query.fluent.LikeOptions;
import ua.com.fielden.platform.eql.stage1.TransformationContext;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.conditions.LikeTest2;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;

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
    public LikeTest2 transform(final TransformationContext context) {
        return new LikeTest2(leftOperand.transform(context), rightOperand.transform(context), options);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + leftOperand.hashCode();
        result = prime * result + options.hashCode();
        result = prime * result + rightOperand.hashCode();
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