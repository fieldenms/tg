package ua.com.fielden.platform.web.centre.api.front_actions;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.top_level_actions.ICentreTopLevelActionsWithCrit;

/**
 * Allows one to add front actions or skip front actions and add top actions.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IFrontWithTopActions<T extends AbstractEntity<?>> extends IFrontActions<T>, ICentreTopLevelActionsWithCrit<T>{

}
