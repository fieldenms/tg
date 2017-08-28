package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

import java.util.LinkedHashSet;
import java.util.Set;

import com.google.inject.Inject;

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
            final String contextDependentValue = "" + selectedEntities().size() + (selectionCritNotEmpty() ? " && crit" : " no crit");
            entity.setValueToInsert(contextDependentValue);
            
            final Set<Long> selectedEntityIds = new LinkedHashSet<Long>();
            selectedEntities().forEach(selectedEntity -> selectedEntityIds.add(selectedEntity.getId()));
            entity.setSelectedEntityIds(selectedEntityIds);
            
            final String userParam = selectionCritNotEmpty() ? selectionCrit().get("tgPersistentEntityWithProperties_userParam.key") : "UNKNOWN_USER";
            entity.setUserParam(userParam);
        }
        entity.setWithBrackets(true);
        return entity;
    }
}