package ua.com.fielden.platform.web.view.master.api.actions;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.actions.multi.EntityMultiActionConfig;
import ua.com.fielden.platform.web.view.master.api.helpers.IAlso;

/**
 * A top level contract for declaring a single/multi property action that is associated with a specific property and its widget.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IPropertyActionConfig<T extends AbstractEntity<?>> {

    IAlso<T> withAction(final EntityActionConfig actionConfig);

    IAlso<T> withMultiAction(final EntityMultiActionConfig actionConfig);
}
