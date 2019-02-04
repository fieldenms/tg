package ua.com.fielden.platform.eql.stage1.elements;

import ua.com.fielden.platform.eql.meta.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage2.elements.GroupBy2;
import ua.com.fielden.platform.eql.stage2.elements.ISingleOperand2;

public class GroupBy1 implements ITransformableToS2<GroupBy2>{
    private final ISingleOperand1<? extends ISingleOperand2> operand;

    public GroupBy1(final ISingleOperand1<? extends ISingleOperand2> operand) {
        this.operand = operand;
    }

    @Override
    public GroupBy2 transform(PropsResolutionContext resolver) {
        return new GroupBy2(operand.transform(resolver));
    }

    public ISingleOperand1<? extends ISingleOperand2> getOperand() {
        return operand;
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
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof GroupBy1)) {
            return false;
        }
        final GroupBy1 other = (GroupBy1) obj;
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