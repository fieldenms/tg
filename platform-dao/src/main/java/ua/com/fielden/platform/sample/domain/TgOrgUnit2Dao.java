package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.sample.domain.ITgOrgUnit2;
import ua.com.fielden.platform.sample.domain.TgOrgUnit2;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

@EntityType(TgOrgUnit2.class)
public class TgOrgUnit2Dao extends CommonEntityDao<TgOrgUnit2> implements ITgOrgUnit2 {

    @Inject
    protected TgOrgUnit2Dao(final IFilter filter) {
        super(filter);
    }
}