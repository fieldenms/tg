package ua.com.fielden.platform.expression.type;

/**
 * Year literal such as 10y.
 * 
 * @author TG Team
 * 
 */
public class Year extends AbstractDateLiteral {

    public Year(final int value) {
        super(value, DateLiteral.YEAR);
    }

}
