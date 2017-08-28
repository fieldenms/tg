package ua.com.fielden.platform.entity;

import java.util.List;
import java.util.stream.Collectors;

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
    protected EntityExportAction provideDefaultValues(final EntityExportAction entity) {
        if (selectionCritNotEmpty()) {
            entity.setCentreContextHolder(selectionCrit().centreContextHolder());
            
            if (selectedEntitiesNotEmpty()) {
                final List<AbstractEntity<?>> selectedEntities = selectedEntities();
                entity.setSelectedEntityIds(
                    selectedEntities.stream()
                    .map(selectedEntity -> selectedEntity.getId())
                    .collect(Collectors.toSet())
                );
            }
        }
        return entity;
    }
}
