package ua.com.fielden.platform.eql.stage3.elements;

import java.util.Objects;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;

public class OrderBy3 {
    public final ISingleOperand3 operand;
    public final Yield3 yield;
    public final boolean isDesc;

    public OrderBy3(final ISingleOperand3 operand, final boolean isDesc) {
        this.operand = operand;
        this.yield = null;
        this.isDesc = isDesc;
    }
    
    public OrderBy3(final Yield3 yield, final boolean isDesc) {
        this.operand = null;
        this.yield = yield;
        this.isDesc = isDesc;
    }

    public String sql(final DbVersion dbVersion) {
        return (operand != null ? operand.sql(dbVersion) : yield.column) +  (isDesc ? " DESC" : " ASC");
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (isDesc ? 1231 : 1237);
        result = prime * result + ((operand == null) ? 0 : operand.hashCode());
        result = prime * result + ((yield == null) ? 0 : yield.hashCode());
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

        return (isDesc == other.isDesc) && Objects.equals(operand, other.operand) && Objects.equals(yield, other.yield);
    }
}