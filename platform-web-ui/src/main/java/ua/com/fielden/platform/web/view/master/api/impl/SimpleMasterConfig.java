package ua.com.fielden.platform.web.view.master.api.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.view.master.api.ISimpleMasterConfig;
import ua.com.fielden.platform.web.view.master.api.helpers.IPropertySelector;

public class SimpleMasterConfig implements ISimpleMasterConfig {

    @Override
    public <T extends AbstractEntity<?>> IPropertySelector<T> forEntity(final Class<T> type) {
        return new SimpleMaster<>(type);
    }

}
