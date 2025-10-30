package ua.com.fielden.platform.entity.activatable.test_entities;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;

@EntityType(Member1.class)
public class Member1Dao extends CommonEntityDao<Member1> implements Member1Co {

    @Override
    public Member1 new_() {
        return super.new_().setActive(true);
    }

}
