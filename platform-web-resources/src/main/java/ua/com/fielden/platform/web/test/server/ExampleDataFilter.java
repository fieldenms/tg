package ua.com.fielden.platform.web.test.server;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.cond;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader.getOriginalType;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties;
import ua.com.fielden.platform.sample.domain.compound.TgCompoundEntity;
import ua.com.fielden.platform.sample.domain.compound.TgCompoundEntityChild;
import ua.com.fielden.platform.sample.domain.compound.TgCompoundEntityDetail;

public class ExampleDataFilter implements IFilter {
    @Override
    public <T extends AbstractEntity<?>> ConditionModel enhance(final Class<T> entityType, final String typeAlias, final String username) {
        final Class<T> originalType = getOriginalType(entityType);
        final String prefix = typeAlias != null ? typeAlias + "." : "";
        if (originalType == TgPersistentEntityWithProperties.class) { // filter TgPersistentEntityWithProperties ...
            return cond().prop(prefix + "key").ne().val("FILTERED").model();
        }
        if (originalType == TgCompoundEntity.class) { // ... and TgCompoundEntity
            return cond().prop(prefix + "key").ne().val("FILTERED1").model();
        }
        if (originalType == TgCompoundEntityDetail.class) { // ... and TgCompoundEntityDetail
            return cond().prop(prefix + "key.key").ne().val("FILTERED2").model();
        }
        if (originalType == TgCompoundEntityChild.class) { // ... and TgCompoundEntityChild instances
            return cond().prop(prefix + "tgCompoundEntity.key").ne().val("FILTERED2").model();
        }
        return null;
    }
}