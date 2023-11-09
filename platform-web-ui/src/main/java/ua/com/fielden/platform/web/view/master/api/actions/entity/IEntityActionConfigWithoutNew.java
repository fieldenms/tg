package ua.com.fielden.platform.web.view.master.api.actions.entity;

import ua.com.fielden.platform.entity.AbstractEntity;

public interface IEntityActionConfigWithoutNew<T extends AbstractEntity<?>> extends IEntityActionConfigWithoutClose<T> {

    IEntityActionConfigWithoutClose<T> excludeNew();
}
