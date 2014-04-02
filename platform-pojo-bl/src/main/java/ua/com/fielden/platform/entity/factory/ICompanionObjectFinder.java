package ua.com.fielden.platform.entity.factory;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract for providing implementation for default entity controller.
 * 
 * @author TG Team
 * 
 */
public interface ICompanionObjectFinder {
    <T extends IEntityDao<E>, E extends AbstractEntity<?>> T find(final Class<E> type);
}
