package ua.com.fielden.platform.eql.stage3.sundries;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.meta.IDomainMetadata;

import java.util.List;

import static java.util.stream.Collectors.joining;

public record GroupBys3 (List<GroupBy3> groups) {

    public String sql(final IDomainMetadata metadata, final DbVersion dbVersion) {
        return groups.stream().map(g -> g.sql(metadata, dbVersion)).collect(joining(", "));
    }

}
