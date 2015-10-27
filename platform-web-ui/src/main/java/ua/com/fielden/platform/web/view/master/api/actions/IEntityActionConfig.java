package ua.com.fielden.platform.web.view.master.api.actions;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.view.master.api.actions.entity.IEntityActionConfig0;
import ua.com.fielden.platform.web.view.master.api.actions.entity.IEntityActionConfig7;

/**
 *
 * An entry contract an API for specifying an action.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IEntityActionConfig<T extends AbstractEntity<?>> {
    
    IEntityActionConfig7<T> addAction(final EntityActionConfig actionConfig);
    
    IEntityActionConfig0<T> addAction(final String name, final Class<? extends AbstractEntity<?>> functionalEntity);

    IEntityActionConfig0<T> addAction(final MasterActions masterAction);
}
