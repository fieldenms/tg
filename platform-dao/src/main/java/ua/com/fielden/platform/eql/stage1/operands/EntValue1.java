package ua.com.fielden.platform.eql.stage1.operands;

import java.util.Objects;

import ua.com.fielden.platform.eql.stage1.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage2.operands.EntValue2;

public class EntValue1 implements ISingleOperand1<EntValue2> {
    public final Object value;
    public final boolean ignoreNull;

    public EntValue1(final Object value) {
        this(value, false);
    }

    public EntValue1(final Object value, final boolean ignoreNull) {
        this.value = value;
        this.ignoreNull = ignoreNull;
    }

    @Override
    public EntValue2 transform(final PropsResolutionContext context) {
        return new EntValue2(value, ignoreNull);
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

        if (!(obj instanceof EntValue1)) {
            return false;
        }

        final EntValue1 other = (EntValue1) obj;
        
        return Objects.equals(value, other.value) && ignoreNull == other.ignoreNull;
    }
}