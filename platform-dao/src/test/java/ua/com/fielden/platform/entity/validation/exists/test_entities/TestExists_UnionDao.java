package ua.com.fielden.platform.entity.validation.exists.test_entities;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;

@EntityType(TestExists_Union.class)
public class TestExists_UnionDao extends CommonEntityDao<TestExists_Union> implements TestExists_UnionCo {

}
