package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.sample.domain.ITgOrgUnit4;
import ua.com.fielden.platform.sample.domain.TgOrgUnit4;

import com.google.inject.Inject;

@EntityType(TgOrgUnit4.class)
public class TgOrgUnit4Dao extends CommonEntityDao<TgOrgUnit4> implements ITgOrgUnit4 {

    @Inject
    protected TgOrgUnit4Dao(final IFilter filter) {
        super(filter);
    }
}