package ua.com.fielden.platform.web.centre.api.context;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IEntityCentreContexSelector1<T extends AbstractEntity<?>> extends IEntityCentreContexSelector2<T> {
    IEntityCentreContexSelector2<T> withMasterEntity();
}
