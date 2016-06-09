package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.sample.domain.mixin.TgEntityForColourMasterMixin;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

/**
 * DAO implementation for companion object {@link ITgEntityForColourMaster}.
 *
 * @author Developers
 *
 */
@EntityType(TgEntityForColourMaster.class)
public class TgEntityForColourMasterDao extends CommonEntityDao<TgEntityForColourMaster>implements ITgEntityForColourMaster {

    private final TgEntityForColourMasterMixin mixin;

    @Inject
    public TgEntityForColourMasterDao(final IFilter filter) {
        super(filter);

        mixin = new TgEntityForColourMasterMixin(this);
    }

    @Override
    public IFetchProvider<TgEntityForColourMaster> createFetchProvider() {
        return super.createFetchProvider().with("key").with("stringProp").with("booleanProp").with("colourProp");
    }

}