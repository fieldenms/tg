package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

@EntityType(TgModelYearCount.class)
public class TgModelYearCountDao extends CommonEntityDao<TgModelYearCount> implements ITgModelYearCount {

    @Inject
    protected TgModelYearCountDao(IFilter filter) {
        super(filter);
    }

}
