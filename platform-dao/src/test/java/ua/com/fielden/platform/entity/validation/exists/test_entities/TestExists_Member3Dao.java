package ua.com.fielden.platform.entity.validation.exists.test_entities;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;

@EntityType(TestExists_Member3.class)
public class TestExists_Member3Dao extends CommonEntityDao<TestExists_Member3> implements TestExists_Member3Co {

    @Override
    public void delete(final TestExists_Member3 entity) {
        defaultDelete(entity);
    }

}
