package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.sample.domain.ITgOrgUnit5;
import ua.com.fielden.platform.sample.domain.TgOrgUnit5;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

@EntityType(TgOrgUnit5.class)
public class TgOrgUnit5Dao extends CommonEntityDao<TgOrgUnit5> implements ITgOrgUnit5 {

    @Inject
    protected TgOrgUnit5Dao(final IFilter filter) {
        super(filter);
    }
}