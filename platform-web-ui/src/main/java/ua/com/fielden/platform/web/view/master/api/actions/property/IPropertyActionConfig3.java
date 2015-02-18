package ua.com.fielden.platform.web.view.master.api.actions.property;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.actions.EnabledState;

public interface IPropertyActionConfig3<T extends AbstractEntity<?>> {
    IPropertyActionConfig4<T> enabledWhen(final EnabledState state);
}
