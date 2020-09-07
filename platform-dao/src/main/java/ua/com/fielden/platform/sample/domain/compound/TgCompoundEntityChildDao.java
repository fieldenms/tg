package ua.com.fielden.platform.sample.domain.compound;

import java.util.Collection;
import java.util.List;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.tokens.persistent.TgCompoundEntityChild_CanDelete_Token;
import ua.com.fielden.platform.security.tokens.persistent.TgCompoundEntityChild_CanSave_Token;

/** 
 * DAO implementation for companion object {@link ITgCompoundEntityChild}.
 * 
 * @author TG Team
 *
 */
@EntityType(TgCompoundEntityChild.class)
public class TgCompoundEntityChildDao extends CommonEntityDao<TgCompoundEntityChild> implements ITgCompoundEntityChild {

    @Inject
    public TgCompoundEntityChildDao(final IFilter filter) {
        super(filter);
    }

    @Override
    @SessionRequired
    @Authorise(TgCompoundEntityChild_CanSave_Token.class)
    public TgCompoundEntityChild save(final TgCompoundEntityChild entity) {
        return super.save(entity);
    }

    @Override
    @SessionRequired
    @Authorise(TgCompoundEntityChild_CanDelete_Token.class)
    public int batchDelete(final Collection<Long> entitiesIds) {
        return defaultBatchDelete(entitiesIds);
    }
    
    @Override
    @SessionRequired
    @Authorise(TgCompoundEntityChild_CanDelete_Token.class)
    public int batchDelete(final List<TgCompoundEntityChild> entities) {
        return defaultBatchDelete(entities);
    }
    
    @Override
    protected IFetchProvider<TgCompoundEntityChild> createFetchProvider() {
        return FETCH_PROVIDER;
    }

}