package ua.com.fielden.platform.web.centre.api.impl.helpers;

import java.util.Optional;

import ua.com.fielden.platform.sample.domain.TgWorkOrder;
import ua.com.fielden.platform.web.centre.CentreContext;
import ua.com.fielden.platform.web.centre.api.crit.defaults.assigners.IRangeValueAssigner;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.RangeCritOtherValueMnemonic;


/**
 * A stub implementation for a default value assigner for range-valued criteria of type integer.
 *
 * @author TG Team
 *
 */
public class DefaultValueAssignerForRangeInteger implements IRangeValueAssigner<RangeCritOtherValueMnemonic<Integer>, TgWorkOrder> {

    @Override
    public Optional<RangeCritOtherValueMnemonic<Integer>> getFromValue(final CentreContext<TgWorkOrder, ?> entity, final String name) {
        return null;
    }

    @Override
    public Optional<RangeCritOtherValueMnemonic<Integer>> getToValue(final CentreContext<TgWorkOrder, ?> entity, final String name) {
        return null;
    }

}
