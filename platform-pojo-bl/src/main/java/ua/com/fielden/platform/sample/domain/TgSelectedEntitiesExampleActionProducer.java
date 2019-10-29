package ua.com.fielden.platform.sample.domain;

import java.util.ArrayList;
import java.util.NoSuchElementException;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

/**
 * A producer for new instances of entity {@link TgSelectedEntitiesExampleAction}.
 *
 * @author TG Team
 *
 */
public class TgSelectedEntitiesExampleActionProducer extends DefaultEntityProducerWithContext<TgSelectedEntitiesExampleAction> {

    @Inject
    public TgSelectedEntitiesExampleActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, TgSelectedEntitiesExampleAction.class, companionFinder);
    }

    @Override
    protected TgSelectedEntitiesExampleAction provideDefaultValues(final TgSelectedEntitiesExampleAction entity) {
        // forEach method of Iterable is as raw as possible way to preserve iteration order of original selectedEntities() / selectedEntityIds() methods.
        // Please note that addAll() operation that is used in methods 'setSelectedEntitiesSeq' and 'setSelectedEntityIdsSeq' also guarantees the iteration order.
        // Stream operations have been avoided here intentionally.
        
        final ArrayList<String> selectedEntitiesSeq = new ArrayList<>();
        selectedEntities().forEach(selectedEntity -> selectedEntitiesSeq.add((String) selectedEntity.getKey()));
        entity.setSelectedEntitiesSeq(selectedEntitiesSeq);
        
        final ArrayList<String> selectedEntityIdsSeq = new ArrayList<>();
        selectedEntityIds().forEach(selectedEntityId -> selectedEntityIdsSeq.add((String) findSelectedEntity(selectedEntityId).getKey()));
        entity.setSelectedEntityIdsSeq(selectedEntityIdsSeq);
        
        return entity;
    }
    
    /**
     * Finds selected entity by <code>id</code>.
     * <p>
     * Throws {@link NullPointerException} if <code>id</code> is null.
     * Throws {@link NoSuchElementException} if there is no selected entity with the specified <code>id</code>.
     * 
     * @param id
     * @return
     */
    private AbstractEntity<?> findSelectedEntity(final Long id) {
        return selectedEntities().stream().filter(ent -> id.equals(ent.getId())).findAny().get();
    }
}