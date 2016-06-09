package ua.com.fielden.platform.test.entities;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

@EntityType(ComplexKeyEntity.class)
public class ComplexKeyEntityDao extends CommonEntityDao<ComplexKeyEntity> implements IComplexKeyEntity {

    @Inject
    protected ComplexKeyEntityDao(IFilter filter) {
        super(filter);
    }

}
