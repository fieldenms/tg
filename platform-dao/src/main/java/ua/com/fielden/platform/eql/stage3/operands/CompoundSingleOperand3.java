package ua.com.fielden.platform.eql.stage3.operands;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.fluent.enums.ArithmeticalOperator;
import ua.com.fielden.platform.meta.IDomainMetadata;

public record CompoundSingleOperand3 (ISingleOperand3 operand, ArithmeticalOperator operator) {

    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        return operator.value + operand.sql(metadata, dbVersion);
    }

}
