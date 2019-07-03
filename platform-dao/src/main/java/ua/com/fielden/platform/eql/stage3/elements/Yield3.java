package ua.com.fielden.platform.eql.stage3.elements;

import java.util.Objects;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;

public class Yield3 {
    public final ISingleOperand3 operand;
    public final String alias;

    public Yield3(final ISingleOperand3 operand, final String alias) {
        this.operand = operand;
        this.alias = alias;
    }

    public String sql(final DbVersion dbVersion) {
        return operand.sql(dbVersion) + " AS " + alias;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((alias == null) ? 0 : alias.hashCode());
        result = prime * result + ((operand == null) ? 0 : operand.hashCode());
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
        
        return Objects.equals(operand, other.operand) && Objects.equals(alias, other.alias);
    }
}