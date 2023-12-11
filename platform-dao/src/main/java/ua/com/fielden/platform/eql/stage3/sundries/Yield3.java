package ua.com.fielden.platform.eql.stage3.sundries;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.util.Objects;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.meta.PropType;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;

public class Yield3 {
    public final ISingleOperand3 operand;
    public final String alias;
    public final String column;
    public final PropType type; // declared type (the one from property declarations (java type and annotation); for non-calculated properties it will be the same as operand.type(); 
    //for calculated properties operand.type() will be inferred from actual expression and may differ from the declared one.

    public Yield3(final ISingleOperand3 operand, final String alias, final int columnId, final PropType type) {
        this.operand = operand;
        this.alias = alias;
        this.column = isEmpty(alias) ? null : "C_" + columnId;
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
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        
        return Objects.equals(operand, other.operand) && Objects.equals(alias, other.alias) && Objects.equals(type, other.type);
    }
}