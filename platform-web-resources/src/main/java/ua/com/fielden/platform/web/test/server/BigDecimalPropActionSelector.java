package ua.com.fielden.platform.web.test.server;

import static java.util.Optional.ofNullable;

import java.math.BigDecimal;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.actions.multi.IEntityMultiActionSelector;

/**
 * Action selector bigDecimalProp on the entity master of test application.
 *
 * @author TG Team
 *
 */
public class BigDecimalPropActionSelector implements IEntityMultiActionSelector {

    @Override
    public int getActionFor(final AbstractEntity<?> entity) {
        final int intProp = ofNullable(entity.<BigDecimal>get("bigDecimalProp")).orElse(BigDecimal.ZERO).intValue();
        if (entity.<Boolean>get("completed")) { // used for BindSavedPropertyPostActionSuccess web test in tg-entity-master.html
            return 1;
        } else if (intProp < 13) {
            return 0;
        } else if (intProp < 25) {
            return 1;
        } else {
            return 2;
        }
    }

}
