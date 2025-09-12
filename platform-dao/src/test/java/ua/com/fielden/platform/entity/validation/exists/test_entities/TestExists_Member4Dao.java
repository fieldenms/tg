package ua.com.fielden.platform.entity.validation.exists.test_entities;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;

@EntityType(TestExists_Member4.class)
public class TestExists_Member4Dao extends CommonEntityDao<TestExists_Member4> implements TestExists_Member4Co {

    @Override
    public void delete(final TestExists_Member4 entity) {
        defaultDelete(entity);
    }

}
