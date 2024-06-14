package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link ITgSubSystem}.
 *
 * @author Developers
 *
 */
@EntityType(TgSubSystem.class)
public class TgSubSystemDao extends CommonEntityDao<TgSubSystem> implements ITgSubSystem {

    public static final String DEFAULT_VALUE_FOR_PROPERTY_EXPLANATION = "Default explanation";

    @Inject
    public TgSubSystemDao(final IFilter filter) {
        super(filter);
    }

    @Override
    protected void assignBeforeSave(final MetaProperty<?> prop) {
        if ("explanation".equals(prop.getName())) {
            prop.setValue(DEFAULT_VALUE_FOR_PROPERTY_EXPLANATION);
        }
    }

}