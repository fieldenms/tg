package ua.com.fielden.platform.expression.type;


/**
 * This is abase class for date literals such as 1d, 3m, 34y.
 *
 * @author TG Team
 *
 */
public abstract class AbstractDateLiteral {
    private final int value;
    private final DateLiteral discriminator;

    public AbstractDateLiteral(final int value, final DateLiteral descriminator) {
	this.value = value;
	this.discriminator = descriminator;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
	return value + discriminator.toString();
    }

    @Override
    public int hashCode() {
        return value * 23 + discriminator.hashCode() * 31;
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj) {
	    return true;
	}
	if (!(obj instanceof AbstractDateLiteral)) {
	    return false;
	}

	final AbstractDateLiteral that = (AbstractDateLiteral) obj;

	return value == that.value && discriminator.equals(that.discriminator);

    }
}
