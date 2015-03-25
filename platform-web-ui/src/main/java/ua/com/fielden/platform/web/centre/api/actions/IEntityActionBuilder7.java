package ua.com.fielden.platform.web.centre.api.actions;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IEntityActionBuilder7<T extends AbstractEntity<?>> extends IEntityActionBuilder<T> {
    EntityActionConfig build();
}
