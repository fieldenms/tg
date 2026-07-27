package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.meta.MetaProperty;

import java.util.stream.Stream;

/// DAO implementation for companion object [ITgSubSystem].
///
@EntityType(TgSubSystem.class)
public class TgSubSystemDao extends CommonEntityDao<TgSubSystem> implements ITgSubSystem {

    public static final String DEFAULT_VALUE_FOR_PROPERTY_EXPLANATION = "Default explanation";

    @Override
    protected void assignBeforeSave(final MetaProperty<?> prop) {
        if ("explanation".equals(prop.getName())) {
            prop.setValue(DEFAULT_VALUE_FOR_PROPERTY_EXPLANATION);
        }
    }

    @Override
    @SessionRequired
    public int batchInsert(Stream<TgSubSystem> newEntities, int batchSize) {
        return defaultBatchInsert(newEntities, batchSize);
    }

}