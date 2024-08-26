package ua.com.fielden.platform.eql.stage3.conditions;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.stage3.operands.ISetOperand3;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;
import ua.com.fielden.platform.meta.IDomainMetadata;

public record SetPredicate3(ISingleOperand3 leftOperand, boolean negated, ISetOperand3 rightOperand)
        implements ICondition3
{

    @Override
    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        return leftOperand.sql(metadata, dbVersion) + (negated ? " NOT IN " : " IN ") + rightOperand.sql(metadata, dbVersion);
    }

}
