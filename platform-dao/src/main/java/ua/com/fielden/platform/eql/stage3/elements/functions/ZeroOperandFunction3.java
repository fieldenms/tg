package ua.com.fielden.platform.eql.stage3.elements.functions;

import java.util.Objects;

import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;

abstract class ZeroOperandFunction3 implements ISingleOperand3 {

    private final String functionName;

    public ZeroOperandFunction3(final String functionName) {
        this.functionName = functionName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((functionName == null) ? 0 : functionName.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof ZeroOperandFunction3)) {
            return false;
        }
        
        final ZeroOperandFunction3 other = (ZeroOperandFunction3) obj;

        return Objects.equals(functionName, other.functionName);
    }
}