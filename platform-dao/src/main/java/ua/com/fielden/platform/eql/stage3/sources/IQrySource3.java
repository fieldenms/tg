package ua.com.fielden.platform.eql.stage3.sources;

import ua.com.fielden.platform.eql.stage3.IGenerateSql;

public interface IQrySource3 extends IGenerateSql {
    String column(final String colName);
    String sqlAlias();
}