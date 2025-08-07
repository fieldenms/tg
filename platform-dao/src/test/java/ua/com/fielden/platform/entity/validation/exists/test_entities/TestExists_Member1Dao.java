package ua.com.fielden.platform.entity.validation.exists.test_entities;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;

@EntityType(TestExists_Member1.class)
public class TestExists_Member1Dao extends CommonEntityDao<TestExists_Member1> implements TestExists_Member1Co {

    @Override
    public void delete(final TestExists_Member1 entity) {
        defaultDelete(entity);
    }

}
