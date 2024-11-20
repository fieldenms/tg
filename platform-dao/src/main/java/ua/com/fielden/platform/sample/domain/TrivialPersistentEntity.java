package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;

@MapEntityTo
@KeyType(String.class)
@CompanionObject(TrivialPersistentEntityDao.class)
public class TrivialPersistentEntity extends AbstractPersistentEntity<String> {}
