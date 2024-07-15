package ua.com.fielden.platform.sample.domain.compound;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.annotation.EntityType;

/** 
 * DAO implementation for companion object {@link ITgCompoundEntityLocator}.
 * 
 * @author TG Team
 *
 */
@EntityType(TgCompoundEntityLocator.class)
public class TgCompoundEntityLocatorDao extends CommonEntityDao<TgCompoundEntityLocator> implements ITgCompoundEntityLocator {

    @Inject
    public TgCompoundEntityLocatorDao(final IFilter filter) {
        super(filter);
    }

    @Override
    protected IFetchProvider<TgCompoundEntityLocator> createFetchProvider() {
        return FETCH_PROVIDER;
    }

}