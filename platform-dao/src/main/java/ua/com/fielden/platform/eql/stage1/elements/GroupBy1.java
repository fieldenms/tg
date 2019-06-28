package ua.com.fielden.platform.eql.stage1.elements;

import java.util.Objects;

import ua.com.fielden.platform.eql.stage1.elements.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.elements.GroupBy2;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;

public class GroupBy1 {
    public final ISingleOperand1<? extends ISingleOperand2> operand;

    public GroupBy1(final ISingleOperand1<? extends ISingleOperand2> operand) {
        this.operand = operand;
    }

    public TransformationResult<GroupBy2> transform(final PropsResolutionContext resolutionContext) {
        final TransformationResult<? extends ISingleOperand2> operandTransformationResult = operand.transform(resolutionContext);
        return new TransformationResult<GroupBy2>(new GroupBy2(operandTransformationResult.item), operandTransformationResult.updatedContext);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((operand == null) ? 0 : operand.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof GroupBy1)) {
            return false;
        }
        
        final GroupBy1 other = (GroupBy1) obj;
        
        return Objects.equals(operand, other.operand);
    }
}