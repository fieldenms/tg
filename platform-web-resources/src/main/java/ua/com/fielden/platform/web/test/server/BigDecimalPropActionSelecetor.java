package ua.com.fielden.platform.web.test.server;

import static java.util.Optional.ofNullable;

import java.math.BigDecimal;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.actions.multi.IEntityMultiActionSelector;

public class BigDecimalPropActionSelecetor implements IEntityMultiActionSelector {

    @Override
    public int getActionFor(final AbstractEntity<?> entity) {
        final int intProp = ofNullable(entity.<BigDecimal>get("bigDecimalProp")).orElse(BigDecimal.ZERO).intValue();
        if (intProp < 13) {
            return 0;
        } else if (intProp < 25) {
            return 1;
        } else {
            return 2;
        }
    }

}
