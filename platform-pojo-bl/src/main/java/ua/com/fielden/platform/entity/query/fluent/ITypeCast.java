package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.DbVersion;

public interface ITypeCast {
    String typecast(String argument, final DbVersion dbVersion);
}
