package ua.com.fielden.platform.test.entities;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.test.entities.CompositeEntity;
import ua.com.fielden.platform.test.entities.ICompositeEntity;

@EntityType(CompositeEntity.class)
public class CompositeEntityDao extends CommonEntityDao<CompositeEntity> implements ICompositeEntity {

    @Inject
    protected CompositeEntityDao(IFilter filter) {
        super(filter);
    }

}
