package ua.com.fielden.platform.eql.stage3.elements;

import java.util.Objects;

import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;

public class OrderBy3 {
    public final ISingleOperand3 operand;
    public final String yieldName;
    public Yield3 yield;
    public final boolean isDesc;

    public OrderBy3(final ISingleOperand3 operand, final boolean isDesc) {
        this.operand = operand;
        this.yieldName = null;
        this.isDesc = isDesc;
    }

    public OrderBy3(final String yieldName, final boolean isDesc) {
        this.operand = null;
        this.yieldName = yieldName;
        this.isDesc = isDesc;
    }

    public Yield3 getYield() {
        return yield;
    }

    public void setYield(final Yield3 yield) {
        this.yield = yield;
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

        if (!(obj instanceof OrderBy3)) {
            return false;
        }
        
        final OrderBy3 other = (OrderBy3) obj;

        return Objects.equals(isDesc, other.isDesc) &&
                Objects.equals(operand, other.operand) &&
                Objects.equals(yield, other.yield) &&
                Objects.equals(yieldName, other.yieldName);
    }
}