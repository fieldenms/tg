package ua.com.fielden.platform.companion;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;

@EntityType(TrivialPersistentEntity.class)
public class TrivialPersistentEntityDao extends CommonEntityDao<TrivialPersistentEntity> {}
