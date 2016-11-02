package ua.com.fielden.platform.data.generator;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.security.user.User;

/**
 * This is just a type-safety marker that highlights the need to have property <code>createdBy:User</code> for entities that are to be used for data generation.
 * 
 * @author TG Team
 *
 * @param <T>
 */
public interface WithCreatedByUser<T extends AbstractEntity<?>> {
    User getCreatedBy();
}
