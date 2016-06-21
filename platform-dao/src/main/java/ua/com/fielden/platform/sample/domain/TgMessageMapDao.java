package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;
/** 
 * DAO implementation for companion object {@link ITgMessageMap}.
 * 
 * @author Developers
 *
 */
@EntityType(TgMessageMap.class)
public class TgMessageMapDao extends CommonEntityDao<TgMessageMap> implements ITgMessageMap {

    @Inject
    public TgMessageMapDao(final IFilter filter) {
        super(filter);
    }

    @Override
    public IFetchProvider<TgMessageMap> createFetchProvider() {
        return super.createFetchProvider()
                .with("key"); // this property is "required" (necessary during saving) -- should be declared as fetching property
    }
}