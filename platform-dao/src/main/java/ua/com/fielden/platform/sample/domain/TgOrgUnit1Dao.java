package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.sample.domain.ITgOrgUnit1;
import ua.com.fielden.platform.sample.domain.TgOrgUnit1;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

@EntityType(TgOrgUnit1.class)
public class TgOrgUnit1Dao extends CommonEntityDao<TgOrgUnit1> implements ITgOrgUnit1 {

    @Inject
    protected TgOrgUnit1Dao(final IFilter filter) {
        super(filter);
    }
}