package ua.com.fielden.platform.sample.domain.compound;

import static java.lang.String.format;

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
        // the following logic is used in web tests for 'PERSISTED / NEW cases' where we change detail entity and expect for compound master title (sectionTitle) to be updated
        if (entity.getId() != null && (entity.getDesc() != null && entity.getDesc().endsWith(" detail_CHANGED") /* 1TEST and NEWXX */ || "1TEST detail".equals(entity.getDesc()))) { // if we add / remove '_CHANGED' part for detail entity description ...
            final TgCompoundEntity masterEntity = co$(TgCompoundEntity.class).findById(entity.getId(), co$(TgCompoundEntity.class).getFetchProvider().fetchModel()); // ... then fetch master entity ...
            masterEntity.setDesc(format("%s (%s)", masterEntity.getKey(), entity.getDesc())); // ... and change its description accordingly ... 
            co$(TgCompoundEntity.class).save(masterEntity); // ... and save master entity.
        }
        return result;
    }

    @Override
    protected IFetchProvider<TgCompoundEntityDetail> createFetchProvider() {
        return FETCH_PROVIDER;
    }

}