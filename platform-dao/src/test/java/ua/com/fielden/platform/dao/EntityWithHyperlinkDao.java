package ua.com.fielden.platform.dao;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithHyperlink;

/**
 * A DAO for {@link EntityWithHyperlink} used for testing.
 *
 * @author 01es
 *
 */
@EntityType(EntityWithHyperlink.class)
public class EntityWithHyperlinkDao extends CommonEntityDao<EntityWithHyperlink> {

    @Inject
    protected EntityWithHyperlinkDao(final IFilter filter) {
        super(filter);
    }
}
