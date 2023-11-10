package ua.com.fielden.platform.web.view.master.api.actions;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.view.master.api.actions.entity.IEntityActionConfig0;
import ua.com.fielden.platform.web.view.master.api.actions.entity.IEntityActionConfig5;
import ua.com.fielden.platform.web.view.master.api.actions.entity.IEntityActionConfigWithoutNew;

/**
 *
 * An entry contract an API for specifying an action.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IEntityActionConfig<T extends AbstractEntity<?>> {

    IEntityActionConfig5<T> addAction(final EntityActionConfig actionConfig);

    IEntityActionConfig0<T> addAction(final MasterActions masterAction);

    /**
     * Adds SAVE action that allows to exclude separate options (close and/or new)
     *
     * @return
     */
    IEntityActionConfigWithoutNew<T> addSaveAction();

    /**
     * Adds CANCEL action that allows to exclude separate options (close and/or new)
     *
     * @return
     */
    IEntityActionConfigWithoutNew<T> addCancelAction();
}
