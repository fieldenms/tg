package ua.com.fielden.platform.eql.stage3.operands;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.eql.stage3.queries.SubQuery3;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.utils.ToString;

public record QueryBasedSet3 (SubQuery3 model) implements ISetOperand3, ToString.IFormattable {

    @Override
    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        return "(" + model.sql(metadata, dbVersion) + ")";
    }

    @Override
    public String toString() {
        return toString(ToString.separateLines);
    }

    @Override
    public String toString(final ToString.IFormat format) {
        return format.toString(this)
                .add("model", model)
                .$();
    }

}
