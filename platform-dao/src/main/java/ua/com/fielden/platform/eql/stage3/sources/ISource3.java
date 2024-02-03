package ua.com.fielden.platform.eql.stage3.sources;

import ua.com.fielden.platform.eql.stage3.IGenerateSql;

public interface ISource3 extends IGenerateSql {
    String column(final String propName);
    Integer id();
}