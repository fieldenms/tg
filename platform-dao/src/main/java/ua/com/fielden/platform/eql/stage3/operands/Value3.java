package ua.com.fielden.platform.eql.stage3.operands;

import java.util.Objects;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.meta.PropType;

public class Value3 implements ISingleOperand3 {
    public final Object value; // can be 'null' in case of yield stmt
    public final String paramName;
    public final PropType type;

    public Value3(final Object value, final String paramName, PropType type) {
        this.value = value;
        this.paramName = paramName;
        this.type = type;
    }

    @Override
    public String sql(final DbVersion dbVersion) {
        if (value == null) {
            return " NULL ";
        } else {
            return paramName == null ? (value instanceof String ? "'" + value + "'" : value.toString()) : ":" + paramName;    
        }
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

        if (!(obj instanceof Value3)) {
            return false;
        }
        
        final Value3 other = (Value3) obj;
        
        return Objects.equals(value, other.value) && paramName == other.paramName;
    }

    @Override
    public PropType type() {
        return type;
    }
}