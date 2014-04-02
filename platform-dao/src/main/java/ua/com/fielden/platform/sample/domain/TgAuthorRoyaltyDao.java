package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import ua.com.fielden.platform.sample.domain.TgAuthorship;
import java.util.Map;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.sample.domain.mixin.TgAuthorRoyaltyMixin;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import com.google.inject.Inject;

/**
 * DAO implementation for companion object {@link ITgAuthorRoyalty} based on a common with DAO mixin.
 * 
 * @author Developers
 * 
 */
@EntityType(TgAuthorRoyalty.class)
public class TgAuthorRoyaltyDao extends CommonEntityDao<TgAuthorRoyalty> implements ITgAuthorRoyalty {

    private final TgAuthorRoyaltyMixin mixin;

    @Inject
    public TgAuthorRoyaltyDao(final IFilter filter) {
        super(filter);

        mixin = new TgAuthorRoyaltyMixin(this);
    }

    @Override
    public IPage<TgAuthorRoyalty> findDetails(final TgAuthorship masterEntity, final fetch<TgAuthorRoyalty> fetch, final int pageCapacity) {
        return mixin.findDetails(masterEntity, fetch, pageCapacity);
    }

    @Override
    public TgAuthorRoyalty saveDetails(final TgAuthorship masterEntity, final TgAuthorRoyalty detailEntity) {
        return mixin.saveDetails(masterEntity, detailEntity);
    }

    @Override
    public void deleteDetails(final TgAuthorship masterEntity, final TgAuthorRoyalty detailEntity) {
        mixin.deleteDetails(masterEntity, detailEntity);
    }

    @Override
    @SessionRequired
    public void delete(final TgAuthorRoyalty entity) {
        defaultDelete(entity);
    }

    @Override
    @SessionRequired
    public void delete(final EntityResultQueryModel<TgAuthorRoyalty> model, final Map<String, Object> paramValues) {
        defaultDelete(model, paramValues);
    }

}