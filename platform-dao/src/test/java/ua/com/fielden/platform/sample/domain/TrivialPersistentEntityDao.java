package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;

@EntityType(TrivialPersistentEntity.class)
public class TrivialPersistentEntityDao extends CommonEntityDao<TrivialPersistentEntity> {}
