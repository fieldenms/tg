package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.sample.domain.mixin.TgSubSystemMixin;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * DAO implementation for companion object {@link ITgSubSystem}.
 *
 * @author Developers
 *
 */
@EntityType(TgSubSystem.class)
public class TgSubSystemDao extends CommonEntityDao<TgSubSystem> implements ITgSubSystem {

    private final TgSubSystemMixin mixin;

    @Inject
    public TgSubSystemDao(final IFilter filter) {
        super(filter);

        mixin = new TgSubSystemMixin(this);
    }

    @Override
    protected void assignBeforeSave(final MetaProperty<?> prop) {
        if ("explanation".equals(prop.getName())) {
            prop.setValue("Default explanation");
        }
    }

}