package ua.com.fielden.platform.eql.stage3.elements.functions;

import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;

public class CountAll3 implements ISingleOperand3 {

    @Override
    public String sql() {
        // TODO Auto-generated method stub
        return null;
    }
   
    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = 1;
        return prime * result + CountAll3.class.getName().hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        return this == obj || obj instanceof CountAll3;
    } 
}
