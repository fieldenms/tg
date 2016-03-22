package ua.com.fielden.platform.web.centre.api.impl.helpers;

import java.util.Optional;

import ua.com.fielden.platform.sample.domain.TgWorkOrder;
import ua.com.fielden.platform.web.centre.CentreContext;
import ua.com.fielden.platform.web.centre.api.crit.defaults.assigners.IValueAssigner;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.MultiCritStringValueMnemonic;


/**
 * A stub implementation for a default value assigner for multi-valued criteria of type string.
 *
 * @author TG Team
 *
 */
public class DefaultValueAssignerForMultiString implements IValueAssigner<MultiCritStringValueMnemonic, TgWorkOrder> {

    @Override
    public Optional<MultiCritStringValueMnemonic> getValue(final CentreContext<TgWorkOrder, ?> entity, final String name) {
        return null;
    }


}
