package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.DbVersion;

public class TypeCastAsString implements ITypeCast {

    private final int length;

    public TypeCastAsString(final int length) {
	super();
	this.length = length;
    }

    @Override
    public String typecast(final String argument, final DbVersion dbVersion) {
	if (DbVersion.H2.equals(dbVersion)) {
	    return "CAST(" + argument + " AS VARCHAR(" + length + "))" ;
	} else {
	    return argument;
	}
    }
}
