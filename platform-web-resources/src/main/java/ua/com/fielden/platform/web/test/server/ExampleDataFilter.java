package ua.com.fielden.platform.web.test.server;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.cond;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader.getOriginalType;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties;

public class ExampleDataFilter implements IFilter {
    @Override
    public <T extends AbstractEntity<?>> ConditionModel enhance(final Class<T> entityType, final String typeAlias, final String username) {
        final Class<T> originalType = getOriginalType(entityType);
        if (originalType == TgPersistentEntityWithProperties.class) { // only filter TgPersistentEntityWithProperties instances
            return cond().prop("key").ne().val("FILTERED").model();
        }
        return null;
    }
}