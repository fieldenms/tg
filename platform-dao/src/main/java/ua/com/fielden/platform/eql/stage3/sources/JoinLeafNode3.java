package ua.com.fielden.platform.eql.stage3.sources;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.utils.ToString;

public record JoinLeafNode3 (ISource3 source) implements IJoinNode3, ToString.IFormattable {

    @Override
    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        return source.sql(metadata, dbVersion);
    }

    @Override
    public boolean needsParentheses() {
        return false;
    }

    @Override
    public String toString() {
        return toString(ToString.separateLines);
    }

    @Override
    public String toString(final ToString.IFormat format) {
        return format.toString(this)
                .add("source", source)
                .$();
    }

}
