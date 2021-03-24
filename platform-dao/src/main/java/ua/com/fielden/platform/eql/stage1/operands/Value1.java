package ua.com.fielden.platform.eql.stage1.operands;

import java.util.Objects;

import ua.com.fielden.platform.eql.stage1.TransformationContext;
import ua.com.fielden.platform.eql.stage2.operands.Value2;

public class Value1 implements ISingleOperand1<Value2> {
    public final Object value;
    public final boolean ignoreNull;

    public Value1(final Object value) {
        this(value, false);
    }

    public Value1(final Object value, final boolean ignoreNull) {
        this.value = value;
        this.ignoreNull = ignoreNull;
    }

    @Override
    public Value2 transform(final TransformationContext context) {
        return new Value2(value, ignoreNull);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (ignoreNull ? 1231 : 1237);
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Value1)) {
            return false;
        }

        final Value1 other = (Value1) obj;
        
        return Objects.equals(value, other.value) && ignoreNull == other.ignoreNull;
    }
}