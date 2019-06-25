package ua.com.fielden.platform.eql.stage3.elements.operands;

import java.util.Objects;

public class EntValue3 implements ISingleOperand3 {
    public final Object value;

    public EntValue3(final Object value) {
        this.value = value;
    }

    @Override
    public String sql() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof EntValue3)) {
            return false;
        }
        
        final EntValue3 other = (EntValue3) obj;
        
        return Objects.equals(value, other.value);
    }
}