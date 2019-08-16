package ua.com.fielden.platform.eql.stage3.elements.sources;

import ua.com.fielden.platform.entity.query.DbVersion;

public interface IQrySources3 {
    String sql(final DbVersion dbVersion);
}
