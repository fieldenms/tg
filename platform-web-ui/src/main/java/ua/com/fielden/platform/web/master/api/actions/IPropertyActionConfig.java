package ua.com.fielden.platform.web.master.api.actions;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.master.api.actions.property.IPropertyActionConfig0;

/**
 * A top level contract for declaring a single property action that is associated with a specific property and its widget.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IPropertyActionConfig<T extends AbstractEntity<?>> {
    IPropertyActionConfig0<T> withAction(final Class<? extends AbstractEntity<?>> functionalEntity);
}
