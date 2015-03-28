package ua.com.fielden.platform.web.centre.api.context;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IEntityCentreContextSelector4<T extends AbstractEntity<?>> extends IEntityCentreContextSelectorDone<T> {

    IEntityCentreContextSelector1<T> withCurrentEntity();
    IEntityCentreContextSelector1<T> withSelectedEntities();
}
