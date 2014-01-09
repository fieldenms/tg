package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.DbVersion;

public class TypeCastAsBoolean implements ITypeCast {

    @Override
    public String typecast(final String argument, final DbVersion dbVersion) {
	if (DbVersion.H2.equals(dbVersion)) {
	    return "CAST(" + argument + " AS CHAR(1))" ;
	} else {
	    return argument;
	}
    }
}
