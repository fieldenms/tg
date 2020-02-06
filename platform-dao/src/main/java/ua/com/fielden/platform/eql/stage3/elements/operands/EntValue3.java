package ua.com.fielden.platform.eql.stage3.elements.operands;

import java.util.Objects;

import ua.com.fielden.platform.entity.query.DbVersion;

public class EntValue3 implements ISingleOperand3 {
    public final Object value;
    public final int paramId;

    public EntValue3(final Object value, final int paramId) {
        this.value = value;
        this.paramId = paramId;
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

        if (!(obj instanceof EntValue3)) {
            return false;
        }
        
        final EntValue3 other = (EntValue3) obj;
        
        return Objects.equals(value, other.value) && paramId == other.paramId;
    }
}