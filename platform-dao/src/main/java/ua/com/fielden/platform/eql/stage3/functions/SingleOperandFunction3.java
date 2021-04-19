package ua.com.fielden.platform.eql.stage3.functions;

import java.util.Objects;

import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;

abstract class SingleOperandFunction3 extends AbstractFunction3 {

    public final ISingleOperand3 operand;

    public SingleOperandFunction3(final ISingleOperand3 operand, final Class<?> type, final Object hibType) {
        super(type, hibType);
        this.operand = operand;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + operand.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (!super.equals(obj)) {
            return false;
        }        

        if (!(obj instanceof SingleOperandFunction3)) {
            return false;
        }
        
        final SingleOperandFunction3 other = (SingleOperandFunction3) obj;
        
        return Objects.equals(operand, other.operand);
    }
}