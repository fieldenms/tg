package ua.com.fielden.platform.web.centre.api.front_actions;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.top_level_actions.ICentreTopLevelActions;

public interface IAlsoFrontActions<T extends AbstractEntity<?>> extends ICentreTopLevelActions<T>{

    IFrontActions<T> also();
}
