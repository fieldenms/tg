package ua.com.fielden.platform.web.view.master.api.widgets.divider;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.helpers.IAlso;

public interface IDividerConfig0<T extends AbstractEntity<?>> extends IAlso<T> {
    IDividerConfig1<T> atLevel1();
    IDividerConfig1<T> atLevel2();
}
