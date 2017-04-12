package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.dao.IEntityProducer;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

import com.google.inject.Inject;

/**
 * A producer for new instances of entity {@link TgFunctionalEntityWithCentreContext}.
 *
 * @author TG Team
 *
 */
public class TgFunctionalEntityWithCentreContextProducer extends DefaultEntityProducerWithContext<TgFunctionalEntityWithCentreContext> implements IEntityProducer<TgFunctionalEntityWithCentreContext> {

    @Inject
    public TgFunctionalEntityWithCentreContextProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, TgFunctionalEntityWithCentreContext.class, companionFinder);
    }

    @Override
    protected TgFunctionalEntityWithCentreContext provideDefaultValues(final TgFunctionalEntityWithCentreContext entity) {
        if (getContext() != null) {
            final String contextDependentValue = "" + getContext().getSelectedEntities().size() + (getContext().getSelectionCrit() != null ? " && crit" : " no crit");
            entity.setValueToInsert(contextDependentValue);
        }
        entity.setWithBrackets(true);
        return entity;
    }
}