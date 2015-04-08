package ua.com.fielden.platform.web.centre.api.impl.helpers;

import java.math.BigDecimal;
import java.util.Optional;

import ua.com.fielden.platform.sample.domain.TgWorkOrder;
import ua.com.fielden.platform.web.centre.CentreContext;
import ua.com.fielden.platform.web.centre.api.crit.defaults.assigners.IRangeValueAssigner;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.RangeCritOtherValueMnemonic;


/**
 * A stub implementation for a default value assigner for range-valued criteria of type BigDecimal or Money.
 *
 * @author TG Team
 *
 */
public class DefaultValueAssignerForRangeDecimal implements IRangeValueAssigner<RangeCritOtherValueMnemonic<BigDecimal>, TgWorkOrder> {

    @Override
    public Optional<RangeCritOtherValueMnemonic<BigDecimal>> getFromValue(final CentreContext<TgWorkOrder, ?> entity, final String name) {
        return null;
    }

    @Override
    public Optional<RangeCritOtherValueMnemonic<BigDecimal>> getToValue(final CentreContext<TgWorkOrder, ?> entity, final String name) {
        return null;
    }

}
