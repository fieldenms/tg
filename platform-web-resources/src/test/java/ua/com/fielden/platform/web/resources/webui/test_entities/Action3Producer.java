package ua.com.fielden.platform.web.resources.webui.test_entities;

import jakarta.inject.Inject;
import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

public class Action3Producer extends DefaultEntityProducerWithContext<Action3> {

    @Inject
    public Action3Producer(
            final EntityFactory factory,
            final ICompanionObjectFinder companionFinder)
    {
        super(factory, Action3.class, companionFinder);
    }

    @Override
    protected Action3 provideDefaultValues(final Action3 entity) {
        if (contextNotEmpty()) {
            entity.setSelectedIds(selectedEntityIds());
            entity.setComputedString((String) computation().map(f -> f.apply(null, null)).orElse(null));
            entity.setMasterEntity(masterEntity());
            entity.setChosenProperty(chosenProperty());
        }

        return super.provideDefaultValues(entity);
    }

}
