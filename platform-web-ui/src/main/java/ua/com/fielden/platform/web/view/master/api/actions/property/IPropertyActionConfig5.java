package ua.com.fielden.platform.web.view.master.api.actions.property;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IPropertyActionConfig5<T extends AbstractEntity<?>> extends IPropertyActionConfig6<T> {
    IPropertyActionConfig6<T> shortDesc(final String shortDesc);
}
