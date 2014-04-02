package ua.com.fielden.platform.domaintree;

import java.util.Set;

/**
 * An interface that defines an "entity" with its "root types".
 * 
 * @author TG Team
 * 
 */
public interface IRootTyped {
    /**
     * Returns a set of domain types to be working with. This types will be included by default (see {@link #isExcludedImmutably(Class, String)} contract).
     * 
     * @return
     */
    Set<Class<?>> rootTypes();
}