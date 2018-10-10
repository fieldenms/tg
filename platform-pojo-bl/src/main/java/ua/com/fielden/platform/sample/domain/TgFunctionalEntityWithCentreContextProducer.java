package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

/**
 * A producer for new instances of entity {@link TgFunctionalEntityWithCentreContext}.
 *
 * @author TG Team
 *
 */
public class TgFunctionalEntityWithCentreContextProducer extends DefaultEntityProducerWithContext<TgFunctionalEntityWithCentreContext> {

    @Inject
    public TgFunctionalEntityWithCentreContextProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, TgFunctionalEntityWithCentreContext.class, companionFinder);
    }

    @Override
    protected TgFunctionalEntityWithCentreContext provideDefaultValues(final TgFunctionalEntityWithCentreContext entity) {
        if (contextNotEmpty()) {
            entity.setValueToInsert("" + selectedEntities().size() + (selectionCritNotEmpty() ? " && crit" : " no crit"));
            entity.setSelectedEntityIds(selectedEntityIds());
            entity.setUserParam(selectionCritNotEmpty() ? selectionCrit().get("tgPersistentEntityWithProperties_userParam.key") : "UNKNOWN_USER");
        }
        entity.setWithBrackets(true);
        return entity;
    }
}