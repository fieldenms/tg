package ua.com.fielden.platform.eql.stage2.sundries;

import java.util.Objects;

import ua.com.fielden.platform.eql.stage2.TransformationContext2;
import ua.com.fielden.platform.eql.stage2.TransformationResult2;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.eql.stage3.sundries.GroupBy3;

public class GroupBy2 {
    public final ISingleOperand2<? extends ISingleOperand3> operand;

    public GroupBy2(final ISingleOperand2<? extends ISingleOperand3> operand) {
        this.operand = operand;
    }
    
    public TransformationResult2<GroupBy3> transform(final TransformationContext2 context) {
        final TransformationResult2<? extends ISingleOperand3> operandTr = operand.transform(context);
        return new TransformationResult2<>(new GroupBy3(operandTr.item), operandTr.updatedContext);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + operand.hashCode();
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
