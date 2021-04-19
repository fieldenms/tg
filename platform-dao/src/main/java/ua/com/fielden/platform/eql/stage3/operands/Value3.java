package ua.com.fielden.platform.eql.stage3.operands;

import java.util.Objects;

import ua.com.fielden.platform.entity.query.DbVersion;

public class Value3 implements ISingleOperand3 {
    public final Object value;
    public final int paramId;
    public final Object hibType;

    public Value3(final Object value, final int paramId, final Object hibType) {
        this.value = value;
        this.paramId = paramId;
        this.hibType = hibType;
    }

    public String getParamName() {
        return paramId != 0 ? "P_" + paramId : null;
    }
    
    @Override
    public String sql(final DbVersion dbVersion) {
        return paramId == 0 ? (value instanceof String ? "'" + value + "'" : value.toString()) : ":" + getParamName(); 
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
        
        return Objects.equals(value, other.value) && paramId == other.paramId;
    }

    @Override
    public Class<?> type() {
        return value != null ? value.getClass() : null;
    }

    @Override
    public Object hibType() {
        return hibType;
    }
}