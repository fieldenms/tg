package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.annotation.EntityType;
/** 
 * DAO implementation for companion object {@link ITgEntityWithTimeZoneDates}.
 * 
 * @author Developers
 *
 */
@EntityType(TgEntityWithTimeZoneDates.class)
public class TgEntityWithTimeZoneDatesDao extends CommonEntityDao<TgEntityWithTimeZoneDates> implements ITgEntityWithTimeZoneDates {

    @Inject
    public TgEntityWithTimeZoneDatesDao(final IFilter filter) {
        super(filter);
    }

    @Override
    protected IFetchProvider<TgEntityWithTimeZoneDates> createFetchProvider() {
        return super.createFetchProvider().with("key", "dateProp", "datePropUtc");
    }
    
}