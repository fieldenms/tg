package ua.com.fielden.platform.serialisation.jackson;

import ua.com.fielden.platform.rao.CommonEntityRao;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.serialisation.jackson.mixin.EntityTypeMixin;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * RAO implementation for master object {@link IEntityType} based on a common with DAO mixin.
 *
 * @author Developers
 *
 */
@EntityType(ua.com.fielden.platform.serialisation.jackson.EntityType.class)
public class EntityTypeRao extends CommonEntityRao<ua.com.fielden.platform.serialisation.jackson.EntityType> implements IEntityType {

    private final EntityTypeMixin mixin;

    @Inject
    public EntityTypeRao(final RestClientUtil restUtil) {
        super(restUtil);

        mixin = new EntityTypeMixin(this);
    }

}