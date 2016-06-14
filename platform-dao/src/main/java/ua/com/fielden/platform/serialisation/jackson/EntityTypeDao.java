package ua.com.fielden.platform.serialisation.jackson;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.serialisation.jackson.mixin.EntityTypeMixin;

import com.google.inject.Inject;

/**
 * DAO implementation for companion object {@link IEntityType}.
 *
 * @author Developers
 *
 */
@EntityType(ua.com.fielden.platform.serialisation.jackson.EntityType.class)
public class EntityTypeDao extends CommonEntityDao<ua.com.fielden.platform.serialisation.jackson.EntityType> implements IEntityType {

    private final EntityTypeMixin mixin;

    @Inject
    public EntityTypeDao(final IFilter filter) {
        super(filter);

        mixin = new EntityTypeMixin(this);
    }

}