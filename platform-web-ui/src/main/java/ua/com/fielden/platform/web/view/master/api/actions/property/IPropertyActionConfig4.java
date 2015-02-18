package ua.com.fielden.platform.web.view.master.api.actions.property;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IPropertyActionConfig4<T extends AbstractEntity<?>> extends IPropertyActionConfig5<T> {
    IPropertyActionConfig5<T> icon(final String iconName);
}
