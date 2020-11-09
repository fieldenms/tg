package ua.com.fielden.platform.web.test.server;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.actions.multi.IEntityMultiActionSelector;

public class PrimaryActionSelector implements IEntityMultiActionSelector {

    @Override
    public int getActionFor(final AbstractEntity<?> entity) {
        final int intProp = ((Integer)entity.get("integerProp")).intValue();
        if (intProp < 13) {
            return 0;
        } else if (intProp < 25) {
            return 1;
        } else {
            return 2;
        }
    }

}
