package ua.com.fielden.platform.web.centre.api.context;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IEntityCentreContextWithChosenEntitySelector<T extends AbstractEntity<?>> extends IEntityCentreContextSelector<T> {

    IEntityCentreContextSelector<T> withChosenEntity();
}
