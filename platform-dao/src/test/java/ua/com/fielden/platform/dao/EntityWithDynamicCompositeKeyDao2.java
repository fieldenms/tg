package ua.com.fielden.platform.dao;

import ua.com.fielden.platform.dao2.CommonEntityDao2;
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
public class EntityWithDynamicCompositeKeyDao2 extends CommonEntityDao2<EntityWithDynamicCompositeKey> {

    @Inject
    protected EntityWithDynamicCompositeKeyDao2(final IFilter filter) {
	super(filter);
    }
}
