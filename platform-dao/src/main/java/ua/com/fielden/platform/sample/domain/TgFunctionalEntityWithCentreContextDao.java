package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.sample.domain.mixin.TgFunctionalEntityWithCentreContextMixin;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * DAO implementation for companion object {@link ITgFunctionalEntityWithCentreContext}.
 *
 * @author Developers
 *
 */
@EntityType(TgFunctionalEntityWithCentreContext.class)
public class TgFunctionalEntityWithCentreContextDao extends CommonEntityDao<TgFunctionalEntityWithCentreContext> implements ITgFunctionalEntityWithCentreContext {

    private final TgFunctionalEntityWithCentreContextMixin mixin;

    @Inject
    public TgFunctionalEntityWithCentreContextDao(final IFilter filter) {
        super(filter);

        mixin = new TgFunctionalEntityWithCentreContextMixin(this);
    }

    @Override
    public IFetchProvider<TgFunctionalEntityWithCentreContext> createFetchProvider() {
        return super.createFetchProvider()
                .with("key") // this property is "required" (necessary during saving) -- should be declared as fetching property
                .with("desc")
                .with("valueToInsert", "withBrackets");
    }
}