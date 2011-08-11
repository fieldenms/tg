package ua.com.fielden.platform.equery;

/**
 * Describes query ordering directions.
 *
 * @author TG Team
 *
 */
public enum Ordering {
    ASC, DESC;

    public String getValue() {
	return toString();
    }
}
