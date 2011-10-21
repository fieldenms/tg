package ua.com.fielden.platform.equery;

public enum HqlDateFunctions {

    DAY {
	@Override
	public String toString() {
	    return "Day";
	}

	@Override
	public String getActualExpression(final String actualProperty) {
	    return Rdbms.rdbms.getDayExpression(actualProperty);
	}
    },
    MONTH {
	@Override
	public String toString() {
	    return "Month";
	}

	@Override
	public String getActualExpression(final String actualProperty) {
	    return Rdbms.rdbms.getMonthExpression(actualProperty);
	}
    },
    YEAR {
	@Override
	public String toString() {
	    return "Year";
	}

	@Override
	public String getActualExpression(final String actualProperty) {
	    return Rdbms.rdbms.getYearExpression(actualProperty);
	}
    };

    public abstract String getActualExpression(String actualProperty);
}
