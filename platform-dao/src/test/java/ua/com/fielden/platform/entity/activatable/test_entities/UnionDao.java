package ua.com.fielden.platform.entity.activatable.test_entities;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;

@EntityType(Union.class)
public class UnionDao extends CommonEntityDao<Union> implements UnionCo {

}
