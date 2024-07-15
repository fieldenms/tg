package ua.com.fielden.platform.web.centre.api.actions;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IEntityActionBuilder8<T extends AbstractEntity<?>> extends IEntityActionBuilder8a<T> {
    /** If used then no auto-refresh of the parent centre would occur upon entity save. */
    IEntityActionBuilder8a<T> withNoParentCentreRefresh();
}
