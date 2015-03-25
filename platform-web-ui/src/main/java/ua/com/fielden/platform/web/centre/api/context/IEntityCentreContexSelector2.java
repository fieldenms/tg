package ua.com.fielden.platform.web.centre.api.context;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IEntityCentreContexSelector2<T extends AbstractEntity<?>> {
    CentreContextConfig build();
}
