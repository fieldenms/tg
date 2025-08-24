package ua.com.fielden.platform.entity.activatable;

import ua.com.fielden.platform.entity.AbstractEntity;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchIdOnly;

public class ProxiedActivatableEntityDeletionAndRefCountUnionTest extends ActivatableEntityDeletionAndRefCountUnionTest {

    @SuppressWarnings("unchecked")
    @Override
    protected void delete(final AbstractEntity<?> entity) {
        final var type = (Class <AbstractEntity<?>>) entity.getType();
        final var co$ = co$(type);
        final var idOnlyEntity = co$.findByEntityAndFetch(fetchIdOnly(type), entity);
        co$.delete(idOnlyEntity);
    }

}
