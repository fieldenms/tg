package ua.com.fielden.platform.web.centre.api.front_actions;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.top_level_actions.ICentreTopLevelActionsWithCrit;

public interface IFrontWithTopActions<T extends AbstractEntity<?>> extends IFrontActions<T>, ICentreTopLevelActionsWithCrit<T>{

}
