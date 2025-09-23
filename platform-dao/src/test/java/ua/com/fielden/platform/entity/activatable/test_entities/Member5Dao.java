package ua.com.fielden.platform.entity.activatable.test_entities;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;

@EntityType(Member5.class)
public class Member5Dao extends CommonEntityDao<Member5> implements Member5Co {

    @Override
    public Member5 new_() {
        return super.new_().setActive(true);
    }

}
