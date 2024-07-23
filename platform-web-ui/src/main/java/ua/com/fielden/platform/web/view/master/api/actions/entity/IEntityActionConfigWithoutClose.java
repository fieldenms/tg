package ua.com.fielden.platform.web.view.master.api.actions.entity;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
*
* @author TG Team
*
* @param <T>
*
* A contract that allows to exclude CLOSE option from action (save/cancel)
*/
public interface IEntityActionConfigWithoutClose<T extends AbstractEntity<?>> extends IEntityActionConfig0<T>{

    /**
     * Excludes CLOSE option from action (save/cancel)
     *
     * @return
     */
    IEntityActionConfig0<T> excludeClose();
}
