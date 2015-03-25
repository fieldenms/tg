package ua.com.fielden.platform.web.centre.api.context;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IEntityCentreContexSelector0<T extends AbstractEntity<?>> extends IEntityCentreContexSelector1<T> {
    IEntityCentreContexSelector1<T> withSelectionCrit();
}
