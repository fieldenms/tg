package ua.com.fielden.platform.sample.domain.mixin;

import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.sample.domain.ITgAuthorRoyalty;
import ua.com.fielden.platform.sample.domain.TgAuthorRoyalty;
import ua.com.fielden.platform.sample.domain.TgAuthorship;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

/**
 * Mixin implementation for companion object {@link ITgAuthorRoyalty}.
 *
 * @author Developers
 *
 */
public class TgAuthorRoyaltyMixin {


    private final ITgAuthorRoyalty companion;


    public TgAuthorRoyaltyMixin(final ITgAuthorRoyalty companion) {
        this.companion = companion;
    }

    public IPage<TgAuthorRoyalty> findDetails(final TgAuthorship masterEntity, final fetch<TgAuthorRoyalty> fetch, final int pageCapacity) {
        final EntityResultQueryModel<TgAuthorRoyalty> selectModel = select(TgAuthorRoyalty.class).where().prop("tgAuthorship").eq().val(masterEntity).model();
        return companion.firstPage(from(selectModel).with(orderBy().prop("paymentDate").asc().model()).with(fetch).model(), pageCapacity);
    }

    public TgAuthorRoyalty saveDetails(final TgAuthorship masterEntity, final TgAuthorRoyalty detailEntity) {
        return companion.save(detailEntity);
    }

    public void deleteDetails(final TgAuthorship masterEntity, final TgAuthorRoyalty detailEntity) {
        companion.delete(detailEntity);
    }

}