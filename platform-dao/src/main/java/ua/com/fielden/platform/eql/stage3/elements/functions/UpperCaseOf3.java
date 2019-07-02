package ua.com.fielden.platform.eql.stage3.elements.functions;

import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;

public class UpperCaseOf3 extends SingleOperandFunction3 {

    public UpperCaseOf3(final ISingleOperand3 operand) {
        super(operand);
    }

    @Override
    public String sql() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + UpperCaseOf3.class.getName().hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        return this == obj || super.equals(obj) && obj instanceof UpperCaseOf3;
    }  
}