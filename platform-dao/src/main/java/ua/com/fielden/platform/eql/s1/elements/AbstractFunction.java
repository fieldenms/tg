package ua.com.fielden.platform.eql.s1.elements;

import ua.com.fielden.platform.entity.query.generation.DbVersion;


public abstract class AbstractFunction {
    private final DbVersion dbVersion;

    public AbstractFunction(final DbVersion dbVersion) {
	this.dbVersion = dbVersion;
    }

    protected DbVersion getDbVersion() {
	return dbVersion;
    }
}