package ua.com.fielden.platform.eql.stage3.sundries;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.meta.IDomainMetadata;

import java.util.Objects;

public class GroupBy3 {
    public final ISingleOperand3 operand;

    public GroupBy3(final ISingleOperand3 operand) {
        this.operand = operand;
    }

    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        return operand.sql(metadata, dbVersion);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + operand.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof GroupBy3)) {
            return false;
        }

        final GroupBy3 other = (GroupBy3) obj;

        return Objects.equals(operand, other.operand);
    }

}
