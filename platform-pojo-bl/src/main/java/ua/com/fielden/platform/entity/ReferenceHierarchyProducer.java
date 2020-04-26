package ua.com.fielden.platform.entity;

import static java.lang.String.format;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isSyntheticBasedOnPersistentEntityType;

import org.apache.commons.lang.StringUtils;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.reflection.exceptions.ReflectionException;

public class ReferenceHierarchyProducer extends DefaultEntityProducerWithContext<ReferenceHierarchy> {

    @Inject
    public ReferenceHierarchyProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, ReferenceHierarchy.class, companionFinder);
    }

    @Override
    protected ReferenceHierarchy provideDefaultValues(final ReferenceHierarchy entity) {
        if (selectedEntitiesNotEmpty()) {
            final AbstractEntity<?> selectedEntity = selectedEntities().get(0);
            entity.setRefEntityId(selectedEntity.getId());
            // we need to be smart about getting the type as it may be a synthetic entity that represents a persistent entity
            // so we really need to handle this case here
            final Class<? extends AbstractEntity<?>> entityType = selectedEntity.getType();
            if (isPersistedEntityType(entityType)) {
                entity.setRefEntityType(entityType.getName());
            } else if (isSyntheticBasedOnPersistentEntityType(entityType)) {
                entity.setRefEntityType(entityType.getSuperclass().getName());
            } else {
                throw new ReflectionException(format("Unsupported entity type [%s] for Reference Hiearchy.", entityType.getSimpleName()));
            }
            entity.setTitle(selectedEntity.getKey() + (StringUtils.isEmpty(selectedEntity.getDesc()) ? "" : ": " + selectedEntity.getDesc()));
        }
        return entity;
    }
}
