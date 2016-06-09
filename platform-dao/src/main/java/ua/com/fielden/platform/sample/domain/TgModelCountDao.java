package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

@EntityType(TgModelCount.class)
public class TgModelCountDao extends CommonEntityDao<TgModelCount> implements ITgModelCount {

    @Inject
    protected TgModelCountDao(IFilter filter) {
        super(filter);
    }

}
