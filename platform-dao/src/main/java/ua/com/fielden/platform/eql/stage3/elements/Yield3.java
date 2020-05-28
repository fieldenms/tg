package ua.com.fielden.platform.eql.stage3.elements;

import java.util.Objects;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.stage3.elements.operands.ISingleOperand3;

public class Yield3 {
    public final ISingleOperand3 operand;
    public final String alias;
    public final Column column;
    public final boolean isHeader;

    public Yield3(final ISingleOperand3 operand, final String alias, final boolean isHeader) {
        this.operand = operand;
        this.alias = alias;
        this.column = StringUtils.isEmpty(alias) || isHeader ? null : new Column(alias.replace(".", "_").toUpperCase() + "_");
        this.isHeader = isHeader;
    }

    public Yield3(final ISingleOperand3 operand, final String alias) {
        this(operand, alias, false);
    }

    public String sql(final DbVersion dbVersion) {
        return operand.sql(dbVersion) + (column == null ? "" : " AS " + column.name);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((alias == null) ? 0 : alias.hashCode());
        result = prime * result + operand.hashCode();
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
        
        return Objects.equals(operand, other.operand) && Objects.equals(alias, other.alias) && Objects.equals(isHeader, other.isHeader);
    }
}