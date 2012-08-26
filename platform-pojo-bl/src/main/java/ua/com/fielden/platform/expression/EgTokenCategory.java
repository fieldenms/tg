package ua.com.fielden.platform.expression;


/**
 * Enumeration of taken categories used by the expression grammar.
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
    UPPER(16),
    LOWER(17),
    DAY_DIFF(18),
    COMMA(19),
    NAME(20), // property name, which includes dot notation for nested properties
    INT(21),
    DECIMAL(22),
    STRING(23),
    DATE_CONST(24), // here it means xd (for x days), xm (for x month) or xy (for x years)
    SELF(25), // a key word indicating an entity itself, which gets translated to its id property; used four things like COUNT(self) etc.
    ///////////// comparison operators ////////////////////
    LESS(26),
    GREATER(27),
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
    END(38);

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
