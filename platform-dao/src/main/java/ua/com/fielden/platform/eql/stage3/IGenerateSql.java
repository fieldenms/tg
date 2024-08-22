package ua.com.fielden.platform.eql.stage3;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.meta.IDomainMetadata;

public interface IGenerateSql {

    String sql(final IDomainMetadata metadata, final DbVersion dbVersion);

}
