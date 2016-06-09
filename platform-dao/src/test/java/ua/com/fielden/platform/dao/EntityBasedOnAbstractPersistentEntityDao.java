package ua.com.fielden.platform.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.persistence.types.EntityBasedOnAbstractPersistentEntity;
import ua.com.fielden.platform.persistence.types.EntityBasedOnAbstractPersistentEntity2;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

/**
 * A companion object implementation for {@link EntityBasedOnAbstractPersistentEntity} used for testing.
 * 
 * @author TG Team
 * 
 */
@EntityType(EntityBasedOnAbstractPersistentEntity.class)
public class EntityBasedOnAbstractPersistentEntityDao extends CommonEntityDao<EntityBasedOnAbstractPersistentEntity> {

    @Inject
    protected EntityBasedOnAbstractPersistentEntityDao(final IFilter filter) {
        super(filter);
    }
    
    @SessionRequired
    public List<EntityBasedOnAbstractPersistentEntity> saveInSingleTransaction(final List<EntityBasedOnAbstractPersistentEntity> toSave) {
        return toSave.stream().map(entity -> save(entity)).collect(Collectors.toList());
    }
    
    @SessionRequired
    public List<AbstractPersistentEntity<?>> nestedSaveWithDifferentCompanion(
            final List<EntityBasedOnAbstractPersistentEntity> thisToSave,
            final List<EntityBasedOnAbstractPersistentEntity2> thatToSave) {
        
        final List<AbstractPersistentEntity<?>> result = new ArrayList<>();
        result.addAll(thisToSave.stream().map(entity -> save(entity)).collect(Collectors.toList()));
        
        final EntityBasedOnAbstractPersistentEntity2Dao thatCo = co(EntityBasedOnAbstractPersistentEntity2.class);
        result.addAll(thatToSave.stream().map(entity -> thatCo.save(entity)).collect(Collectors.toList()));
        
        return result;
    }
}
