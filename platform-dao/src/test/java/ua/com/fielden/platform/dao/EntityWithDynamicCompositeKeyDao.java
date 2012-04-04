package ua.com.fielden.platform.dao;

import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.persistence.composite.EntityWithDynamicCompositeKey;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * This is a test DOA with a composite key implemented by {@link DynamicEntityKey}.
 *
 * @author 01es
 *
 */
@EntityType(EntityWithDynamicCompositeKey.class)
public class EntityWithDynamicCompositeKeyDao extends CommonEntityDao<EntityWithDynamicCompositeKey> {

    @Inject
    protected EntityWithDynamicCompositeKeyDao(final IFilter filter) {
	super(filter);
    }
}
