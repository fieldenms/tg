package ua.com.fielden.platform.web.centre.api.context;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IEntityCentreContexSelector<T extends AbstractEntity<?>> extends IEntityCentreContexSelector0<T> {
    IEntityCentreContexSelector0<T> withCurrentEntity();
    IEntityCentreContexSelector0<T> withSelectedEntities();
}
