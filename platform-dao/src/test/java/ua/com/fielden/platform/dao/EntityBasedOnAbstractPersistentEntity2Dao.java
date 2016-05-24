package ua.com.fielden.platform.dao;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.persistence.types.EntityBasedOnAbstractPersistentEntity2;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

/**
 * A companion object implementation for {@link EntityBasedOnAbstractPersistentEntity2} used for testing.
 * 
 * @author TG Team
 * 
 */
@EntityType(EntityBasedOnAbstractPersistentEntity2.class)
public class EntityBasedOnAbstractPersistentEntity2Dao extends CommonEntityDao<EntityBasedOnAbstractPersistentEntity2> {

    @Inject
    protected EntityBasedOnAbstractPersistentEntity2Dao(final IFilter filter) {
        super(filter);
    }
    
}
