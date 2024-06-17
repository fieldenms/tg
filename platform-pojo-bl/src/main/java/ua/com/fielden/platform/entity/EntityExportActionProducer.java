package ua.com.fielden.platform.entity;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

/**
 * Producer of {@link EntityExportAction} that initialises entity IDs from context's selected entities.
 * 
 * @author TG Team
 *
 */
public class EntityExportActionProducer extends DefaultEntityProducerWithContext<EntityExportAction> {

    @Inject
    public EntityExportActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, EntityExportAction.class, companionFinder);
    }

    @Override
    protected EntityExportAction provideDefaultValues(final EntityExportAction action) {
        if (selectionCritNotEmpty()) {
            action.setCentreContextHolder(selectionCrit().centreContextHolder());
            if (selectedEntitiesNotEmpty()) {
                action.setSelectedEntityIds(selectedEntityIds());
                action.setExportSelected(true);
            }
        }
        return action;
    }

}
