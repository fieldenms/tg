package ua.com.fielden.platform.web.centre.api.context;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IEntityCentreContextSelector3<T extends AbstractEntity<?>> extends IEntityCentreContextSelectorFunction<T> {
    IEntityCentreContextSelector1<T> withCurrentEntity();
    IEntityCentreContextSelector1<T> withSelectedEntities();

    IEntityCentreContextSelector4<T> withMasterEntity();
}
