package ua.com.fielden.platform.web.centre.api.context;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IEntityCentreContextSelector4<T extends AbstractEntity<?>> extends IExtendedEntityCentreContextWithFunctionSelector<T> {

    IExtendedEntityCentreContextWithFunctionSelector<T> withCurrentEntity();
    IExtendedEntityCentreContextWithFunctionSelector<T> withSelectedEntities();
}
