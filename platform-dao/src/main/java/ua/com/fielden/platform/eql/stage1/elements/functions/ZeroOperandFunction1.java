package ua.com.fielden.platform.eql.stage1.elements.functions;

import java.util.Objects;

import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;

abstract class ZeroOperandFunction1<S2 extends ISingleOperand2<?>> extends AbstractFunction1<S2> {

    private final String functionName;

    public ZeroOperandFunction1(final String functionName) {
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

        if (!(obj instanceof ZeroOperandFunction1)) {
            return false;
        }
        
        final ZeroOperandFunction1<S2> other = (ZeroOperandFunction1<S2>) obj;

        return Objects.equals(functionName, other.functionName);
    }
}