package ua.com.fielden.platform.eql.stage3.elements.functions;

import java.util.List;
import java.util.Objects;

import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;

public class Concat3 implements ISingleOperand3 {

    private final List<ISingleOperand3> operands;

    public Concat3(final List<ISingleOperand3> operands) {
        this.operands = operands;
    }

    @Override
    public String sql() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((operands == null) ? 0 : operands.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
    
        if (!(obj instanceof Concat3)) {
            return false;
        }
        
        final Concat3 other = (Concat3) obj;
        
        return Objects.equals(operands, other.operands);
    }
}