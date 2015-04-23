package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.DefaultEntityProducer;
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
public class TgFunctionalEntityWithCentreContextProducer extends DefaultEntityProducer<TgFunctionalEntityWithCentreContext> implements IEntityProducer<TgFunctionalEntityWithCentreContext> {
    private final ITgFunctionalEntityWithCentreContext companion;

    @Inject
    public TgFunctionalEntityWithCentreContextProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder, final ITgFunctionalEntityWithCentreContext companion) {
        super(factory, TgFunctionalEntityWithCentreContext.class, companionFinder);
        this.companion = companion;
    }

    @Override
    protected TgFunctionalEntityWithCentreContext provideDefaultValues(final TgFunctionalEntityWithCentreContext entity) {
        entity.setKey("ANY");
        if (getCentreContext() != null) {
            // entity.setContext(getCentreContext());

            final String contextDependentValue = "" + getCentreContext().getSelectedEntities().size() + (getCentreContext().getSelectionCrit() != null ? " && crit" : " no crit");
            entity.setValueToInsert(contextDependentValue);

            // entity.setContext(null);
        }

        // final IFetchProvider<TgFunctionalEntityWithCentreContext> fetchStrategy = companion.getFetchProvider();
        entity.setWithBrackets(true);
        return entity;
    }
}