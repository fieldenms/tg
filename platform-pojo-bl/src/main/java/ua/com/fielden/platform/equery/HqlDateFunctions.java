package ua.com.fielden.platform.equery;

public enum HqlDateFunctions {

    DAY {
	@Override
	public String toString() {
	    return "Day";
	}

	@Override
	public String getActualExpression(final String actualProperty) {
	    return YEAR.name() + "([" + actualProperty + "]) * 10000 + " + //
		    MONTH.name() + "([" + actualProperty + "]) * 100 + " + name() + "([" + actualProperty + "])";
	}
    },
    MONTH {
	@Override
	public String toString() {
	    return "Month";
	}

	@Override
	public String getActualExpression(final String actualProperty) {
	    return YEAR.name() + "([" + actualProperty + "]) * 100 + " + name() + "([" + actualProperty + "])";
	}
    },
    YEAR {
	@Override
	public String toString() {
	    return "Year";
	}

	@Override
	public String getActualExpression(final String actualProperty) {
	    return name() + "([" + actualProperty + "])";
	}
    };

    public abstract String getActualExpression(String actualProperty);
}
