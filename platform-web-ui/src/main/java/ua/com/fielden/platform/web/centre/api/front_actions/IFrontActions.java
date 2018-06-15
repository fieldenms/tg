package ua.com.fielden.platform.web.centre.api.front_actions;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;

public interface IFrontActions<T extends AbstractEntity<?>>{

    IAlsoFrontActions<T> addFrontAction(final EntityActionConfig actionConfig);
}
