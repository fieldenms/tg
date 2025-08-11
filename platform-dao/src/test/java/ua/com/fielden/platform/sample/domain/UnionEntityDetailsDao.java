package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;

@EntityType(UnionEntityDetails.class)
public class UnionEntityDetailsDao extends CommonEntityDao<UnionEntityDetails> implements UnionEntityDetailsCo {

    @Override
    protected IFetchProvider<UnionEntityDetails> createFetchProvider() {
        return FETCH_PROVIDER;
    }

}
