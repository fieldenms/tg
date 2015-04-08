package ua.com.fielden.platform.web.centre.api.impl.helpers;

import java.util.Optional;

import ua.com.fielden.platform.sample.domain.TgWorkOrder;
import ua.com.fielden.platform.web.centre.CentreContext;
import ua.com.fielden.platform.web.centre.api.crit.defaults.assigners.ISingleValueAssigner;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.SingleCritOtherValueMnemonic;


/**
 * A stub implementation for a default value assigner for single-valued criteria of type integer.
 *
 * @author TG Team
 *
 */
public class DefaultValueAssignerForSingleInteger implements ISingleValueAssigner<SingleCritOtherValueMnemonic<Integer>, TgWorkOrder> {

    @Override
    public Optional<SingleCritOtherValueMnemonic<Integer>> getValue(final CentreContext<TgWorkOrder, ?> entity, final String name) {
        return null;
    }

}
