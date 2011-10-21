package ua.com.fielden.platform.equery;

public enum HqlDateFunctions {

    DAY {
	@Override
	public String toString() {
	    return "Day";
	}

	@Override
	public String getActualExpression(final String actualProperty) {
	    return "cast(EXTRACT(" + YEAR.name() + " FROM " + "[" + actualProperty + "]) * 10000 + " + //
	    "EXTRACT(" + MONTH.name() + " FROM " + "[" + actualProperty + "]) * 100 + " + "EXTRACT(" + name() + " FROM " + "[" + actualProperty + "]) as int)";

	    //	    return YEAR.name() + "([" + actualProperty + "]) * 10000 + " + //
	    //	    MONTH.name() + "([" + actualProperty + "]) * 100 + " + name() + "([" + actualProperty + "])";
	}
    },
    MONTH {
	@Override
	public String toString() {
	    return "Month";
	}

	@Override
	public String getActualExpression(final String actualProperty) {
	    return "cast(EXTRACT(" + YEAR.name() + " FROM " + "[" + actualProperty + "]) * 100 + " + //
	    "EXTRACT(" + name() + " FROM " + "[" + actualProperty + "]) as int)";
	    //	    return YEAR.name() + "([" + actualProperty + "]) * 100 + " + name() + "([" + actualProperty + "])";
	}
    },
    YEAR {
	@Override
	public String toString() {
	    return "Year";
	}

	@Override
	public String getActualExpression(final String actualProperty) {
	    return "cast(EXTRACT(" + name() + " FROM " + "[" + actualProperty + "]) as int)";
	    //	    return name() + "([" + actualProperty + "])";
	}
    };

    public abstract String getActualExpression(String actualProperty);
}
