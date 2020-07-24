package ua.com.fielden.platform.eql.stage3.elements.sources;

import ua.com.fielden.platform.entity.query.DbVersion;

public interface IQrySource3 {
    String column(final String colName);
    String sqlAlias();
    String sql(final DbVersion dbVersion);
}