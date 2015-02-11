package ua.com.fielden.platform.web.master.api.actions.property;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.master.api.helpers.IAlso;
import ua.com.fielden.platform.web.master.api.helpers.ILayoutConfig;

public interface IPropertyActionConfig6<T extends AbstractEntity<?>> extends IAlso<T>, ILayoutConfig {
    IPropertyActionConfig7<T> longDesc(final String longDesc);
}
