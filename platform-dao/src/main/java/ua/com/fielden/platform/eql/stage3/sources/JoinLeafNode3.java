package ua.com.fielden.platform.eql.stage3.sources;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.meta.IDomainMetadata;

public record JoinLeafNode3 (ISource3 source) implements IJoinNode3 {

    @Override
    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        return source.sql(metadata, dbVersion);
    }

    @Override
    public boolean needsParentheses() {
        return false;
    }

}
