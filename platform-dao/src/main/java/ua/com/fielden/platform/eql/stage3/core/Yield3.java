package ua.com.fielden.platform.eql.stage3.core;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.util.Objects;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;

public class Yield3 {
    public final ISingleOperand3 operand;
    public final String alias;
    public final String column;
    public final boolean isHeader;
    public final Class<?> type;
    public final Object hibType;

    public Yield3(final ISingleOperand3 operand, final String alias, final int columnId, final boolean isHeader, final Class<?> type, final Object hibType) {
        this.operand = operand;
        this.alias = alias;
        this.column = isEmpty(alias) || isHeader ? null : "C_" + columnId;
        this.isHeader = isHeader;
        this.hibType = hibType;
        this.type = type;
    }

    public String sql(final DbVersion dbVersion) {
        return operand.sql(dbVersion) + (column == null ? "" : " AS " + column);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((alias == null) ? 0 : alias.hashCode());
        result = prime * result + operand.hashCode();
        result = prime * result + ((hibType == null) ? 0 : hibType.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + (isHeader ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Yield3)) {
            return false;
        }
        
        final Yield3 other = (Yield3) obj;
        
        return Objects.equals(operand, other.operand) && Objects.equals(alias, other.alias) && Objects.equals(isHeader, other.isHeader) && Objects.equals(hibType, other.hibType) && Objects.equals(type, other.type);
    }
}