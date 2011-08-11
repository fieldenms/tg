/**
 *
 */
package ua.com.fielden.platform.swing.review;

/**
 * Enumeration holding values for ordering direction with some convenience methods
 * 
 * @author Yura
 */
public enum Ordering {

    ASCENDING {
	@Override
	public Ordering next() {
	    return DESCENDING;
	}

	@Override
	public String hqlSuffix() {
	    return "asc";
	}

	@Override
	public boolean ignore() {
	    return false;
	}
    },
    DESCENDING {
	@Override
	public Ordering next() {
	    return NONE;
	}

	@Override
	public String hqlSuffix() {
	    return "desc";
	}

	@Override
	public boolean ignore() {
	    return false;
	}
    },
    /**
     * Value representing absence of ordering condition
     */
    NONE {
	@Override
	public Ordering next() {
	    return ASCENDING;
	}

	@Override
	public String hqlSuffix() {
	    return "";
	}

	@Override
	public boolean ignore() {
	    return true;
	}
    };

    /**
     * Returns next {@link Ordering} value. The cycle looks like {@link #NONE} -> {@link #ASCENDING} -> {@link #DESCENDING} -> {@link #NONE}
     */
    public abstract Ordering next();

    /**
     * Returns "asc", "desc" or "" HQL or SQLsuffix depending on ordering direction
     */
    public abstract String hqlSuffix();

    /**
     * Convenience method returning true if this is {@link #NONE} value and false otherwise
     */
    public abstract boolean ignore();

}
