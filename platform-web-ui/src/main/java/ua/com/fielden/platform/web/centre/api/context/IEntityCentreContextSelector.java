package ua.com.fielden.platform.web.centre.api.context;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IEntityCentreContextSelector<T extends AbstractEntity<?>> {
    IEntityCentreContextSelector0<T> withCurrentEntity();
    IEntityCentreContextSelector0<T> withSelectedEntities();

    IEntityCentreContextSelector3<T> withSelectionCrit();
    IEntityCentreContextSelector5<T> withMasterEntity();
}
