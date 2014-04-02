package ua.com.fielden.platform.expression.type;

/**
 * Month literal such as 3m.
 * 
 * @author TG Team
 * 
 */
public class Month extends AbstractDateLiteral {

    public Month(final int value) {
        super(value, DateLiteral.MONTH);
    }

}
