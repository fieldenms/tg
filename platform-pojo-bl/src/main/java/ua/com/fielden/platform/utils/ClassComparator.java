package ua.com.fielden.platform.utils;

import java.util.Comparator;

/**
 * This is utility class that implements {@link Comparator} for instances of {@link Class} type.
 * 
 * @author TG Team
 *
 */
public class ClassComparator implements Comparator<Class<?>> {

    @Override
    public int compare(final Class<?> thisClass, final Class<?> thatClass) {
	final String thisValue = thisClass != null ? thisClass.getName() : null;
	final String thatValue = thatClass != null ? thatClass.getName() : null;
	if (thisValue == null && thatValue == null) {
	    return 0;
	}
	if (thisValue != null) {
	    return thisValue.compareTo(thatValue);
	}
	return -1; // if thisValue is null than it is smaller than thatValue
    }
}
