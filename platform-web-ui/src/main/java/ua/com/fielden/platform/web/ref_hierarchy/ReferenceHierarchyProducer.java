package ua.com.fielden.platform.web.ref_hierarchy;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchKeyAndDescOnly;
import static ua.com.fielden.platform.ref_hierarchy.ReferenceHierarchyLevel.REFERENCE_INSTANCE;
import static ua.com.fielden.platform.utils.EntityUtils.hasDescProperty;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isSyntheticBasedOnPersistentEntityType;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.ref_hierarchy.ReferenceHierarchy;
import ua.com.fielden.platform.reflection.exceptions.ReflectionException;

/**
 * The producer for {@link ReferenceHierarchy} entity used to initialize {@link ReferenceHierarchy} instance with data that was calculated based on data received from client.
 *
 * @author TG Team
 *
 */
public class ReferenceHierarchyProducer extends DefaultEntityProducerWithContext<ReferenceHierarchy> {

    @Inject
    public ReferenceHierarchyProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, ReferenceHierarchy.class, companionFinder);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    protected ReferenceHierarchy provideDefaultValues(final ReferenceHierarchy entity) {
        if (selectedEntitiesNotEmpty() || currentEntityNotEmpty()) {
            final AbstractEntity<?> selectedEntity = currentEntityNotEmpty() ? currentEntity() : selectedEntities().get(0);
            // we need to be smart about getting the type as it may be a synthetic entity that represents a persistent entity
            // and so, we really need to handle this case here
            final Class<? extends AbstractEntity<?>> entityType;
            if (isPersistedEntityType(selectedEntity.getType())) {
                entityType = selectedEntity.getType();
                entity.setRefEntityType(entityType.getName());
            } else if (isSyntheticBasedOnPersistentEntityType(selectedEntity.getType())) {
                entityType = (Class<? extends AbstractEntity<?>>) selectedEntity.getType().getSuperclass();
                entity.setRefEntityType(entityType.getName());
            } else {
                throw new ReflectionException(format("Unsupported entity type [%s] for Reference Hiearchy.", selectedEntity.getType().getSimpleName()));
            }
            final fetch fetchModel = fetchKeyAndDescOnly(entityType);
            final AbstractEntity<?> refetchedEntity = co(entityType).findById(selectedEntity.getId(), hasDescProperty(entityType) ? fetchModel.with("desc") : fetchModel);
            entity.setRefEntityId(refetchedEntity.getId());
            entity.setLoadedHierarchyLevel(REFERENCE_INSTANCE);
            entity.setActiveOnly(true);
            entity.setTitle(refetchedEntity.getKey() + (StringUtils.isEmpty(refetchedEntity.getDesc()) ? "" : ": " + refetchedEntity.getDesc()));
        }
        return entity;
    }
}
