package ua.com.fielden.platform.web.master.api.actions.property;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.master.api.actions.pre.IPreAction;

public interface IPropertyActionConfig0<T extends AbstractEntity<?>> extends IPropertyActionConfig1<T> {
    IPropertyActionConfig1<T> preAction(final IPreAction preAction);
}
