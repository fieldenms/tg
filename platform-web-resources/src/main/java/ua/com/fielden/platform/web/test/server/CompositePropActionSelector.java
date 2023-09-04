package ua.com.fielden.platform.web.test.server;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.actions.multi.IEntityMultiActionSelector;

/**
 * Action selector for property action on EGI in test application.
 *
 * @author TG Team
 *
 */
public class CompositePropActionSelector implements IEntityMultiActionSelector {

    @Override
    public int getActionFor(final AbstractEntity<?> entity) {
        if (entity.get("compositeProp") == null) {
            return 0;
        }
        return 1;
    }

}
