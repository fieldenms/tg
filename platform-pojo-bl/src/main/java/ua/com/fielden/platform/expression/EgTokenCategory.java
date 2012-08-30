package ua.com.fielden.platform.expression;


/**
 * Enumeration of token categories used by the expression language grammar.
 *
 * @author TG Team
 *
 */
public enum EgTokenCategory implements ILexemeCategory {
    EOF(0),
    LPAREN(2),
    RPAREN(3),
    PLUS(4),
    MINUS(5),
    MULT(6),
    DIV(7),
    AVG(8),
    SUM(9),
    MIN(10),
    MAX(11),
    COUNT(12),
    DAY(13),
    MONTH(14),
    YEAR(15),
    HOUR(48),
    MINUTE(49),
    SECOND(50),
    UPPER(16),
    LOWER(17),
    DAY_DIFF(18), // TODO remove deprecated function from lexer, parser, AST visitors and unit tests
    COMMA(19),
    NAME(20), // property name, which includes dot notation for nested properties
    INT(21),
    DECIMAL(22),
    STRING(23),
    DATE_CONST(24), // here it means xd (for x days), xm (for x month) or xy (for x years)
    DATE(43), // represents a date literal such as '2001-02-31' or '2012-06-30 23:59'
    SELF(25), // a key word indicating an entity itself, which gets translated to its id property; used four things like COUNT(self) etc.
    NULL(44), // a key word indicating and empty value; should be used as part of comparison operations
    ///////////// comparison operators ////////////////////
    LT(26),
    GT(27),
    LE(28),
    GE(29),
    EQ(30),
    NE(31),
    ////////////// logical operations ////////////////////////
    AND(32),
    OR(33),
    ////////////// case/when operations ////////////////////////
    CASE(34),
    WHEN(35),
    THEN(36),
    ELSE(37),
    END(38),
    ////////////// date difference functions ////////////////////////
    DAYS(39),
    MONTHS(40),
    YEARS(41),
    HOURS(45),
    MINUTES(46),
    SECONDS(47),
    NOW(42);


    public final int index;

    private EgTokenCategory(final int index) {
	this.index = index;
    }

    @Override
    public String getName() {
	return toString();
    }

    @Override
    public int getIndex() {
	return index;
    }

    public static EgTokenCategory byIndex(final int index) {
	for (final EgTokenCategory value : EgTokenCategory.values()) {
	    if (value.index == index) {
		return value;
	    }
	}
	throw new IllegalArgumentException("There is no token category with index " + index);
    }
}
