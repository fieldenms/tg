package ua.com.fielden.platform.entity.validation.exists.test_entities;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;

@EntityType(TestExists_UnionOwner.class)
public class TestExists_UnionOwnerDao extends CommonEntityDao<TestExists_UnionOwner> implements TestExists_UnionOwnerCo {

}
