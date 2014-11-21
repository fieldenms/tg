package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.DbVersion;

public enum TypeCastAsInteger implements ITypeCast {

    INSTANCE;

    @Override
    public String typecast(final String argument, final DbVersion dbVersion) {
        if (DbVersion.H2.equals(dbVersion)) {
            return "CAST(" + argument + " AS INT)";
        } else {
            return argument;
        }
    }
}