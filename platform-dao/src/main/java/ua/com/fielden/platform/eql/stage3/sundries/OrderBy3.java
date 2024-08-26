package ua.com.fielden.platform.eql.stage3.sundries;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.meta.IDomainMetadata;

public record OrderBy3 (ISingleOperand3 operand, Yield3 yield, boolean isDesc) {

    public OrderBy3(final ISingleOperand3 operand, final boolean isDesc) {
        this(operand, null, isDesc);
    }

    public OrderBy3(final Yield3 yield, final boolean isDesc) {
        this(null, yield, isDesc);
    }

    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        return (operand != null ? operand.sql(metadata, dbVersion) : yield.column()) + (isDesc ? " DESC" : " ASC");
    }

}
