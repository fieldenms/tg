package ua.com.fielden.platform.dao;

import java.util.List;
import java.util.stream.Collectors;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.persistence.types.EntityBasedOnAbstractPersistentEntity;
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
}
