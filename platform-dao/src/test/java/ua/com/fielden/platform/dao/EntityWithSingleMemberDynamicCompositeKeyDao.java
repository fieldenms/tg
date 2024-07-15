package ua.com.fielden.platform.dao;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.persistence.composite.EntityWithSingleMemberDynamicCompositeKey;

/**
 * This is a test DAO with a single member composite key.
 * 
 * @author TG Team
 * 
 */
@EntityType(EntityWithSingleMemberDynamicCompositeKey.class)
public class EntityWithSingleMemberDynamicCompositeKeyDao extends CommonEntityDao<EntityWithSingleMemberDynamicCompositeKey> {

    @Inject
    protected EntityWithSingleMemberDynamicCompositeKeyDao(final IFilter filter) {
        super(filter);
    }
}
