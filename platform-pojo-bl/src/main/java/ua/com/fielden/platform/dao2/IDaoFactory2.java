package ua.com.fielden.platform.dao2;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * An interface for dynamic instantiation of RAO and DAO implementations.
 *
 * @author TG Team
 *
 */
public interface IDaoFactory2 {
    IEntityDao2<?> newDao(final Class<? extends AbstractEntity<?>> entityType);
}
