package ua.com.fielden.platform.eql.stage2.elements;

import java.util.Objects;

import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.elements.GroupBy3;
import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;

public class GroupBy2 {
    public final ISingleOperand2 operand;

    public GroupBy2(final ISingleOperand2 operand) {
        this.operand = operand;
    }
    
    public TransformationResult<GroupBy3> transform(final TransformationContext resolutionContext) {
        final TransformationResult<? extends ISingleOperand3> operandTransformationResult = operand.transform(resolutionContext);
        return new TransformationResult<GroupBy3>(new GroupBy3(operandTransformationResult.getItem()), operandTransformationResult.getUpdatedContext());
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

        if (!(obj instanceof GroupBy2)) {
            return false;
        }
        
        final GroupBy2 other = (GroupBy2) obj;
        
        return Objects.equals(operand, other.operand);
    }
}
