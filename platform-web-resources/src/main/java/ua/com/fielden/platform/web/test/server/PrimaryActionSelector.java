package ua.com.fielden.platform.web.test.server;

import static java.util.Optional.ofNullable;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.actions.multi.IEntityMultiActionSelector;

/**
 * Example of action selector to use in test application.
 *
 * @author TG Team
 *
 */
public class PrimaryActionSelector implements IEntityMultiActionSelector {

    @Override
    public int getActionFor(final AbstractEntity<?> entity) {
        final int intProp = ofNullable(entity.<Integer>get("integerProp")).orElse(0);
        if (intProp < 13) {
            return 0;
        } else if (intProp < 25) {
            return 1;
        } else {
            return 2;
        }
    }

}
