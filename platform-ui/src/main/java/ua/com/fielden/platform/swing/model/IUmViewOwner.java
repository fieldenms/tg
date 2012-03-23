package ua.com.fielden.platform.swing.model;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract that can be implemented and passed into UI model in order to get some custom logic invoked upon entity change. The name "view owner" alludes to the fact that in many
 * cases such an update makes sense on the object responsible for creation of the associative view. In most cases such view is owned (has a non weak reference to it) by the owner.
 * For example, master frames are invoked from entity centres -- here centre would be an invoker, which needs to be updated when entity changes on the master.
 *
 * @author TG Team
 *
 */
public interface IUmViewOwner {
    /**
     * If required, the implementation should ensure its execution on EDT.
     *
     * @param entity
     */
    <T extends AbstractEntity<?>> void notifyEntityChange(final T entity);

}
