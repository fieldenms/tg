package ua.com.fielden.platform.expression.type;

/**
 * Describes date literal discriminators.
 * 
 * @author TG Team
 * 
 */
public enum DateLiteral {
    DAY("d"), MONTH("m"), YEAR("y");

    public final String discriminator;

    DateLiteral(final String descriminator) {
        this.discriminator = descriminator;
    }

    @Override
    public String toString() {
        return discriminator;
    }
}
