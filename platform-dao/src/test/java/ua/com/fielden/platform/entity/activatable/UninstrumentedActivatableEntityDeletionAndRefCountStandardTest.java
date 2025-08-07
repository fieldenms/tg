package ua.com.fielden.platform.entity.activatable;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.EntityUtils;

import static ua.com.fielden.platform.entity.factory.EntityFactory.newPlainEntity;

public class UninstrumentedActivatableEntityDeletionAndRefCountStandardTest extends ActivatableEntityDeletionAndRefCountStandardTest {

    @SuppressWarnings("unchecked")
    @Override
    protected void delete(final AbstractEntity<?> entity) {
        final var co$ = (IEntityDao<AbstractEntity<?>>) co$(entity.getType());
        final var plainEntity = newPlainEntity(entity.getType(), entity.getId());
        EntityUtils.copy(entity, plainEntity);
        co$.delete(plainEntity);
    }

}
