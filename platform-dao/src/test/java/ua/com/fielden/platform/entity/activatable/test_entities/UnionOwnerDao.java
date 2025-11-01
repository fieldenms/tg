package ua.com.fielden.platform.entity.activatable.test_entities;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;

@EntityType(UnionOwner.class)
public class UnionOwnerDao extends CommonEntityDao<UnionOwner> implements UnionOwnerCo {

}
