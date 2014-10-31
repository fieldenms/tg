package ua.com.fielden.platform.dao;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * An interface for dynamic instantiation of RAO and DAO implementations.
 *
 * @author TG Team
 *
 */
public interface IDaoFactory {
    <T extends IEntityDao<E>, E extends AbstractEntity<?>> T newDao(final Class<E> entityType);
}
