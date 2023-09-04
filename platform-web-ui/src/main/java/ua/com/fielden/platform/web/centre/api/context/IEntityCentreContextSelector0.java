package ua.com.fielden.platform.web.centre.api.context;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IEntityCentreContextSelector0<T extends AbstractEntity<?>> extends IEntityCentreContextSelector6<T> {
    IEntityCentreContextSelector1<T> withSelectionCrit();
    IEntityCentreContextSelector2<T> withMasterEntity();
}
