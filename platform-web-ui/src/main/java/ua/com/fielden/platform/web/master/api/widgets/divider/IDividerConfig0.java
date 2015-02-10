package ua.com.fielden.platform.web.master.api.widgets.divider;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.master.api.helpers.IAlso;
import ua.com.fielden.platform.web.master.api.helpers.ILayoutConfig;

public interface IDividerConfig0<T extends AbstractEntity<?>> extends IAlso<T>, ILayoutConfig {
    IDividerConfig1<T> atLevel1();
    IDividerConfig1<T> atLevel2();
}
