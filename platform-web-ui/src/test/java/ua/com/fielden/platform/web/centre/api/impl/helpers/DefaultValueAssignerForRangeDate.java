package ua.com.fielden.platform.web.centre.api.impl.helpers;

import java.util.Optional;

import ua.com.fielden.platform.sample.domain.TgWorkOrder;
import ua.com.fielden.platform.web.centre.CentreContext;
import ua.com.fielden.platform.web.centre.api.crit.defaults.assigners.IValueAssigner;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.RangeCritDateValueMnemonic;


/**
 * A stub implementation for a default value assigner for range-valued criteria of type date.
 *
 * @author TG Team
 *
 */
public class DefaultValueAssignerForRangeDate implements IValueAssigner<RangeCritDateValueMnemonic, TgWorkOrder> {

    @Override
    public Optional<RangeCritDateValueMnemonic> getValue(final CentreContext<TgWorkOrder, ?> entity, final String name) {
        return null;
    }

}
