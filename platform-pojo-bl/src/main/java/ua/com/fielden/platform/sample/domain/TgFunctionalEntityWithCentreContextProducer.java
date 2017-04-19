package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.dao.IEntityProducer;
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
            
            final Set<Long> selectedEntityIds = new LinkedHashSet<Long>();
            getContext().getSelectedEntities().forEach(selectedEntity -> selectedEntityIds.add(selectedEntity.getId()));
            entity.setSelectedEntityIds(selectedEntityIds);
            
            final String userParam = getContext().getSelectionCrit() != null ? getContext().getSelectionCrit().get("tgPersistentEntityWithProperties_userParam.key") : "UNKNOWN_USER";
            entity.setUserParam(userParam);
        }
        entity.setWithBrackets(true);
        return entity;
    }
}