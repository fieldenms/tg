package ua.com.fielden.platform.web.centre.api.context;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IEntityCentreContextSelector0<T extends AbstractEntity<?>> extends IEntityCentreContextSelectorDone<T> {
    IEntityCentreContextSelector1<T> withSelectionCrit();
    IEntityCentreContextSelector2<T> withMasterEntity();
}
