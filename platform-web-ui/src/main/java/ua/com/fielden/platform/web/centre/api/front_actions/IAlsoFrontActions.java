package ua.com.fielden.platform.web.centre.api.front_actions;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.top_level_actions.ICentreTopLevelActions;

/**
 * Contract that allows one to add another front actions otherwise switch to other configurations
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IAlsoFrontActions<T extends AbstractEntity<?>> extends ICentreTopLevelActions<T>{

    /**
     * Meaningful method call that allows to augment other actions.
     *
     * @return
     */
    IFrontActions<T> also();
}
