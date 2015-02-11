package ua.com.fielden.platform.web.master.api.actions.property;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IPropertyActionConfig4<T extends AbstractEntity<?>> extends IPropertyActionConfig5<T> {
    IPropertyActionConfig5<T> useIcon(final String iconName);
}
