package ua.com.fielden.platform.web.centre.api.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.IEntityCentreBuilder;
import ua.com.fielden.platform.web.centre.api.top_level_actions.ICentreTopLevelActions;

public class EntityCentreBuilder<T extends AbstractEntity<?>> implements IEntityCentreBuilder<T> {

    @Override
    public ICentreTopLevelActions<T> forEntity(final Class<T> type) {
        // TODO Auto-generated method stub
        return null;
    }

}
