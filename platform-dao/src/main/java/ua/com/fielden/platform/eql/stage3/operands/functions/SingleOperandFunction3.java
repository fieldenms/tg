package ua.com.fielden.platform.eql.stage3.operands.functions;

import java.util.Objects;

import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;

abstract class SingleOperandFunction3 extends AbstractFunction3 {

    public final ISingleOperand3 operand;

    public SingleOperandFunction3(final ISingleOperand3 operand, final PropType type) {
        super(type);
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
        
        if (!(obj instanceof SingleOperandFunction3)) {
            return false;
        }

        if (!super.equals(obj)) {
            return false;
        }        

        final SingleOperandFunction3 other = (SingleOperandFunction3) obj;
        
        return Objects.equals(operand, other.operand);
    }

}