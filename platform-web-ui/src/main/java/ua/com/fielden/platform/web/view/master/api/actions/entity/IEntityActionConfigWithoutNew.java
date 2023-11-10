package ua.com.fielden.platform.web.view.master.api.actions.entity;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 *
 * @author TG Team
 *
 * @param <T>
 *
 * A contract that allows to exclude NEW option from action (save/cancel)
 */
public interface IEntityActionConfigWithoutNew<T extends AbstractEntity<?>> extends IEntityActionConfigWithoutClose<T> {

    /**
     * Excludes NEW option from action (save/cancel)
     *
     * @return
     */
    IEntityActionConfigWithoutClose<T> excludeNew();
}
