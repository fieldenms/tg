package ua.com.fielden.platform.test.entities;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

@EntityType(ComplexKeyEntity.class)
public class ComplexKeyEntityDao extends CommonEntityDao<ComplexKeyEntity> implements IComplexKeyEntity {

    @Inject
    protected ComplexKeyEntityDao(IFilter filter) {
        super(filter);
    }

}
