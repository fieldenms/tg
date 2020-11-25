package ua.com.fielden.platform.eql.stage1.core;

import java.util.Objects;

import ua.com.fielden.platform.eql.stage1.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.core.GroupBy2;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;

public class GroupBy1 {
    public final ISingleOperand1<? extends ISingleOperand2<?>> operand;

    public GroupBy1(final ISingleOperand1<? extends ISingleOperand2<?>> operand) {
        this.operand = operand;
    }

    public GroupBy2 transform(final PropsResolutionContext context) {
        return new GroupBy2(operand.transform(context));
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

        if (!(obj instanceof GroupBy1)) {
            return false;
        }
        
        final GroupBy1 other = (GroupBy1) obj;
        
        return Objects.equals(operand, other.operand);
    }
}