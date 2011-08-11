package ua.com.fielden.platform.entity.query;

import java.util.Map;

/**
 * Interface for enabling hibernate-free instantiation of instances of hibernate mapped custom user types.
 * @author TG Team
 *
 */
public interface ICompositeUserTypeInstantiate {
    /**
     * Should provide object instantiation based on the passed by name constructor arguments.
     * @param arguments
     * @return
     */
    Object instantiate(Map<String, Object> arguments);
}
