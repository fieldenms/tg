package ua.com.fielden.platform.web.centre.api.impl.helpers;

import java.util.Optional;

import ua.com.fielden.platform.sample.domain.TgWorkOrder;
import ua.com.fielden.platform.web.centre.CentreContext;
import ua.com.fielden.platform.web.centre.api.crit.defaults.assigners.IValueAssigner;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.SingleCritDateValueMnemonic;


/**
 * A stub implementation for a default value assigner for single-valued criteria of type date.
 *
 * @author TG Team
 *
 */
public class DefaultValueAssignerForSingleDate implements IValueAssigner<SingleCritDateValueMnemonic, TgWorkOrder> {

    @Override
    public Optional<SingleCritDateValueMnemonic> getValue(final CentreContext<TgWorkOrder, ?> entity, final String name) {
        return null;
    }


}
