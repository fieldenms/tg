package ua.com.fielden.platform.eql.stage3.operands;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.stage3.queries.SubQuery3;
import ua.com.fielden.platform.meta.IDomainMetadata;

public record QueryBasedSet3 (SubQuery3 model) implements ISetOperand3 {

    @Override
    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        return "(" + model.sql(metadata, dbVersion) + ")";
    }

}
