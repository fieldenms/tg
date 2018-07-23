package ua.com.fielden.platform.web.view.master.api.actions.entity;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.actions.IEntityActionConfig;
import ua.com.fielden.platform.web.view.master.api.helpers.IActionBarLayoutConfig0;

/**
 * A contract that allows one to specify whether this action doesn't close dialog after execution (by default save and cancel actions close diaog after execution)
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
    IEntityActionConfig8<T> dontCloseAfterExecution();
}
