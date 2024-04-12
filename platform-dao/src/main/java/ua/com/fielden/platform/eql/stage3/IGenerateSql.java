package ua.com.fielden.platform.eql.stage3;

import ua.com.fielden.platform.eql.meta.EqlDomainMetadata;

public interface IGenerateSql {

    String sql(final EqlDomainMetadata metadata);

}
