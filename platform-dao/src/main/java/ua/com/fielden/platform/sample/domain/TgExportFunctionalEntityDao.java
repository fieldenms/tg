package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.sample.domain.mixin.TgExportFunctionalEntityMixin;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * DAO implementation for companion object {@link ITgExportFunctionalEntity}.
 *
 * @author Developers
 *
 */
@EntityType(TgExportFunctionalEntity.class)
public class TgExportFunctionalEntityDao extends CommonEntityDao<TgExportFunctionalEntity> implements ITgExportFunctionalEntity {

    private final TgExportFunctionalEntityMixin mixin;

    @Inject
    public TgExportFunctionalEntityDao(final IFilter filter) {
        super(filter);

        mixin = new TgExportFunctionalEntityMixin(this);
    }

    @Override
    public IFetchProvider<TgExportFunctionalEntity> createFetchProvider() {
        return super.createFetchProvider()
                // .with("key") // this property is "required" (necessary during saving) -- should be declared as fetching property
                .with("parentEntity", "parentEntity.key", "parentEntity.integerProp");
    }

}