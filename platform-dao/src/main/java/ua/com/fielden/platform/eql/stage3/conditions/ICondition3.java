package ua.com.fielden.platform.eql.stage3.conditions;

import ua.com.fielden.platform.entity.query.DbVersion;

public interface ICondition3 {
    String sql(final DbVersion dbVersion);
}
