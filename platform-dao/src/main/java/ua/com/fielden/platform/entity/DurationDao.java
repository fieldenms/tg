package ua.com.fielden.platform.entity;

import java.util.Collection;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.tokens.persistent.Duration_CanDelete_Token;
import ua.com.fielden.platform.security.tokens.persistent.Duration_CanSave_Token;

/**
 * DAO implementation for companion object {@link DurationCo}.
 *
 * @author TG Team
 *
 */
@EntityType(Duration.class)
public class DurationDao extends CommonEntityDao<Duration> implements DurationCo {
    
    @Inject
    public DurationDao(final IFilter filter) {
        super(filter);
    }
    
    @Override
    @SessionRequired
    @Authorise(Duration_CanSave_Token.class)
    public Duration save(final Duration entity) {
        return super.save(entity);
    }

    @Override
    @SessionRequired
    @Authorise(Duration_CanDelete_Token.class)
    public int batchDelete(final Collection<Long> entitiesIds) {
        return defaultBatchDelete(entitiesIds);
    }
    
    @Override
    protected IFetchProvider<Duration> createFetchProvider() {
         return FETCH_PROVIDER;
    }
    
}