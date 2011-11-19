package ua.com.fielden.platform.equery;

import static ua.com.fielden.platform.equery.HqlDateFunctions.DAY;
import static ua.com.fielden.platform.equery.HqlDateFunctions.MONTH;
import static ua.com.fielden.platform.equery.HqlDateFunctions.YEAR;

public enum Rdbms {
    H2 {
	@Override
	public String getDayExpression(final String property) {
	    return MSSQL.getDayExpression(property);
	}

	@Override
	public String getMonthExpression(final String property) {
	    return MSSQL.getMonthExpression(property);
	}

	@Override
	public String getYearExpression(final String property) {
	    return MSSQL.getYearExpression(property);
	}

	@Override
	public String getLike() {
	    return "LIKE";
	}
    },
    MYSQL {
	@Override
	public String getDayExpression(final String property) {
	    return MSSQL.getDayExpression(property);
	}

	@Override
	public String getMonthExpression(final String property) {
	    return MSSQL.getMonthExpression(property);
	}

	@Override
	public String getYearExpression(final String property) {
	    return MSSQL.getYearExpression(property);
	}

	@Override
	public String getLike() {
	    return "LIKE";
	}
    },
    MSSQL {
	@Override
	public String getDayExpression(final String property) {
	    return YEAR.name() + "([" + property + "]) * 10000 + " + //
	    	    MONTH.name() + "([" + property + "]) * 100 + " + DAY.name() + "([" + property + "])";
	}

	@Override
	public String getMonthExpression(final String property) {
	    return YEAR.name() + "([" + property + "]) * 100 + " + MONTH.name() + "([" + property + "])";
	}

	@Override
	public String getYearExpression(final String property) {
	    return YEAR.name() + "([" + property + "])";
	}

	@Override
	public String getLike() {
	    return "LIKE";
	}
    },
    POSTGRESQL {
	@Override
	public String getDayExpression(final String property) {
	    return "cast(EXTRACT(" + YEAR.name() + " FROM " + "[" + property + "]) * 10000 + " + //
	    "EXTRACT(" + MONTH.name() + " FROM " + "[" + property + "]) * 100 + " + "EXTRACT(" + DAY.name() + " FROM " + "[" + property + "]) as int)";
	}

	@Override
	public String getMonthExpression(final String property) {
	    return "cast(EXTRACT(" + YEAR.name() + " FROM " + "[" + property + "]) * 100 + " + //
	    "EXTRACT(" + MONTH.name() + " FROM " + "[" + property + "]) as int)";
	}

	@Override
	public String getYearExpression(final String property) {
	    return "cast(EXTRACT(" + YEAR.name() + " FROM " + "[" + property + "]) as int)";
	}

	@Override
	public String getLike() {
	    return "ILIKE";
	}
    };

    public abstract String getDayExpression(final String property);
    public abstract String getMonthExpression(final String property);
    public abstract String getYearExpression(final String property);
    public abstract String getLike();

    public static Rdbms rdbms;
}
