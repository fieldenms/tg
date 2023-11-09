package ua.com.fielden.platform.web.view.master.api.actions.entity;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IEntityActionConfigWithoutClose<T extends AbstractEntity<?>> extends IEntityActionConfig0<T>{

    IEntityActionConfig0<T> excludeClose();
}
