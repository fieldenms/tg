package ua.com.fielden.platform.entity.factory;

import ua.com.fielden.platform.dao2.IEntityDao2;
import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract for providing implementation for default entity controller.
 *
 * @author TG Team
 *
 */
public interface IDefaultControllerProvider2 {
    <T extends IEntityDao2<E>, E extends AbstractEntity<?>> T findController(Class<E> type);
}
