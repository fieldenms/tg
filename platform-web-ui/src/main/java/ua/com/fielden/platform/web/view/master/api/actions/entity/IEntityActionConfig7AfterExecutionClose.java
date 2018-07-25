package ua.com.fielden.platform.web.view.master.api.actions.entity;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.actions.IEntityActionConfig;
import ua.com.fielden.platform.web.view.master.api.helpers.IActionBarLayoutConfig0;

/**
 * An API to provide an action configuration hint that an Entity Master should not be closed as the result of executing the action.
 * <p>
 * In general the decision to close or keep an Entity Master open is governed by special algorithm a the client side that is based on the state and kind of the underlying entity instance.
 * For example, a predefined action {@code SAVE} will always try to close the master upon successful execution.
 * <p>
 * This API provides a way to override such default behaviour.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IEntityActionConfig7AfterExecutionClose <T extends AbstractEntity<?>> extends IEntityActionConfig<T>, IActionBarLayoutConfig0<T> {

    /**
     * Configures action so that it doesn't close dialog after execution.
     *
     * @return
     */
    IEntityActionConfig8<T> keepMasterOpenAfterExecution();
}
