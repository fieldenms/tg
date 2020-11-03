package ua.com.fielden.platform.web.centre.api.actions.multi;

import ua.com.fielden.platform.entity.AbstractEntity;

public class SingleActionSelector implements IEntityMultiActionSelector {

    @Override
    public int getActionFor(final AbstractEntity<?> entity) {
        return 0;
    }

}
