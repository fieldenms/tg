package ua.com.fielden.platform.serialisation.jackson;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link IEntityType}.
 *
 * @author Developers
 *
 */
@EntityType(ua.com.fielden.platform.serialisation.jackson.EntityType.class)
public class EntityTypeDao extends CommonEntityDao<ua.com.fielden.platform.serialisation.jackson.EntityType> implements IEntityType {

    @Inject
    public EntityTypeDao(final IFilter filter) {
        super(filter);
    }

}