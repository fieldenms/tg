package ua.com.fielden.platform.eql.stage3.operands.queries;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.stage3.QueryComponents3;
import ua.com.fielden.platform.eql.stage3.operands.ISingleOperand3;

public class SubQuery3 extends AbstractQuery3 implements ISingleOperand3 {
    
    public SubQuery3(final QueryComponents3 queryComponents, final Class<?> resultType) {
        super(queryComponents, resultType);
    }

    @Override
    public String sql(final DbVersion dbVersion) {
        return "(" + super.sql(dbVersion) + ")";
    }
    
    @Override
    public Class<?> type() {
        return resultType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + SubQuery3.class.getName().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || super.equals(obj) && obj instanceof SubQuery3;
    }
}