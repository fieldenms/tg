package ua.com.fielden.platform.entity;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

public class EntityExportActionProducer extends DefaultEntityProducerWithContext<EntityExportAction> {

    @Inject
    public EntityExportActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, EntityExportAction.class, companionFinder);
    }

    @Override
    protected EntityExportAction provideDefaultValues(final EntityExportAction entity) {
        if (getContext() != null) {
            entity.setCentreContextHolder(getContext().getSelectionCrit().centreContextHolder());
            
            final List<AbstractEntity<?>> selectedEntities = getContext().getSelectedEntities();
            if (selectedEntities.size() > 0) {
                final Set<Long> selectedEntityIds = new LinkedHashSet<Long>();
                selectedEntities.forEach(selectedEntity -> selectedEntityIds.add(selectedEntity.getId()));
                entity.setSelectedEntityIds(selectedEntityIds);
            }
        }
        return entity;
    }
}
