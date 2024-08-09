package ua.com.fielden.platform.eql.stage1.sundries;

import java.util.Objects;

import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.eql.stage2.sundries.OrderBy2;

public class OrderBy1 {
    public final ISingleOperand1<? extends ISingleOperand2<?>> operand;
    public final String yieldName;
    public final boolean isDesc;

    public OrderBy1(final ISingleOperand1<? extends ISingleOperand2<?>> operand, final boolean isDesc) {
        this.operand = operand;
        this.isDesc = isDesc;
        this.yieldName = null;
    }

    public OrderBy1(final String yieldName, final boolean isDesc) {
        this.operand = null;
        this.isDesc = isDesc;
        this.yieldName = yieldName;
    }

    public OrderBy2 transform(final TransformationContextFromStage1To2 context) {
        return operand != null ? new OrderBy2(operand.transform(context), isDesc) : new OrderBy2(yieldName, isDesc);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (isDesc ? 1231 : 1237);
        result = prime * result + ((operand == null) ? 0 : operand.hashCode());
        result = prime * result + ((yieldName == null) ? 0 : yieldName.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof OrderBy1)) {
            return false;
        }

        final OrderBy1 other = (OrderBy1) obj;

        return Objects.equals(operand, other.operand) && Objects.equals(yieldName, other.yieldName) && (isDesc == other.isDesc);
    }
}
