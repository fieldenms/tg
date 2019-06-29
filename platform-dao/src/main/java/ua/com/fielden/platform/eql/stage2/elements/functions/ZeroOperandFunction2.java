package ua.com.fielden.platform.eql.stage2.elements.functions;

import java.util.Objects;

import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;

abstract class ZeroOperandFunction2<S3 extends ISingleOperand3> extends AbstractFunction2<S3> {

    private final String functionName;

    public ZeroOperandFunction2(final String functionName) {
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

        if (!(obj instanceof ZeroOperandFunction2)) {
            return false;
        }
        
        final ZeroOperandFunction2<S3> other = (ZeroOperandFunction2<S3>) obj;

        return Objects.equals(functionName, other.functionName);
    }
}