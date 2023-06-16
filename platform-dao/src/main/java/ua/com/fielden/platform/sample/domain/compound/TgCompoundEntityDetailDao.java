package ua.com.fielden.platform.sample.domain.compound;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.tokens.persistent.TgCompoundEntityDetail_CanSave_Token;

/** 
 * DAO implementation for companion object {@link ITgCompoundEntityDetail}.
 * 
 * @author TG Team
 *
 */
@EntityType(TgCompoundEntityDetail.class)
public class TgCompoundEntityDetailDao extends CommonEntityDao<TgCompoundEntityDetail> implements ITgCompoundEntityDetail {

    @Inject
    public TgCompoundEntityDetailDao(final IFilter filter) {
        super(filter);
    }

    @Override
    @SessionRequired
    @Authorise(TgCompoundEntityDetail_CanSave_Token.class)
    public TgCompoundEntityDetail save(final TgCompoundEntityDetail entity) {
        final TgCompoundEntityDetail result = super.save(entity);
        if (entity.getId() != null && ("1TEST detail_CHANGED".equals(entity.getDesc()) || "1TEST detail".equals(entity.getDesc()))) {
            final TgCompoundEntity masterEntity = co$(TgCompoundEntity.class).findById(entity.getId(), co$(TgCompoundEntity.class).getFetchProvider().fetchModel());
            masterEntity.setDesc("1TEST (" + entity.getDesc() + ")");
            co$(TgCompoundEntity.class).save(masterEntity);
        }
        return result;
    }

    @Override
    protected IFetchProvider<TgCompoundEntityDetail> createFetchProvider() {
        return FETCH_PROVIDER;
    }

}