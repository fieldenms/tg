package ua.com.fielden.platform.eql.stage2.elements.functions;

import java.util.List;
import java.util.Objects;

import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;

public class Concat2 extends AbstractFunction2 {

    private final List<ISingleOperand2> operands;

    public Concat2(final List<ISingleOperand2> operands) {
        this.operands = operands;
    }

    public List<ISingleOperand2> getOperands() {
        return operands;
    }

    @Override
    public Class type() {
        return String.class;
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
    
        if (!(obj instanceof Concat2)) {
            return false;
        }
        
        final Concat2 other = (Concat2) obj;
        
        return Objects.equals(operands, other.operands);
    }
}