package ua.com.fielden.platform.web.centre.api.actions;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;

public interface IEntityActionBuilder0<T extends AbstractEntity<?>> {
    IEntityActionBuilder1<T> withContext(final CentreContextConfig contextConfig);
}