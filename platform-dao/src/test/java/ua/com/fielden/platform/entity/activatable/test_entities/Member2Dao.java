package ua.com.fielden.platform.entity.activatable.test_entities;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;

@EntityType(Member2.class)
public class Member2Dao extends CommonEntityDao<Member2> implements Member2Co {

}
