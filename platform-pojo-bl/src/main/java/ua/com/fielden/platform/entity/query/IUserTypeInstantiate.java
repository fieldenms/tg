package ua.com.fielden.platform.entity.query;

import ua.com.fielden.platform.entity.factory.EntityFactory;

/**
 * Interface for enabling hibernate-free instantiation of instances of hibernate mapped user types.
 * 
 * @author TG Team
 * 
 */
public interface IUserTypeInstantiate {
    /**
     * Should provide object instantiation based on the passed argument.
     * 
     * @param argument
     * @return
     */
    Object instantiate(final Object argument, final EntityFactory factory);

}
