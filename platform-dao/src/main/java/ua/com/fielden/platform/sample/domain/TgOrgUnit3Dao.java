package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.sample.domain.ITgOrgUnit3;
import ua.com.fielden.platform.sample.domain.TgOrgUnit3;

import com.google.inject.Inject;

@EntityType(TgOrgUnit3.class)
public class TgOrgUnit3Dao extends CommonEntityDao<TgOrgUnit3> implements ITgOrgUnit3 {

    @Inject
    protected TgOrgUnit3Dao(final IFilter filter) {
        super(filter);
    }
}