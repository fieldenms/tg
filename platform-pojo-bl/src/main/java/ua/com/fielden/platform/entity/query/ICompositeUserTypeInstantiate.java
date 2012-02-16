package ua.com.fielden.platform.entity.query;

import java.util.Map;

/**
 * Interface for enabling hibernate-free instantiation of instances of hibernate mapped custom user types.
 *
 * @author TG Team
 *
 */
public interface ICompositeUserTypeInstantiate {
    /**
     * Should provide object instantiation based on the passed by name constructor arguments.
     *
     * @param arguments
     * @return
     */
    Object instantiate(Map<String, Object> arguments);

    /**
     * Get the "property names" that may be used in a query.
     *
     * @return an array of "property names"
     */
    public String[] getPropertyNames();

    /**
     * The class returned by <tt>nullSafeGet()</tt>.
     *
     * @return Class
     */
    public Class returnedClass();
}
