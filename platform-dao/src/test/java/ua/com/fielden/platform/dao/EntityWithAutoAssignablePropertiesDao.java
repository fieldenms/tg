package ua.com.fielden.platform.dao;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.persistence.types.EntityWithAutoAssignableProperties;

/**
 * A companion object implementation for {@link EntityWithAutoAssignableProperties} used for testing.
 * 
 * @author TG Team
 * 
 */
@EntityType(EntityWithAutoAssignableProperties.class)
public class EntityWithAutoAssignablePropertiesDao extends CommonEntityDao<EntityWithAutoAssignableProperties> {

    @Inject
    protected EntityWithAutoAssignablePropertiesDao(final IFilter filter) {
        super(filter);
    }
}
