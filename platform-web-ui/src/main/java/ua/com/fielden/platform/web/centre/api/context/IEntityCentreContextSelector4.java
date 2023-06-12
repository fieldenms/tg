package ua.com.fielden.platform.web.centre.api.context;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IEntityCentreContextSelector4<T extends AbstractEntity<?>> extends IEntityCentreContextSelector6<T> {

    IEntityCentreContextSelector6<T> withCurrentEntity();
    IEntityCentreContextSelector6<T> withSelectedEntities();
}
