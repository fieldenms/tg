package ua.com.fielden.platform.eql.stage3;

import ua.com.fielden.platform.entity.query.DbVersion;

public interface IGenerateSql {
    String sql(final DbVersion dbVersion);
}
