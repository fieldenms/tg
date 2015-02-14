package ua.com.fielden.platform.web.master.api.actions.property;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.master.api.helpers.IAlso;

public interface IPropertyActionConfig6<T extends AbstractEntity<?>> extends IAlso<T> {
    IPropertyActionConfig7<T> longDesc(final String longDesc);
}
