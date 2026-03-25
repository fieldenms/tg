package ua.com.fielden.platform.eql.stage3.operands.functions;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.meta.IDomainMetadata;

public record ConcatOfOrderItem3(ISingleOperand3 operand, boolean isDesc) {

    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        return operand.sql(metadata, dbVersion) + (isDesc ? " DESC" : " ASC");
    }

}
