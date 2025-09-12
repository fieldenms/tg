package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.utils.EntityUtils;

import static ua.com.fielden.platform.sample.domain.UnionEntityDetails.Property.serial;
import static ua.com.fielden.platform.sample.domain.UnionEntityDetails.Property.union;

public interface UnionEntityDetailsCo extends IEntityDao<UnionEntityDetails> {


    IFetchProvider<UnionEntityDetails> FETCH_PROVIDER = EntityUtils.fetch(UnionEntityDetails.class)
            .with(serial, union);
}
