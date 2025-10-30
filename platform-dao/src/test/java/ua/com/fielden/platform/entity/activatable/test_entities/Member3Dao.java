package ua.com.fielden.platform.entity.activatable.test_entities;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;

@EntityType(Member3.class)
public class Member3Dao extends CommonEntityDao<Member3> implements Member3Co {

}
