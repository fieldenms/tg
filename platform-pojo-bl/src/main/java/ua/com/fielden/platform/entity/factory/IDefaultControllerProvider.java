package ua.com.fielden.platform.entity.factory;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract for providing implementation for default entity controller.
 *
 * @author TG Team
 *
 */
public interface IDefaultControllerProvider {
    <T extends IEntityDao<E>, E extends AbstractEntity> T findController(Class<E> type);
}
