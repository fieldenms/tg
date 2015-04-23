package ua.com.fielden.platform.sample.domain.definers;

import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.sample.domain.TgFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.web.centre.CentreContext;

/**
 * Definer for {@link TgFunctionalEntityWithCentreContext} context. Assigns context dependent properties for example functional entity.
 *
 * @author TG Team
 *
 */
public class TgFunctionalEntityContextHandler implements IAfterChangeEventHandler<CentreContext<?, ?>> {

    @Override
    public void handle(final MetaProperty<CentreContext<?, ?>> property, final CentreContext<?, ?> context) {
        final TgFunctionalEntityWithCentreContext entity = (TgFunctionalEntityWithCentreContext) property.getEntity();

        final String contextDependentValue = "" + context.getSelectedEntities().size() + (context.getSelectionCrit() != null ? " && crit" : " no crit");
        entity.setValueToInsert(contextDependentValue);
    }

}
