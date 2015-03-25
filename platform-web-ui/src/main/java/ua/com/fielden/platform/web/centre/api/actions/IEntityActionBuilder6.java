package ua.com.fielden.platform.web.centre.api.actions;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IEntityActionBuilder6<T extends AbstractEntity<?>> extends IEntityActionBuilder<T> {
    IEntityActionBuilder7<T> longDesc(final String longDesc);
}