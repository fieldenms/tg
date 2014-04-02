package ua.com.fielden.platform.expression.type;

/**
 * Day literal such as 23d.
 * 
 * @author TG Team
 * 
 */
public class Day extends AbstractDateLiteral {

    public Day(final int value) {
        super(value, DateLiteral.DAY);
    }

}
