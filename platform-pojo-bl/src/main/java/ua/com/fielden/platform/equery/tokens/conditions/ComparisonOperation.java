package ua.com.fielden.platform.equery.tokens.conditions;

import ua.com.fielden.platform.equery.Rdbms;



public enum ComparisonOperation {
//    LIKE("LIKE") {
//	public String getValue() {
//	    return Rdbms.rdbms.getLike();
//	}
//    },
    LIKE("LIKE"),
    IN("IN"), BETWEEN("BETWEEN"), NE("<>"), EQ("="), GT(">"), GE(">="), LT("<"), LE("<=");

    protected final String value;

    ComparisonOperation(final String value) {
	this.value = value;
    }

    public String getValue() {
	if ("LIKE".equals(name())) {
	    return Rdbms.rdbms.getLike();
	}
	return value;
    }

    @Override
    public String toString() {
	return value;
    }
}
